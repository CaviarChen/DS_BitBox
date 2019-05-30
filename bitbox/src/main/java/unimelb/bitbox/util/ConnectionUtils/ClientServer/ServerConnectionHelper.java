package unimelb.bitbox.util.ConnectionUtils.ClientServer;


import javafx.util.Pair;
import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.ClientProtocol;
import unimelb.bitbox.protocol.ClientProtocolFactory;
import unimelb.bitbox.protocol.ClientProtocolType;
import unimelb.bitbox.protocol.InvalidProtocolException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.ConnectionUtils.Peer.OutgoingConnectionHelper;
import unimelb.bitbox.util.PublicKeyNotFoundException;
import unimelb.bitbox.util.SecManager;

import java.net.ServerSocket;
import java.util.logging.Logger;


/**
 * Helper class for server connection
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class ServerConnectionHelper {
    private final OutgoingConnectionHelper outgoingConnectionHelper;
    private Logger log = Logger.getLogger(ServerConnectionHelper.class.getName());
    private Thread thread;
    private ServerSocket serverSocket;
    private String port;


    /**
     * ServerConnectionHelper constructor
     *
     * @param outgoingConnectionHelper the helper for outgoing connection
     * @throws Exception failed to create server socket
     */
    public ServerConnectionHelper(OutgoingConnectionHelper outgoingConnectionHelper) throws Exception {
        thread = null;
        this.outgoingConnectionHelper = outgoingConnectionHelper;
        port = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_CLIENT_PORT);
        serverSocket = new ServerSocket(Integer.parseInt(port));
    }


    /**
     * Start listening client connection
     */
    public void start() {
        if (thread != null) throw new RuntimeException("Already started");

        thread = new Thread(() -> {
            try {
                execute();
            } catch (Exception e) {
                log.severe(e.toString());
            }
        });
        thread.start();
    }


    /**
     * Handle client protocols
     */
    private void execute() {
        log.info("Server start to listen for clients on port " + port);

        ClientConnection clientConnection = null;

        while (true) {
            try {
                clientConnection = new ClientConnection(serverSocket.accept());

                ClientProtocol protocol = clientConnection.receiveProtocol();
                handleAuthRequest(clientConnection, protocol);

                protocol = clientConnection.receiveProtocol();
                ClientProtocolType protocolType = ClientProtocolType.typeOfProtocol(protocol);

                switch (protocolType) {
                    case LIST_PEERS_REQUEST:
                        handleListPeerRequest(clientConnection, protocol);
                        break;
                    case CONNECT_PEER_REQUEST:
                        handleConnectPeerRequest(clientConnection, protocol);
                        break;
                    case DISCONNECT_PEER_REQUEST:
                        handleDisconnectPeerRequest(clientConnection, protocol);
                        break;
                    default:
                        throw new InvalidProtocolException("Unexpected protocol:" + protocolType.toString(), null);
                }

                clientConnection.close();

            } catch (Exception e) {
                log.severe(e.toString());
                continue;
            }

            SecManager.getInstance().removeAES();
        }
    }


    private void handleAuthRequest(ClientConnection clientConnection, ClientProtocol protocol) throws Exception {
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.AUTH_REQUEST);
        ClientProtocol.AuthRequest authRequest = (ClientProtocol.AuthRequest) protocol;

        // send auth response
        ClientProtocol.AuthResponse authResponse = null;
        try {
            authResponse = new ClientProtocol.AuthResponse();
            authResponse.authKey.key = SecManager.getInstance().encryptAESWithRSA(authRequest.authIdentity.identity);
            authResponse.response.status = true;
            authResponse.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_PUBLIC_KEY_FOUND;
        } catch (PublicKeyNotFoundException e) {
            authResponse.response.status = false;
            authResponse.response.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_PUBLIC_KEY_NOT_FOUND;
        }

        clientConnection.send(authResponse);
    }


    private void handleListPeerRequest(ClientConnection clientConnection, ClientProtocol protocol) throws Exception {
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.LIST_PEERS_REQUEST);

        // construct response
        ClientProtocol.ListPeersResponse listPeersResponse = new ClientProtocol.ListPeersResponse();
        listPeersResponse.peers.peers = ConnectionManager.getInstance().getConnectedPeers();

        clientConnection.send(listPeersResponse);
    }


    private void handleConnectPeerRequest(ClientConnection clientConnection, ClientProtocol protocol) throws Exception {
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.CONNECT_PEER_REQUEST);
        ClientProtocol.ConnectPeerRequest connectPeerRequest = (ClientProtocol.ConnectPeerRequest) protocol;

        Pair<Boolean, String> result = outgoingConnectionHelper.connectTo(connectPeerRequest.hostPort);

        // construct response
        ClientProtocol.ConnectPeerResponse connectPeerResponse = new ClientProtocol.ConnectPeerResponse();
        connectPeerResponse.hostPort = connectPeerRequest.hostPort;
        connectPeerResponse.response.status = result.getKey();
        connectPeerResponse.response.msg = result.getValue();

        clientConnection.send(connectPeerResponse);
    }


    private void handleDisconnectPeerRequest(ClientConnection clientConnection, ClientProtocol protocol) throws Exception {
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.DISCONNECT_PEER_REQUEST);
        ClientProtocol.DisconnectPeerRequest disconnectPeerRequest = (ClientProtocol.DisconnectPeerRequest) protocol;

        // try to disconnect
        ClientProtocol.DisconnectPeerResponse disconnectPeerResponse = new ClientProtocol.DisconnectPeerResponse();
        Pair<Boolean, String> result = ConnectionManager.getInstance().disconnectFrom(disconnectPeerRequest.hostPort);

        // construct response
        disconnectPeerResponse.hostPort = disconnectPeerRequest.hostPort;
        disconnectPeerResponse.response.status = result.getKey();
        disconnectPeerResponse.response.msg = result.getValue();

        clientConnection.send(disconnectPeerResponse);
    }

}
