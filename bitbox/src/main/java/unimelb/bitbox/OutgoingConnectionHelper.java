package unimelb.bitbox;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolType;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class OutgoingConnectionHelper {

    private static Logger log = Logger.getLogger(OutgoingConnectionHelper.class.getName());

    private String handshakeRequestJson;
    private PriorityQueue<PeerInfo> queue;

    public OutgoingConnectionHelper(String advertisedName, int port) {

        Protocol.HandshakeRequest handshakeRequest = new Protocol.HandshakeRequest();
        handshakeRequest.peer.host = advertisedName;
        handshakeRequest.peer.port = port;
        handshakeRequestJson = ProtocolFactory.marshalProtocol(handshakeRequest);

        queue = new PriorityQueue<>(new Comparator<PeerInfo>() {
            @Override
            public int compare(PeerInfo o1, PeerInfo o2) {
                return Long.compare(o1.getTime(), o2.getTime());
            }
        });

        String[] peers = Configuration.getConfigurationValue("peers").split(",");

        for (String peer : peers) {
            String[] data = peer.split(":");
            queue.add(new PeerInfo(data[0], Integer.parseInt(data[1])));
        }
    }

    public void execute() throws Exception {

        while (true) {

            // empty priority queue
            if (queue.peek() == null) {
                return;
            }

            if (queue.peek().getTime() <= System.currentTimeMillis()) {
                PeerInfo peer = queue.poll();

                Socket clientSocket = new Socket(peer.getHost(), peer.getPort());
                Connection conn = new Connection(Connection.ConnectionType.OUTGOING, clientSocket);

                log.info(String.format("Start connecting to port: %d", peer.getPort()));

                requestHandshake(conn);

            } else {
                // sleep 60 seconds
                sleep(60000);
            }
        }
    }

    private void requestHandshake(Connection conn) {
        conn.send(handshakeRequestJson);

        String json = conn.waitForOneRequest();
        Protocol protocol = ProtocolFactory.parseProtocol(json);

        if (protocol == null) {
            // TODO
            return;
        }

        switch (ProtocolType.typeOfProtocol(protocol)) {
            case HANDSHAKE_RESPONSE:
                Protocol.HandshakeResponse handshakeResponse = (Protocol.HandshakeResponse) protocol;
                HostPort hostPort = handshakeResponse.peer;

                int res = ConnectionManager.getInstance().addConnection(conn, hostPort);
                if (res == 0) {
                    conn.active(hostPort);
                    return;
                } else if (res == -2) {
                    // already exists
                    Protocol.InvalidProtocol invalidProtocol = new Protocol.InvalidProtocol();
                    invalidProtocol.msg = "HostPort is already existed";
                    conn.send(ProtocolFactory.marshalProtocol(invalidProtocol));
                }
                conn.close();
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
        }
    }
}
