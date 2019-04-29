package unimelb.bitbox;

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

    private static final int CHECK_INTERVAL = 10000;
    private static final int PENALTY_TIME = 5000;
    private static final int HANDSHAKE_TIMEOUT = 20000;
    private static Logger log = Logger.getLogger(OutgoingConnectionHelper.class.getName());

    private String handshakeRequestJson;
    private PriorityQueue<PeerInfo> queue;

    public OutgoingConnectionHelper(String advertisedName, int port) {

        Protocol.HandshakeRequest handshakeRequest = new Protocol.HandshakeRequest();
        handshakeRequest.peer.host = advertisedName;
        handshakeRequest.peer.port = port;
        handshakeRequestJson = ProtocolFactory.marshalProtocol(handshakeRequest);

        queue = new PriorityQueue<>(Comparator.comparingLong(PeerInfo::getTime));

        String[] peers = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_PEERS).split(Constants.CONFIG_PEERS_SEPARATOR);

        for (String peer : peers) {
            String[] data = peer.split(Constants.CONFIG_HOSTNAME_PORT_SEPARATOR);
            queue.add(new PeerInfo(data[0], Integer.parseInt(data[1])));
        }
    }

    public void execute(){

        // if queue is not empty
        while (queue.peek() != null) {

            if (queue.peek().getTime() <= System.currentTimeMillis()) {
                PeerInfo peer = queue.poll();

                try {
                    Socket clientSocket = new Socket(peer.getHost(), peer.getPort());
                    Connection conn = new Connection(Connection.ConnectionType.OUTGOING, clientSocket);
                    log.info(String.format("Start connecting to port: %d", peer.getPort()));
                    requestHandshake(conn);

                } catch (IOException e) {
                    log.warning(e.toString());
                    peer.setTime(System.currentTimeMillis() + PENALTY_TIME);
                    queue.add(peer);
                }
            } else {
                // sleep 60 seconds
                try {
                    sleep(CHECK_INTERVAL);
                } catch (InterruptedException e) {
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

        Protocol protocol = ProtocolFactory.parseProtocol(json);
        ProtocolType protocolType = ProtocolType.typeOfProtocol(protocol);

        // TODO: waiting for InvalidProtocolException to be completed
        if (protocolType == null) {
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
                    queue.add(new PeerInfo(hp.host, hp.port));
                }
                conn.close();
                break;
            case INVALID_PROTOCOL:
                // TODO add into log
                conn.close();
                break;
            default:
                conn.abortWithInvalidProtocol("Unexpected protocol: " + protocol.getClass().getName());
        }
    }

    private class PeerInfo {

        private int port;
        private long time;
        private String host;

        PeerInfo(String host, int port) {
            this.host = host;
            this.port = port;
            this.time = System.currentTimeMillis();
        }

        int getPort() {
            return port;
        }

        long getTime() { return time; }

        void setTime(long time) {
            this.time = time;
        }

        String getHost() {
            return host;
        }
    }
}
