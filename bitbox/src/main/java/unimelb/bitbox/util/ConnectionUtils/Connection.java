package unimelb.bitbox.util.ConnectionUtils;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.util.HostPort;

public abstract class Connection {

    protected Connection(ConnectionType type) {
        this.type = type;
    }

    /**
     * enum for connection types
     */
    public enum ConnectionType {
        INCOMING,
        OUTGOING
    }

    protected HostPort hostPort;
    protected static final int MAX_LOG_LEN = 250;

    public final ConnectionType type;


    public abstract void sendAsync(Protocol protocol);

    public abstract void close();

    public abstract void abortWithInvalidProtocol(String additionalMsg);

    // get a string that represents this connection
    protected String currentHostPort() {
        return (hostPort == null) ? "[Unknown]" : "[" + hostPort.toString() + "]";
    }

    public HostPort getHostPort() {
        return hostPort;
    }
}
