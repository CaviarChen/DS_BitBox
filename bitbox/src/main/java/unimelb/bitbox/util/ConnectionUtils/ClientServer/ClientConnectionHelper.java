package unimelb.bitbox.util.ConnectionUtils.ClientServer;


import unimelb.bitbox.Client;
import unimelb.bitbox.protocol.ClientProtocol;
import unimelb.bitbox.protocol.ClientProtocolFactory;
import unimelb.bitbox.protocol.ClientProtocolType;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.SecManager;


public class ClientConnectionHelper {

    private ClientConnection clientConnection;

    public ClientConnectionHelper(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    public void handleDisConnectPeer(String peer) throws Exception {
        ClientProtocol.DisconnectPeerRequest disconnectPeerRequest = new ClientProtocol.DisconnectPeerRequest();
        disconnectPeerRequest.hostPort = new HostPort(peer);
        clientConnection.send(disconnectPeerRequest, true);
    }


    public void handleConnectPeer(String peer) throws Exception {
        ClientProtocol.ConnectPeerRequest connectPeerRequest = new ClientProtocol.ConnectPeerRequest();
        connectPeerRequest.hostPort = new HostPort(peer);
        clientConnection.send(connectPeerRequest, true);

        ClientProtocol.ConnectPeerResponse connectPeerResponse =
                (ClientProtocol.ConnectPeerResponse) clientConnection.getMsgProtocolType(
                        ClientProtocolType.CONNECT_PEER_RESPONSE);
        if (connectPeerResponse.response.status) {
            System.out.println("Successfully connected to " + connectPeerResponse.hostPort.toString());
        } else {
            System.out.println("Failed to connect: " + connectPeerResponse.hostPort.toString()
                    + "\n" +
                    connectPeerResponse.response.status);
        }
    }

    public void handleListPeer() throws Exception {
        ClientProtocol.ListPeersRequest listPeersRequest = new ClientProtocol.ListPeersRequest();
        clientConnection.send(listPeersRequest, true);

        ClientProtocol.ListPeersResponse listPeersResponse =
                (ClientProtocol.ListPeersResponse) clientConnection.getMsgProtocolType(
                        ClientProtocolType.LIST_PEERS_RESPONSE);

        System.out.println("Connected peers:");
        for (HostPort peer : listPeersResponse.peers.peers) {
            System.out.println(peer.toString());
        }
    }


    public void handleAuth() throws Exception {
        String identity = SecManager.getPrivateIdentity();
        ClientProtocol.AuthRequest authReq = new ClientProtocol.AuthRequest();
        authReq.authIdentity.identity = identity;
        clientConnection.send(authReq, false);

        ClientProtocol.AuthResponse authResponse =
                (ClientProtocol.AuthResponse) clientConnection.getMsgProtocolType(ClientProtocolType.AUTH_RESPONSE);

        if (authResponse.response.status) {
            SecManager.getInstance().decryptAESWithRSA(authResponse.authKey.key);
        } else {
            throw new Exception("Failed to be authorized");
        }
    }

}
