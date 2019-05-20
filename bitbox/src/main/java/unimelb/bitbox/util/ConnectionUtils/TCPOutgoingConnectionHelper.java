package unimelb.bitbox.util.ConnectionUtils;


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

import static java.lang.Thread.sleep;


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
    public Pair<Boolean, String> connectTo(HostPort hostPort) {
        return null;
    }


    // request handshake to the peer
    private void requestHandshake(TCPConnection conn) {
//        conn.send(handshakeRequestJson);
//
//        String json;
//        try {
//            json = conn.waitForOneMessage(HANDSHAKE_TIMEOUT);
//        } catch (SocketTimeoutException e) {
//            conn.abortWithInvalidProtocol("Handshake response timeout");
//            return;
//        }
//
//        Protocol protocol;
//        ProtocolType protocolType;
//        try {
//            protocol = ProtocolFactory.parseProtocol(json);
//            protocolType = ProtocolType.typeOfProtocol(protocol);
//        } catch (InvalidProtocolException e) {
//            conn.abortWithInvalidProtocol(e.getMessage());
//            return;
//        }
//
//        switch (protocolType) {
//            case HANDSHAKE_RESPONSE:
//                Protocol.HandshakeResponse handshakeResponse = (Protocol.HandshakeResponse) protocol;
//                HostPort hostPort = handshakeResponse.peer;
//
//                int res = ConnectionManager.getInstance().addConnection(conn, hostPort);
//                if (res == 0) {
//                    conn.active(hostPort);
//                    return;
//                } else {
//                    // already exists
//                    conn.abortWithInvalidProtocol("HostPort is already existed");
//                }
//                break;
//            case CONNECTION_REFUSED:
//                Protocol.ConnectionRefused connectionRefused = (Protocol.ConnectionRefused) protocol;
//                ArrayList<HostPort> hostPorts = connectionRefused.peers;
//                for (HostPort hp : hostPorts) {
//                    addPeerInfo(new PeerInfo(hp));
//                }
//                conn.close(true);
//                break;
//            case INVALID_PROTOCOL:
//                conn.close(true);
//                break;
//            default:
//                conn.abortWithInvalidProtocol("Unexpected protocol: " + protocol.getClass().getName());
//        }
    }
}
