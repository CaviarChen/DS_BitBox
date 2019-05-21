package unimelb.bitbox.util.ConnectionUtils;


import javafx.util.Pair;
import unimelb.bitbox.protocol.*;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class UDPOutgoingConnectionHelper extends OutgoingConnectionHelper {

//    private static Logger log = Logger.getLogger(UDPOutgoingConnectionHelper.class.getName());

    private final DatagramSocket serverSocket;

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

    // Boolean: true -> success, false -> fail and shouldn't retry, null -> fail and allow retry
    // String: message
    protected Pair<Boolean, String> handleHandshake(UDPConnection conn, String msg) {

        Protocol protocol;
        ProtocolType protocolType;

        try {
            protocol = ProtocolFactory.parseProtocol(msg);
            protocolType = ProtocolType.typeOfProtocol(protocol);
        } catch (InvalidProtocolException e) {
            conn.abortWithInvalidProtocol(e.getMessage());
            return new Pair<>(null, e.getMessage());
        }

        switch (protocolType) {
            case HANDSHAKE_RESPONSE:
                Protocol.HandshakeResponse handshakeResponse = (Protocol.HandshakeResponse) protocol;
                HostPort hostPort = handshakeResponse.peer;

                int res = ConnectionManager.getInstance().addConnection(conn, hostPort);
                if (res == 0) {
                    conn.active();
                    return new Pair<>(true, "Connected");
                } else {
                    // already exists
                    conn.abortWithInvalidProtocol("A connection with the same HostPort is already existed");
                    return new Pair<>(false, "A connection with the same HostPort is already existed");
                }
            case CONNECTION_REFUSED:
                Protocol.ConnectionRefused connectionRefused = (Protocol.ConnectionRefused) protocol;
                ArrayList<HostPort> hostPorts = connectionRefused.peers;
                conn.close();
                for (HostPort hostPort1: hostPorts) {
                    this.scheduleConnectionTask(hostPort1, 0);
                }
                return new Pair<>(false, "Connection refused: " + connectionRefused.msg);
            case INVALID_PROTOCOL:
                Protocol.InvalidProtocol invalidProtocol = (Protocol.InvalidProtocol) protocol;
                conn.close();
                return new Pair<>(null, "Invalid protocol: " + invalidProtocol.msg);
            default:
                conn.abortWithInvalidProtocol("Unexpected protocol: " + protocol.getClass().getName());
                return new Pair<>(null, "Unexpected protocol: " + protocol.getClass().getName());
        }
    }
}
