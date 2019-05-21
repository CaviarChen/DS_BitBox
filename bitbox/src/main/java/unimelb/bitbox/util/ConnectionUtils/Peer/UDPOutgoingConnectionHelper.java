package unimelb.bitbox.util.ConnectionUtils.Peer;


import unimelb.bitbox.protocol.*;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class UDPOutgoingConnectionHelper extends OutgoingConnectionHelper {

    private final int port;

    private static Logger log = Logger.getLogger(UDPOutgoingConnectionHelper.class.getName());

    public UDPOutgoingConnectionHelper(String advertisedName, int port) {
        super(advertisedName, port);
        this.port = port;
    }

    @Override
    protected void execute() throws Exception {

        while (true) {
            PeerInfo peer = null;

            synchronized (queue) {
                if (queue.peek() != null && queue.peek().getTime() <= System.currentTimeMillis()) {
                    peer = queue.poll();
                }
            }

            if (peer != null) {
                // try to connect ot the peer
                try {
                    DatagramSocket serverSocket = null;

                    UDPConnection conn = new UDPConnection(serverSocket, peer.getHostPort(),
                            InetAddress.getByName(peer.getHostPort().host), this);

                    log.info(String.format("Start connecting to port: %d", peer.getPort()));
                    conn.sendAsync(handshakeRequest);

                } catch (IOException e) {
                    log.warning(peer.getHostPort().toString() + " " + e.toString());
                    peer.setPenaltyTime();
                    addPeerInfo(peer);
                }
            } else {
                // sleep 10 seconds if there is no job
                try {
                    sleep(CHECK_INTERVAL);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    protected void handleHandshake(UDPConnection conn, String msg) {

        Protocol protocol;
        ProtocolType protocolType;

        try {
            protocol = ProtocolFactory.parseProtocol(msg);
            protocolType = ProtocolType.typeOfProtocol(protocol);
        } catch (InvalidProtocolException e) {
            conn.abortWithInvalidProtocol(e.getMessage());
            return;
        }

        switch (protocolType) {
            case HANDSHAKE_RESPONSE:
                Protocol.HandshakeResponse handshakeResponse = (Protocol.HandshakeResponse) protocol;
                HostPort hostPort = handshakeResponse.peer;

                int res = ConnectionManager.getInstance().addConnection(conn, hostPort);
                if (res == 0) {
                    conn.active();
                } else {
                    // already exists
                    conn.abortWithInvalidProtocol("HostPort is already existed");
                }
                break;
            case CONNECTION_REFUSED:
                Protocol.ConnectionRefused connectionRefused = (Protocol.ConnectionRefused) protocol;
                ArrayList<HostPort> hostPorts = connectionRefused.peers;
                for (HostPort hp : hostPorts) {
                    addPeerInfo(new PeerInfo(hp));
                }
                conn.close(true);
                break;
            case INVALID_PROTOCOL:
                conn.close(true);
                break;
            default:
                conn.abortWithInvalidProtocol("Unexpected protocol: " + protocol.getClass().getName());
        }
    }
}
