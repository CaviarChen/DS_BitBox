package unimelb.bitbox.util.ConnectionUtils;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.util.HostPort;

public abstract class Connection {
    protected HostPort hostPort;
    protected static final int MAX_LOG_LEN = 250;


    public abstract void sendAsync(Protocol protocol);

    public abstract void close();

    // get a string that represents this connection
    protected String currentHostPort() {
        return (hostPort == null) ? "[Unknown]" : "[" + hostPort.toString() + "]";
    }
}
