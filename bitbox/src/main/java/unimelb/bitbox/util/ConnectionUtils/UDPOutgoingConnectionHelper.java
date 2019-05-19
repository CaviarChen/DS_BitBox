package unimelb.bitbox.util.ConnectionUtils;


import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
                            InetAddress.getByName(peer.getHostPort().host), peer.getPort());

                    log.info(String.format("Start connecting to port: %d", peer.getPort()));
                    requestHandshake(conn);

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

    private void requestHandshake(UDPConnection conn) {
        conn.sendAsync(handshakeRequest);

    }

    /**
     * add a host&port to the queue for connecting
     * @param hostPort
     */
    public void addPeerInfo(HostPort hostPort) {
        log.info("New target: " + hostPort.toString());
        synchronized (queue) {
            queue.add(new PeerInfo(hostPort));
        }
    }


    /**
     * add back a peerInfo to the queue for connecting
     * @param peerInfo
     */
    public void addPeerInfo(PeerInfo peerInfo) {
        synchronized (queue) {
            queue.add(peerInfo);
        }
    }

}
