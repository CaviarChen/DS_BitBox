package unimelb.bitbox;

import unimelb.bitbox.util.HostPort;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

// TODO: make necessary methods thread-safe

public class Connection {
    private static Logger log = Logger.getLogger(Connection.class.getName());

    public final ConnectionType type;

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private Thread thread;

    private boolean active;
    private HostPort hostPort;

    public Connection(ConnectionType type, Socket socket) throws IOException {
        this.type = type;
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter =  new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        active = false;
        hostPort = null;
    }

    public String waitForOneRequest() {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            // log
            close();
            return null;
        }
    }

    public void send(String msg) {
        // TODO: lock
        try {
            bufferedWriter.write(msg);
            bufferedWriter.flush();
        } catch (IOException e) {
            // log
            close();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            log.severe(e.toString());
        }

        bufferedWriter = null;
        bufferedReader = null;

        if (active) {
            active = false;
            thread.interrupt();
            // unregister from ConnectionManager
            ConnectionManager.getInstance().removeConnection(this);
        }
    }

    // active connection will create its own thread for waiting for request
    // non-blocking method might be better here
    public void active(HostPort hostPort) {
        if (!active) {
            active = true;
            this.hostPort = hostPort;
            thread = new Thread(this::work);
            thread.start();
        }
    }

    private void work() {
        try {
            while (!thread.isInterrupted()) {
                String msg = this.waitForOneRequest();

                // TODO: handle request

            }
        } catch (Exception e) {
            // TODO: log
            this.close();
        }
    }

    public HostPort getHostPort() {
        return hostPort;
    }

    public boolean isActive() {
        return active;
    }

    public enum ConnectionType {
        INCOMING,
        OUTGOING
    }
}
