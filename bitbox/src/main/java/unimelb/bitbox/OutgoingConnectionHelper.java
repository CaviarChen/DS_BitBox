package unimelb.bitbox;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class OutgoingConnectionHelper {

    private static Logger log = Logger.getLogger(OutgoingConnectionHelper.class.getName());

    private PriorityQueue<PeerInfo> queue;

    public OutgoingConnectionHelper() {
        queue = new PriorityQueue<>(new Comparator<PeerInfo>() {
            @Override
            public int compare(PeerInfo o1, PeerInfo o2) {
                return Long.compare(o1.getTime(), o2.getTime());
            }
        });
    }

    public void execute() throws Exception {

        // read configuration property file
        readProperty();

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
        String req = "";

        conn.send(req);
    }

    private void readProperty() {
        // preceding slash for root property file
        String path = "/configuration.properties";

        try (InputStream input = new FileInputStream(path)) {

            Properties prop = new Properties();
            prop.load(input);

            String[] peers = prop.getProperty("peers").split(",");

            for (String peer : peers) {
                String[] data = peer.split(":");

                String host = data[0];
                int port = Integer.parseInt(data[1]);

                queue.add(new PeerInfo(host, port));
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
