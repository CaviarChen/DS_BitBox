package unimelb.bitbox.util.ConnectionUtils;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class UDPConnection extends Connection {

//    private static final int listenPort = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));

    private static final ConcurrentHashMap<HostPort, UDPConnection> udpConnectionMap = new ConcurrentHashMap<>();
    private static Logger log = Logger.getLogger(UDPConnection.class.getName());

    private final DatagramSocket serverSocket;
    private final InetAddress hostAddress;

    private boolean isClosed = false;



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
        // TODO: add retry mechanism
        String msg = ProtocolFactory.marshalProtocol(protocol);
        byte[] buffer = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, hostAddress, hostPort.port);
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            close();
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



    }

    public static class CException extends Exception {
        public CException(String message) {
            super(message);
        }
    }

    @Override
    public void abortWithInvalidProtocol(String msg) {

    }
}
