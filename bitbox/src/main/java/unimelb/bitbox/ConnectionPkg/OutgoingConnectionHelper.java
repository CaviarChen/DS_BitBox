package unimelb.bitbox.ConnectionPkg;


import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.InvalidProtocolException;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolType;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;


public class OutgoingConnectionHelper {

    private static final int PENALTY_TIME = 60000;
    private static final int CHECK_INTERVAL = 10000;
    private static final int HANDSHAKE_TIMEOUT = 20000;
    private static Logger log = Logger.getLogger(OutgoingConnectionHelper.class.getName());

    private String handshakeRequestJson;
    private final PriorityQueue<PeerInfo> queue;


    public OutgoingConnectionHelper(String advertisedName, int port) {

        Protocol.HandshakeRequest handshakeRequest = new Protocol.HandshakeRequest();
        handshakeRequest.peer.host = advertisedName;
        handshakeRequest.peer.port = port;
        handshakeRequestJson = ProtocolFactory.marshalProtocol(handshakeRequest);

        queue = new PriorityQueue<>(Comparator.comparingLong(PeerInfo::getTime));

        String[] peers = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PEERS).split(Constants.CONFIG_PEERS_SEPARATOR);

        for (String peer : peers) {
            if (!peer.isEmpty()) {
                addPeerInfo(new PeerInfo(peer));
            }
        }
    }


    public void execute() {

        while (true) {

            PeerInfo peer = null;

            synchronized (queue) {
                if (queue.peek() != null && queue.peek().getTime() <= System.currentTimeMillis()) {
                    peer = queue.poll();
                }
            }

            if (peer != null) {
                try {
                    Socket clientSocket = new Socket(peer.getHost(), peer.getPort());
                    Connection conn = new Connection(clientSocket, this);
                    log.info(String.format("Start connecting to port: %d", peer.getPort()));
                    requestHandshake(conn);
                } catch (IOException e) {
                    log.warning(e.toString());
                    peer.setPenaltyTime();
                    addPeerInfo(peer);
                }
            } else {
                // sleep 10 seconds
                try {
                    sleep(CHECK_INTERVAL);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }


    private void requestHandshake(Connection conn) {
        conn.send(handshakeRequestJson);

        String json;
        try {
            json = conn.waitForOneMessage(HANDSHAKE_TIMEOUT);
        } catch (SocketTimeoutException e) {
            conn.abortWithInvalidProtocol("Handshake response timeout");
            return;
        }

        Protocol protocol;
        ProtocolType protocolType;
        try {
            protocol = ProtocolFactory.parseProtocol(json);
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
                    conn.active(hostPort);
                    return;
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
                conn.close();
                break;
            case INVALID_PROTOCOL:
                conn.close();
                break;
            default:
                conn.abortWithInvalidProtocol("Unexpected protocol: " + protocol.getClass().getName());
        }
    }


    public void addPeerInfo(HostPort hostPort) {
        synchronized (queue) {
            queue.add(new PeerInfo(hostPort));
        }
    }


    public void addPeerInfo(PeerInfo peerInfo) {
        synchronized (queue) {
            queue.add(peerInfo);
        }
    }


    private class PeerInfo {

        private HostPort hostPort;
        private long time;
        private int penaltyMin;


        PeerInfo(HostPort hostPort) {
            this.hostPort = hostPort;
            this.time = System.currentTimeMillis();
            this.penaltyMin = 1;
        }


        PeerInfo(String hostPort) {
            this.hostPort = new HostPort(hostPort);
            this.time = System.currentTimeMillis();
            this.penaltyMin = 1;
        }


        int getPort() {
            return hostPort.port;
        }


        long getTime() {
            return time;
        }


        void setTime(long time) {
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
