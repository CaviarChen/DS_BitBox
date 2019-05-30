package unimelb.bitbox.util.ConnectionUtils.Peer;

import javafx.util.Pair;
import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.HostPort;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

/**
 * OutgoingConnectionHelper is an abstract class for TCP and UDP
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */

public abstract class OutgoingConnectionHelper {

    private static final int CHECK_INTERVAL = 10000;
    protected static final int HANDSHAKE_TIMEOUT = 10000;
    // 4 minutes
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
            } else {
                // sleep 10 seconds if there is no job
                try {
                    sleep(CHECK_INTERVAL);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    protected abstract int getRetryCount();

    protected abstract long getRetryInterval();

    public Pair<Boolean, String> connectTo(HostPort hostPort) {

        if (ConnectionManager.getInstance().checkExist(hostPort)) {
            return new Pair<>(false, "An active connection with the target peer already exists.");
        }

        int retryCount = 0;
        while (true) {
            Pair<Boolean, String> res = tryConnectTo(hostPort);

            log.info("Connection result: " + res.toString());

            if (res.getKey() != null) {
                return res;
            } else {
                retryCount += 1;
                if (retryCount >= getRetryCount()) {
                    // stop retry
                    return new Pair<>(false, res.getValue());
                }

                try {
                    Thread.sleep(getRetryInterval());
                } catch (Exception ignored) {}
            }

        }
    }

    // Boolean: true -> success, false -> fail and shouldn't retry, null -> fail and allow retry
    // String: message
    protected abstract Pair<Boolean, String> tryConnectTo(HostPort hostPort);


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
