package unimelb.bitbox.util.ConnectionUtils;

import javafx.util.Pair;
import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public abstract class OutgoingConnectionHelper {

    private static final int CHECK_INTERVAL = 10000;
    protected static final int HANDSHAKE_TIMEOUT = 10000;
    // 4 mins
    protected static final long RECONNECT_INTERVAL = 4 * 60 * 1000;

    private static Logger log = Logger.getLogger(OutgoingConnectionHelper.class.getName());

    protected Protocol.HandshakeRequest handshakeRequest;
    private final PriorityQueue<ConnectionTask> queue;

    public OutgoingConnectionHelper(String advertisedName, int port) {
        handshakeRequest = new Protocol.HandshakeRequest();
        handshakeRequest.peer.host = advertisedName;
        handshakeRequest.peer.port = port;

        queue = new PriorityQueue<>(Comparator.comparingLong(ConnectionTask::getExecutionTime));

        String[] peers = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PEERS)
                                                        .split(Constants.CONFIG_PEERS_SEPARATOR);

        // add all init peers to the queue
        for (String peer : peers) {
            if (!peer.isEmpty()) {
                scheduleConnectionTask(new HostPort(peer), 0);
            }
        }
    }

    /**
     * start working (blocking)
     */
    public void execute() {

        while (true) {

            ConnectionTask peer = null;

            synchronized (queue) {
                if (queue.peek() != null && queue.peek().getExecutionTime() <= System.currentTimeMillis()) {
                    peer = queue.poll();
                }
            }

            if (peer != null) {
                    connectTo(peer.hostPort);
//                // try to connect to the peer
//                try {
//                    Socket clientSocket = new Socket(peer.getHost(), peer.getPort());
//                    TCPConnection conn = new TCPConnection(clientSocket, this);
//                    log.info(String.format("Start connecting to port: %d", peer.getPort()));
//                    requestHandshake(conn);
//                } catch (IOException e) {
//                    log.warning(peer.getHostPort().toString() + " " + e.toString());
//                    peer.setPenaltyTime();
//                    addPeerInfo(peer);
//                }
            } else {
                // sleep 10 seconds if there is no job
                try {
                    sleep(CHECK_INTERVAL);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public abstract Pair<Boolean, String> connectTo(HostPort hostPort);


    public void scheduleConnectionTask(HostPort hostPort, long executionDelayTime) {
        synchronized (queue) {
            queue.add(new ConnectionTask(hostPort, System.currentTimeMillis() +  executionDelayTime));
        }
    }

    /**
     * internal class for a peer that waiting to be connected and it's retry info
     */
     private class ConnectionTask {

        private HostPort hostPort;
        private long executionTime;

        ConnectionTask(HostPort hostPort, long executionTime) {
            this.hostPort = hostPort;
            this.executionTime = executionTime;
        }

        HostPort getHostPort() {
            return hostPort;
        }

        long getExecutionTime() {
            return executionTime;
        }
    }
}
