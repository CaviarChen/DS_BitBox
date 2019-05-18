package unimelb.bitbox.util.ConnectionUtils;

import unimelb.bitbox.protocol.Protocol;

public abstract class Connection {
    public abstract void sendAsync(Protocol protocol);
}
