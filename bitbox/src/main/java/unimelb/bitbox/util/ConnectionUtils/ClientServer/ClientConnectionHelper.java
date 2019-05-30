package unimelb.bitbox.util.ConnectionUtils.ClientServer;


import unimelb.bitbox.protocol.ClientProtocol;
import unimelb.bitbox.protocol.ClientProtocolFactory;
import unimelb.bitbox.protocol.ClientProtocolType;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.SecManager;

import java.util.ArrayList;


/**
 * Helper class for client connection
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class ClientConnectionHelper {

    private ClientConnection clientConnection;


    /**
     * ClientConnectionHelper constructor
     *
     * @param clientConnection related clientConnection
     */
    public ClientConnectionHelper(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }


    /**
     * Handle Disconnect Peer Protocol
     *
     * @param peer the peer wanted to be disconnected from
     * @throws Exception failed to disconnect
     */
    public void handleDisConnectPeer(String peer) throws Exception {
        ClientProtocol.DisconnectPeerRequest disconnectPeerRequest = new ClientProtocol.DisconnectPeerRequest();
        disconnectPeerRequest.hostPort = new HostPort(peer);
        clientConnection.send(disconnectPeerRequest);
        System.out.println("Disconnecting...");

        ClientProtocol protocol = clientConnection.receiveProtocol();
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.DISCONNECT_PEER_RESPONSE);
        ClientProtocol.DisconnectPeerResponse disconnectPeerResponse = (ClientProtocol.DisconnectPeerResponse) protocol;

        if (disconnectPeerResponse.response.status) {
            System.out.println("Successfully disconnected from " + disconnectPeerResponse.hostPort.toString());
        } else {
            System.out.println("Failed to disconnect from " + disconnectPeerResponse.hostPort.toString() +
                    "\n" +
                    disconnectPeerResponse.response.msg);
        }
    }


    /**
     * Handle Connect Peer Protocol
     *
     * @param peer the peer wanted to be connected to
     * @throws Exception failed to connect
     */
    public void handleConnectPeer(String peer) throws Exception {
        ClientProtocol.ConnectPeerRequest connectPeerRequest = new ClientProtocol.ConnectPeerRequest();
        connectPeerRequest.hostPort = new HostPort(peer);
        clientConnection.send(connectPeerRequest);
        System.out.println("Connecting...");

        ClientProtocol protocol = clientConnection.receiveProtocol();
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.CONNECT_PEER_RESPONSE);
        ClientProtocol.ConnectPeerResponse connectPeerResponse = (ClientProtocol.ConnectPeerResponse) protocol;

        if (connectPeerResponse.response.status) {
            System.out.println("Successfully connected to " + connectPeerResponse.hostPort.toString());
        } else {
            System.out.println("Failed to connect to " + connectPeerResponse.hostPort.toString() +
                    "\n" +
                    connectPeerResponse.response.msg);
        }
    }


    /**
     * Handle List Peer protocol
     *
     * @throws Exception failed to find a list of connected peers
     */
    public void handleListPeer() throws Exception {
        ClientProtocol.ListPeersRequest listPeersRequest = new ClientProtocol.ListPeersRequest();
        clientConnection.send(listPeersRequest);

        ClientProtocol protocol = clientConnection.receiveProtocol();
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.LIST_PEERS_RESPONSE);
        ClientProtocol.ListPeersResponse listPeersResponse = (ClientProtocol.ListPeersResponse) protocol;

        ArrayList<HostPort> peerHostPorts = listPeersResponse.peers.peers;
        if (peerHostPorts.size() > 0) {
            System.out.println("Connected peers:");
            for (HostPort peer : listPeersResponse.peers.peers) {
                System.out.println("  - " + peer.toString());
            }
        } else {
            System.out.println("The server does not connect to any peer");
        }
    }


    /**
     * Handle challenge request and response
     *
     * @throws Exception failed to authorize
     */
    public void handleAuth() throws Exception {
        // send request
        String identity = SecManager.getInstance().getPrivateIdentity();
        ClientProtocol.AuthRequest authReq = new ClientProtocol.AuthRequest();
        authReq.authIdentity.identity = identity;
        clientConnection.send(authReq);

        // receive
        ClientProtocol protocol = clientConnection.receiveProtocol();
        ClientProtocolFactory.validateProtocolType(protocol, ClientProtocolType.AUTH_RESPONSE);
        ClientProtocol.AuthResponse authResponse = (ClientProtocol.AuthResponse) protocol;

        if (authResponse.response.status) {
            SecManager.getInstance().decryptAESWithRSA(authResponse.authKey.key);
        } else {
            throw new Exception("Failed to be authorized");
        }
    }

}
