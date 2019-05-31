package unimelb.bitbox.util.ConnectionUtils.Peer;


import javafx.util.Pair;
import unimelb.bitbox.protocol.InvalidProtocolException;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolType;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * UDPOutgoingConnectionHelper deals with all UDP outgoing connections
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class UDPOutgoingConnectionHelper extends OutgoingConnectionHelper {

    private final DatagramSocket serverSocket;

    /**
     * Constructor
     *
     * @param advertisedName from config
     * @param port           from config
     * @param serverSocket   created in UDPIncomingConnectionHelper
     */
    public UDPOutgoingConnectionHelper(String advertisedName, int port, DatagramSocket serverSocket) {
        super(advertisedName, port);
        this.serverSocket = serverSocket;
    }

    @Override
    protected int getRetryCount() {
        return UDPConnection.MAX_RETRY;
    }

    @Override
    protected long getRetryInterval() {
        return 1000;
    }

    // Boolean: true -> success, false -> fail and shouldn't retry, null -> fail and allow retry
    // String: message
    @Override
    protected Pair<Boolean, String> tryConnectTo(HostPort hostPort) {
        UDPConnection conn = null;
        try {
            conn = new UDPConnection(serverSocket, hostPort,
                    InetAddress.getByName(hostPort.host), this);
            conn.sendAsync(handshakeRequest);

            // wait for async handshake to be finished
            try {
                conn.activeSemaphore.tryAcquire(UDPConnection.UDP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }

            if (conn.handshakeResult != null)
                return conn.handshakeResult;
            return new Pair<>(null, "Unable to establish connection: timeout");

        } catch (IOException | UDPConnection.CException e) {
            return new Pair<>(null, "Unable to establish connection: " + e.getMessage());
        } finally {
            if (conn != null && !conn.isActive()) {
                conn.close();
            }
        }

    }

    // String: message
    protected void handleHandshake(UDPConnection conn, String msg) {

        Protocol protocol;
        ProtocolType protocolType;

        try {
            protocol = ProtocolFactory.parseProtocol(msg);
            protocolType = ProtocolType.typeOfProtocol(protocol);
        } catch (InvalidProtocolException e) {
            conn.handshakeResult = new Pair<>(null, e.getMessage());
            conn.abortWithInvalidProtocol(e.getMessage());
            return;
        }

        switch (protocolType) {
            case HANDSHAKE_RESPONSE:
                Protocol.HandshakeResponse handshakeResponse = (Protocol.HandshakeResponse) protocol;
                HostPort hostPort = handshakeResponse.peer;

                int res = ConnectionManager.getInstance().addConnection(conn, hostPort);
                if (res == 0) {
                    conn.handshakeResult = new Pair<>(true, "Connected");
                    conn.active();
                    return;
                } else {
                    // already exists
                    conn.handshakeResult = new Pair<>(false, "A connection with the same HostPort is already existed");
                    conn.abortWithInvalidProtocol("A connection with the same HostPort is already existed");
                    return;
                }
            case CONNECTION_REFUSED:
                Protocol.ConnectionRefused connectionRefused = (Protocol.ConnectionRefused) protocol;
                conn.handshakeResult = new Pair<>(false, "Connection refused: " + connectionRefused.msg);
                ArrayList<HostPort> hostPorts = connectionRefused.peers;
                conn.close();
                for (HostPort hostPort1 : hostPorts) {
                    this.scheduleConnectionTask(hostPort1, 0);
                }
                return;
            case INVALID_PROTOCOL:
                Protocol.InvalidProtocol invalidProtocol = (Protocol.InvalidProtocol) protocol;
                conn.handshakeResult = new Pair<>(null, "Invalid protocol: " + invalidProtocol.msg);
                conn.close();
                return;
            default:
                // ignore this case for UDP since the order is not guaranteed
                conn.handshakeResult = new Pair<>(null, "Unexpected protocol: " + protocol.getClass().getName());
                return;
        }
    }
}
