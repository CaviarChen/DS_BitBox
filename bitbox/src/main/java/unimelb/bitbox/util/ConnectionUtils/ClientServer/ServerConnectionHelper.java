package unimelb.bitbox.util.ConnectionUtils.ClientServer;


import javafx.util.Pair;
import unimelb.bitbox.Client;
import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.ClientProtocol;
import unimelb.bitbox.protocol.ClientProtocolType;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.ConnectionUtils.Peer.OutgoingConnectionHelper;
import unimelb.bitbox.util.PublicKeyNotFoundException;
import unimelb.bitbox.util.SecManager;

import java.net.ServerSocket;
import java.util.Objects;
import java.util.logging.Logger;


public class ServerConnectionHelper {
    private final OutgoingConnectionHelper outgoingConnectionHelper;
    private Logger log = Logger.getLogger(ServerConnectionHelper.class.getName());
    private Thread thread;
    private ServerSocket serverSocket;
    private String port;


    public ServerConnectionHelper(OutgoingConnectionHelper outgoingConnectionHelper) throws Exception {
        thread = null;
        this.outgoingConnectionHelper = outgoingConnectionHelper;
        port = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_CLIENT_PORT);
        serverSocket = new ServerSocket(Integer.parseInt(port));
    }


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


    private void execute() {
        log.info("Server start to listen for clients on port " + port);

        ClientConnection clientConnection = null;

        while (true) {
            try {
                clientConnection = new ClientConnection(serverSocket.accept());

                handleAuthRequest(clientConnection);

                // TODO: Switch
                handleListPeerRequest(clientConnection);

                handleConnectPeerRequest(clientConnection);

                handleDisconnectPeerRequest(clientConnection);

            } catch (Exception e) {
                log.severe(e.toString());
                continue;
            }

            SecManager.removeAES();
        }
    }


    private void handleAuthRequest(ClientConnection clientConnection) throws Exception {
        ClientProtocol.AuthRequest authRequest =
                (ClientProtocol.AuthRequest) clientConnection.getMsgProtocolType(
                        ClientProtocolType.AUTH_REQUEST);

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

        clientConnection.send(authResponse, false);
    }


    private void handleListPeerRequest(ClientConnection clientConnection) throws Exception {
        ClientProtocol.ListPeersRequest listPeersRequest =
                (ClientProtocol.ListPeersRequest) clientConnection.getMsgProtocolType(
                        ClientProtocolType.LIST_PEERS_REQUEST);

        ClientProtocol.ListPeersResponse listPeersResponse = new ClientProtocol.ListPeersResponse();
        listPeersResponse.peers.peers = ConnectionManager.getInstance().getConnectedPeers();

        clientConnection.send(listPeersResponse, true);
    }


    private void handleConnectPeerRequest(ClientConnection clientConnection) throws Exception {
        ClientProtocol.ConnectPeerRequest connectPeerRequest =
                (ClientProtocol.ConnectPeerRequest) clientConnection.getMsgProtocolType(
                        ClientProtocolType.CONNECT_PEER_REQUEST);

        Pair<Boolean, String> result = outgoingConnectionHelper.connectTo(connectPeerRequest.hostPort);

        ClientProtocol.ConnectPeerResponse connectPeerResponse = new ClientProtocol.ConnectPeerResponse();
        connectPeerResponse.response.status = result.getKey();
        connectPeerResponse.response.msg = result.getValue();
        clientConnection.send(connectPeerResponse, true);
    }


    private void handleDisconnectPeerRequest(ClientConnection clientConnection) throws Exception {
        ClientProtocol.DisconnectPeerRequest disconnectPeerRequest =
                (ClientProtocol.DisconnectPeerRequest) clientConnection.getMsgProtocolType(
                        ClientProtocolType.DISCONNECT_PEER_REQUEST);

        ClientProtocol.DisconnectPeerResponse disconnectPeerResponse = new ClientProtocol.DisconnectPeerResponse();
        Pair<Boolean, String> result = ConnectionManager.getInstance().disconnectFrom(disconnectPeerRequest.hostPort);
        disconnectPeerResponse.response.status = result.getKey();
        disconnectPeerResponse.response.msg = result.getValue();
        clientConnection.send(disconnectPeerResponse, true);
    }

}
