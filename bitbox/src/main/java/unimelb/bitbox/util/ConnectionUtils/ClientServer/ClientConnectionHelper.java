package unimelb.bitbox.util.ConnectionUtils.ClientServer;


import unimelb.bitbox.Client;
import unimelb.bitbox.protocol.*;
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
        clientConnection.send(disconnectPeerRequest);
    }


    public void handleConnectPeer(String peer) throws Exception {
        ClientProtocol.ConnectPeerRequest connectPeerRequest = new ClientProtocol.ConnectPeerRequest();
        connectPeerRequest.hostPort = new HostPort(peer);
        clientConnection.send(connectPeerRequest);

        ClientProtocol protocol = clientConnection.receviceProtocol();
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.CONNECT_PEER_RESPONSE);
        ClientProtocol.ConnectPeerResponse connectPeerResponse = (ClientProtocol.ConnectPeerResponse) protocol;

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
        clientConnection.send(listPeersRequest);

        ClientProtocol protocol = clientConnection.receviceProtocol();
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.LIST_PEERS_RESPONSE);
        ClientProtocol.ListPeersResponse listPeersResponse = (ClientProtocol.ListPeersResponse) protocol;

        System.out.println("Connected peers:");
        for (HostPort peer : listPeersResponse.peers.peers) {
            System.out.println(peer.toString());
        }
    }


    public void handleAuth() throws Exception {
        String identity = SecManager.getInstance().getPrivateIdentity();
        ClientProtocol.AuthRequest authReq = new ClientProtocol.AuthRequest();
        authReq.authIdentity.identity = identity;
        clientConnection.send(authReq);

        ClientProtocol protocol = clientConnection.receviceProtocol();
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.AUTH_RESPONSE);
        ClientProtocol.AuthResponse authResponse = (ClientProtocol.AuthResponse) protocol;

        if (authResponse.response.status) {
            SecManager.getInstance().decryptAESWithRSA(authResponse.authKey.key);
        } else {
            throw new Exception("Failed to be authorized");
        }
    }

}
