package unimelb.bitbox.util.ConnectionUtils.Peer;

import javafx.util.Pair;
import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.IRequest;
import unimelb.bitbox.protocol.IResponse;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.*;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;


/**
 * Class for UDP connection with other peer
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */

public class UDPConnection extends Connection {

    private static final long BLOCK_SIZE =
            Math.min(Long.parseLong(Configuration.getConfigurationValue(Constants.CONFIG_FIELD_BLOCKSIZE)), 8192);

    protected static final long UDP_TIMEOUT_MS = Long.parseLong(Configuration.getConfigurationValue("udpTimeout"));
    protected static final int MAX_RETRY = Integer.parseInt(Configuration.getConfigurationValue("udpRetries"));

    private static final ConcurrentHashMap<HostPort, UDPConnection> udpConnectionMap = new ConcurrentHashMap<>();
    private static final int INCOMING_CONNECTION_FIRST_SYNC_WAIT_TIME = 1000;
    private static Logger log = Logger.getLogger(UDPConnection.class.getName());

    private final DatagramSocket serverSocket;
    private final InetAddress hostAddress;

    // use linkedHashMap to get LRU-liked order, so the oldest request should be in front
    // default is accessOrder = false
    private final LinkedHashMap<IRequest, WaitingInfo> waitingList = new LinkedHashMap<>();

    private boolean isClosed = false;
    private boolean isActive = false;

    private UDPOutgoingConnectionHelper outgoingConnectionHelper;


    // for outgoing connection
    // this semaphore will be released until the connection is active
    protected final Semaphore activeSemaphore = new Semaphore(1);
    protected Pair<Boolean, String> handshakeResult = null;



