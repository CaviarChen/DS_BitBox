package unimelb.bitbox.util.ConnectionUtils.ClientServer;


import unimelb.bitbox.protocol.ClientProtocol;
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

        String msg = clientConnection.receive();
        ClientProtocol.ConnectPeerResponse connectPeerResponse =
                (ClientProtocol.ConnectPeerResponse) clientConnection.validateProtocolType(
                        ClientProtocolType.CONNECT_PEER_RESPONSE, msg);
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

        String msg = clientConnection.receive();
        ClientProtocol.ListPeersResponse listPeersResponse =
                (ClientProtocol.ListPeersResponse) clientConnection.validateProtocolType(
                        ClientProtocolType.LIST_PEERS_RESPONSE, msg);

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

        String msg = clientConnection.receive();
        ClientProtocol.AuthResponse authResponse =
                (ClientProtocol.AuthResponse) clientConnection.validateProtocolType(ClientProtocolType.AUTH_RESPONSE, msg);

        if (authResponse.response.status) {
            SecManager.getInstance().decryptAESWithRSA(authResponse.authKey.key);
        } else {
            throw new Exception("Failed to be authorized");
        }
    }

}
