package unimelb.bitbox.util.ConnectionUtils;

import unimelb.bitbox.protocol.IRequest;
import unimelb.bitbox.protocol.IResponse;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class UDPConnection extends Connection {

    private static final ConcurrentHashMap<HostPort, UDPConnection> udpConnectionMap = new ConcurrentHashMap<>();
    private static Logger log = Logger.getLogger(UDPConnection.class.getName());

    private final DatagramSocket serverSocket;
    private final InetAddress hostAddress;

    // use linkedHashMap to get LRU-liked order, so the oldest request should be in front
    // default is accessOrder = false
    private final LinkedHashMap<IRequest, WaitingInfo> waitingList = new LinkedHashMap<>();

    private boolean isClosed = false;
    private boolean isActive = false;



    protected static boolean distributeMessage(DatagramPacket packet) {
        UDPConnection connection =
                udpConnectionMap.get(new HostPort(packet.getAddress().getHostAddress(), packet.getPort()));
        if (connection == null) {
            return false;
        }
        String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        connection.receive(msg);
        return true;
    }


    public UDPConnection(DatagramSocket serverSocket, HostPort hostPort, InetAddress hostAddress, int actualPort) throws CException {
        super(ConnectionType.INCOMING);
        this.serverSocket = serverSocket;
        this.hostPort = hostPort;
        // allow the advertisedName be fake
        // actual one should be InetAddress.getByName(hostPort.host)
        this.hostAddress = hostAddress;
        if (this.hostPort.port != actualPort) {
            throw new CException("Given port does not math with the actual port");
        }

        // register self
        // TODO: check duplicates
        if (udpConnectionMap.putIfAbsent(new HostPort(this.hostAddress.getHostAddress(), hostPort.port), this) != null) {
            throw new CException("Connection already exists");
        }
    }


    @Override
    public void sendAsync(Protocol protocol) {
        // add to waiting list if the protocol requires retry
        if (protocol instanceof IRequest) {
            IRequest request = (IRequest) protocol;
            synchronized (waitingList) {
                WaitingInfo waitingInfo = waitingList.remove(request);
                if (waitingInfo == null) {
                    // new request, create new waiting info
                    waitingInfo = new WaitingInfo();
                } else {
                    // still waiting for the same old request, reset the timestamp but keep the retryCount
                    waitingInfo.timestamp = System.currentTimeMillis();
                }

                // put in the back
                waitingList.put(request, waitingInfo);
            }
        }
        sendDatagram(protocol);
    }


    private void sendDatagram(Protocol protocol) {
        String msg = ProtocolFactory.marshalProtocol(protocol);
        byte[] buffer = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, hostAddress, hostPort.port);
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            close();
        }
    }

    public void active() {
        if (!isActive) {
            isActive = true;
        }
    }


    private void receive(String msg) {
        log.info(currentHostPort() + " Message Received: "
                + msg.substring(0, Math.min(MAX_LOG_LEN, msg.length())));
    }

    @Override
    public void close() {
        // TODO: implement
        synchronized (this) {
            if (isClosed) {
                return;
            }
            isClosed = true;
        }

        log.info(currentHostPort() + " Connection Closed");

        udpConnectionMap.remove(new HostPort(this.hostAddress.getHostAddress(), hostPort.port), this);
        if (isActive) {
            ConnectionManager.getInstance().removeConnection(this);
        }

    }

    @Override
    public void abortWithInvalidProtocol(String additionalMsg) {
        Protocol.InvalidProtocol invalidProtocol = new Protocol.InvalidProtocol();
        invalidProtocol.msg = additionalMsg;
        sendAsync(invalidProtocol);
        close();
    }

    @Override
    public void markRequestAsDone(IResponse response) {
        IRequest request = ProtocolFactory.identifyRes(response);
        synchronized (waitingList) {
            waitingList.remove(request);
        }
    }

    protected static class CException extends Exception {
        protected CException(String message) {
            super(message);
        }
    }

    protected static class WaitingInfo {
        // TODO: get from config
        private static final int MAX_RETRY = 3;

        int retryCount;
        long timestamp;

        protected WaitingInfo() {
            retryCount = 0;
            timestamp = System.currentTimeMillis();
        }

        protected boolean doRetry() {
            if (retryCount >= MAX_RETRY) return false;
            retryCount += 1;
            timestamp = System.currentTimeMillis();
            return true;
        }
    }

}
