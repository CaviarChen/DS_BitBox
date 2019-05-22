package unimelb.bitbox.util.ConnectionUtils.Peer;


import javafx.util.Pair;
import unimelb.bitbox.protocol.InvalidProtocolException;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolType;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.logging.Logger;



/**
 * OutgoingConnectionHelper deal with all outgoing connections
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class TCPOutgoingConnectionHelper extends OutgoingConnectionHelper{

    private static Logger log = Logger.getLogger(TCPOutgoingConnectionHelper.class.getName());

    /**
     * Constructor
     * @param advertisedName from config
     * @param port from config
     */
    public TCPOutgoingConnectionHelper(String advertisedName, int port) {
        super(advertisedName, port);
    }

    @Override
    protected int getRetryCount() {
        return 2;
    }

    @Override
    protected long getRetryInterval() {
        return 1000;
    }


    // Boolean: true -> success, false -> fail and shouldn't retry, null -> fail and allow retry
    // String: message
    @Override
    protected Pair<Boolean, String> tryConnectTo(HostPort hostPort) {
        // try to connect to the peer
        try {
            log.info("Start connecting to peer: %d" + hostPort);

            Socket clientSocket = new Socket(hostPort.host, hostPort.port);
            TCPConnection conn = new TCPConnection(clientSocket, this);
            conn.send(ProtocolFactory.marshalProtocol(handshakeRequest));

            String json;
            try {
                json = conn.waitForOneMessage(HANDSHAKE_TIMEOUT);
            } catch (SocketTimeoutException e) {
                conn.abortWithInvalidProtocol("Handshake response timeout");
                return new Pair<>(null, "Handshake response timeout");
            }

            Protocol protocol;
            ProtocolType protocolType;
            try {
                protocol = ProtocolFactory.parseProtocol(json);
                protocolType = ProtocolType.typeOfProtocol(protocol);
            } catch (InvalidProtocolException e) {
                conn.abortWithInvalidProtocol(e.getMessage());
                return new Pair<>(false, e.getMessage());
            }


            switch (protocolType) {
                case HANDSHAKE_RESPONSE:
                    Protocol.HandshakeResponse handshakeResponse = (Protocol.HandshakeResponse) protocol;

                    int res = ConnectionManager.getInstance().addConnection(conn, hostPort);
                    if (res == 0) {
                        conn.active(handshakeResponse.peer);
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
                    return new Pair<>(false, "Invalid protocol: " + invalidProtocol.msg);
                default:
                    conn.abortWithInvalidProtocol("Unexpected protocol: " + protocol.getClass().getName());
                    return new Pair<>(false, "Unexpected protocol: " + protocol.getClass().getName());
            }

        } catch (IOException e) {
            log.warning(hostPort.toString() + " " + e.toString());
            return new Pair<>(null, "Unable to establish TCP connection");
        }
    }

}
