package unimelb.bitbox;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import unimelb.bitbox.util.HostPort;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Logger;

public class Connection {

    public final ConnectionType type;

    private Socket socket;
    private BufferedReader bufferedReader;
    private DataOutputStream dataOutputStream;

    private boolean initialized = false;
    private HostPort hostPort;

    public Connection(ConnectionType type, Socket socket) throws IOException {
        this.type = type;
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        hostPort = null;
    }

    public String waitForOneRequest() {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            // TODO: close connection
            return null;
        }

    }

    public void init(HostPort hostPort) {
        if (!initialized) {
            this.hostPort = hostPort;
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
