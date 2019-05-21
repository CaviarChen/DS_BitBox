package unimelb.bitbox.util.ConnectionUtils.Peer;

import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public abstract class OutgoingConnectionHelper {

    protected static final int CHECK_INTERVAL = 10000;
    protected static final int HANDSHAKE_TIMEOUT = 10000;

    private static Logger log = Logger.getLogger(OutgoingConnectionHelper.class.getName());

    protected String handshakeRequestJson;
    protected Protocol.HandshakeRequest handshakeRequest;
    protected final PriorityQueue<PeerInfo> queue;

    public OutgoingConnectionHelper(String advertisedName, int port) {
        handshakeRequest = new Protocol.HandshakeRequest();
        handshakeRequest.peer.host = advertisedName;
        handshakeRequest.peer.port = port;
        handshakeRequestJson = ProtocolFactory.marshalProtocol(handshakeRequest);

        queue = new PriorityQueue<>(Comparator.comparingLong(PeerInfo::getTime));

        String[] peers = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PEERS).split(Constants.CONFIG_PEERS_SEPARATOR);

        // add all init peers to the queue
        for (String peer : peers) {
            if (!peer.isEmpty()) {
                addPeerInfo(new HostPort(peer));
            }
        }
    }

    protected abstract void execute() throws Exception;

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


    /**
     * internal class for a peer that waiting to be connected and it's retry info
     */
    class PeerInfo {

        private static final int PENALTY_TIME = 60000;
        private HostPort hostPort;
        private long time;
        private int penaltyMin;


        PeerInfo(HostPort hostPort) {
            this.hostPort = hostPort;
            this.time = System.currentTimeMillis();
            this.penaltyMin = 1;
        }


        HostPort getHostPort() {
            return hostPort;
        }

        int getPort() {
            return hostPort.port;
        }


        long getTime() {
            return time;
        }


        private void setTime(long time) {
            this.time = time;
        }


        void setPenaltyTime() {
            this.penaltyMin *= 2;
            if (penaltyMin > 60) {
                penaltyMin = 60;
            }
            setTime(System.currentTimeMillis() + penaltyMin * PENALTY_TIME);
        }


        String getHost() {
            return hostPort.host;
        }
    }
}