    protected static boolean distributeMessage(DatagramPacket packet) {
        UDPConnection connection =
                udpConnectionMap.get(new HostPort(packet.getAddress().getHostAddress(), packet.getPort()));
        if (connection == null) {
            return false;
        }
        String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                "handle UDP request",
                Priority.NORMAL,
                () -> connection.receive(msg)
        ));

        return true;
    }

    protected static void checkTimeoutRequest() {
        for (UDPConnection conn : udpConnectionMap.values()) {
            conn.retryTimeoutRequest();
        }
    }

    /**
     * Constructor for incoming connection
     * @param serverSocket      the established socket
     * @param hostPort          host and port information
     * @param hostAddress       host address
     * @param actualPort        actual port
     * @throws CException       customized exception
     */
    public UDPConnection(DatagramSocket serverSocket, HostPort hostPort, InetAddress hostAddress, int actualPort) throws CException {
        super(ConnectionType.INCOMING);
        try {
            activeSemaphore.acquire();
        } catch (InterruptedException ignored) { }
        this.serverSocket = serverSocket;
        this.hostPort = hostPort;
        // allow the advertisedName be fake
        // actual one should be InetAddress.getByName(hostPort.host)

        this.hostAddress = hostAddress;
        if (this.hostPort.port != actualPort) {
            throw new CException("Given port does not math with the actual port");
        }

        // register self
        if (udpConnectionMap.putIfAbsent(new HostPort(this.hostAddress.getHostAddress(), hostPort.port), this) != null) {
            throw new CException("Connection already exists");
        }
    }

    /**
     * Constructor for outgoing connection
     * @param serverSocket                  the established socket
     * @param hostPort                      host and port information
     * @param hostAddress                   host address
     * @param outgoingConnectionHelper      the established outgoing connection helper
     * @throws CException                   customized exception
     */
    public UDPConnection(DatagramSocket serverSocket, HostPort hostPort, InetAddress hostAddress, UDPOutgoingConnectionHelper outgoingConnectionHelper) throws CException {
        super(ConnectionType.OUTGOING);
        try {
            activeSemaphore.acquire();
        } catch (InterruptedException ignored) { }
        this.serverSocket = serverSocket;
        this.hostPort = hostPort;
        this.outgoingConnectionHelper = outgoingConnectionHelper;
        this.hostAddress = hostAddress;

        // register self
        if (udpConnectionMap.putIfAbsent(new HostPort(this.hostAddress.getHostAddress(), hostPort.port), this) != null) {
            throw new CException("Connection already exists");
        }
    }

    // Send protocol asynchronously
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

                // put at the back of waiting list
                waitingList.put(request, waitingInfo);
            }
        }
        sendDatagram(protocol);
    }


    private void sendDatagram(Protocol protocol) {
        String msg = ProtocolFactory.marshalProtocol(protocol);
        log.info(currentHostPort() + " Message sent: "
                + msg.substring(0, Math.min(MAX_LOG_LEN, msg.length())));
        byte[] buffer = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, hostAddress, hostPort.port);
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            close();
        }
    }

    public void active() {
        synchronized (this) {
            if (isActive) {
                return;
            }
            isActive = true;
            activeSemaphore.release();
        }

        // trigger first sync
        PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                "UDP connection first sync",
                Priority.NORMAL,
                this::firstSync
        ));
    }

    private void retryTimeoutRequest() {
        // note that every thing in waiting list is time ordered
        synchronized (waitingList) {
            while (!waitingList.isEmpty()) {
                // get the first one
                Map.Entry<IRequest, UDPConnection.WaitingInfo> entry = waitingList.entrySet().iterator().next();
                if (entry.getValue().timestamp + UDP_TIMEOUT_MS > System.currentTimeMillis()) {
                    // not timeout yet.
                    return;
                }

                // timeout
                IRequest request = entry.getKey();
                WaitingInfo info = entry.getValue();
                waitingList.remove(request);

                if (!info.doRetry()) {
                    // out of retry count, abort
                    this.close();
                    return;
                }

                this.sendDatagram((Protocol) request);

                // move item to the end of waiting list
                waitingList.put(request, info);
            }
        }
    }


    private void receive(String msg) {
        synchronized (this) {
            if (!isActive) {
                if (type == ConnectionType.OUTGOING) {
                    outgoingConnectionHelper.handleHandshake(this, msg);
                }
                return;
            }
        }

        log.info(currentHostPort() + " Message Received: "
                + msg.substring(0, Math.min(MAX_LOG_LEN, msg.length())));
        MessageHandler.handleMessage(msg, this);
    }

    @Override
    public void close(Boolean allowReconnect) {
        synchronized (this) {
            if (isClosed) {
                return;
            }
            isClosed = true;


            log.info(currentHostPort() + " Connection Closed");

            udpConnectionMap.remove(new HostPort(this.hostAddress.getHostAddress(), hostPort.port), this);
            if (isActive) {
                ConnectionManager.getInstance().removeConnection(this);

                // if it is an outgoing connection, and allowReconnect is true
                // add back to queue for retry this hostPort later
                if (type == ConnectionType.OUTGOING && allowReconnect) {
                    outgoingConnectionHelper.scheduleConnectionTask(hostPort, OutgoingConnectionHelper.RECONNECT_INTERVAL);
                }
            } else {
                activeSemaphore.release();
            }
        }
    }

    private void firstSync() {
        if (this.type == ConnectionType.INCOMING) {
            // for incoming connection, wait a while before the first sync
            // to reduce the chance that sync message received before handshake response
            try {
                Thread.sleep(INCOMING_CONNECTION_FIRST_SYNC_WAIT_TIME);
            } catch (Exception ignored) { }
        }
        SyncManager.getInstance().syncWithOneAsync(this);
    }

    /**
     * Send invalid protocol with additional message, and close the connection
     * @param additionalMsg     message in invalid protocol
     */
    @Override
    public void abortWithInvalidProtocol(String additionalMsg) {
        Protocol.InvalidProtocol invalidProtocol = new Protocol.InvalidProtocol();
        invalidProtocol.msg = additionalMsg;
        sendAsync(invalidProtocol);
        close();
    }


    /**
     * Remove the corresponded request in waiting list
     * @param response received response protocol
     */
    @Override
    public void markRequestAsDone(IResponse response) {
        IRequest request = ProtocolFactory.identifyRes(response);
        synchronized (waitingList) {
            waitingList.remove(request);
        }
    }

    @Override
    public boolean allowInvalidMessage() {
        return true;
    }

    @Override
    public long getBlockSize() {
        return BLOCK_SIZE;
    }

    public boolean isActive() {
        synchronized (this) {
            return isActive;
        }
    }

    protected static class CException extends Exception {
        protected CException(String message) {
            super(message);
        }
    }

    protected static class WaitingInfo {

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
