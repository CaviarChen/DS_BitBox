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

    public void handle() throws Exception {
        handleAuth();

        sendListPeerRequest();
    }


    private void sendListPeerRequest() throws Exception {
        //send
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


    private void handleAuth() throws Exception {
        // send
        String identity = SecManager.getInstance().getPrivateIdentity();
        ClientProtocol.AuthRequest authReq = new ClientProtocol.AuthRequest();
        authReq.authIdentity.identity = identity;
        clientConnection.send(authReq, false);

        // recv
        ClientProtocol.AuthResponse authResponse =
                (ClientProtocol.AuthResponse) clientConnection.getMsgProtocolType(ClientProtocolType.AUTH_RESPONSE);

        if (authResponse.response.status) {
            SecManager.getInstance().decryptAESWithRSA(authResponse.authKey.key);
        } else {
            throw new Exception("Failed to be authorized");
        }
    }

}
