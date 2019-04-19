package unimelb.bitbox;

import unimelb.bitbox.util.HostPort;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class IncomingConnectionHelper {
    private static Logger log = Logger.getLogger(IncomingConnectionHelper.class.getName());

    private Thread thread;

    public IncomingConnectionHelper(int port) {
        thread = new Thread(()->{
            try {
                execute(port);
            } catch (Exception e) {
                log.severe(e.toString());
            }

        });
        thread.start();
    }

    private void execute(int port) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        log.info(String.format("Start listening to port: %d", port));

        while (!Thread.currentThread().isInterrupted()) {
            Socket clientSocket = serverSocket.accept();

            // TODO: catch
            Connection conn = new Connection(Connection.ConnectionType.INCOMING, clientSocket);

            // TODO: put this in thread pool
            handleHandshake(conn);

        }

        log.info("Stop listening to incoming connection");
    }

    private void handleHandshake(Connection conn) {
        // TODO: timeout
        String req = conn.waitForOneRequest();
        if (req == null) return;

        // parse handshake, get host&port
        HostPort hostPort = new HostPort("test", 1);

        int res = ConnectionManager.getInstance().addConnection(conn, hostPort);
        if (res == -1) {
            // exceed limit
        } else if (res == -2) {
            // already exists
        } else {
            // ok

        }

    }

}
