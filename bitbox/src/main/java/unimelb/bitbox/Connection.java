package unimelb.bitbox;

import unimelb.bitbox.util.HostPort;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Connection {

    public final ConnectionType type;

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private boolean initialized;
    private HostPort hostPort;

    public Connection(ConnectionType type, Socket socket) throws IOException {
        this.type = type;
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter =  new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        initialized = false;
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

    public void sendResponse(String msg) {
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
        // TODO: implement
    }

    public void init(HostPort hostPort) {
        if (!initialized) {
            this.hostPort = hostPort;
            initialized = true;
        }
    }

    public HostPort getHostPort() {
        return hostPort;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public enum ConnectionType {
        INCOMING,
        OUTGOING
    }
}
