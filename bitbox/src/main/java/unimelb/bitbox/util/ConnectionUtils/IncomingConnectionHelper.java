package unimelb.bitbox.util.ConnectionUtils;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.HostPort;

import java.util.ArrayList;
import java.util.logging.Logger;

public abstract class IncomingConnectionHelper {
    private static final long PEERS_CACHE_TIMEOUT = 10000;

    private static Logger log = Logger.getLogger(IncomingConnectionHelper.class.getName());

    private Thread thread = null;

    private ArrayList<HostPort> connectedPeersCache;
    private long connectedPeersCacheTime = 0;
    private final Object connectedPeersCacheLock = new Object();

    protected String handshakeResponseJsonCache;

    protected IncomingConnectionHelper(String advertisedName, int port) {
        Protocol.HandshakeResponse handshakeResponse = new Protocol.HandshakeResponse();
        handshakeResponse.peer.host = advertisedName;
        handshakeResponse.peer.port = port;
        handshakeResponseJsonCache = ProtocolFactory.marshalProtocol(handshakeResponse);
    }

    /**
     * start working thread
     */
    public void start() {
        if (thread != null) throw new RuntimeException("Already started");

        thread = new Thread(() -> {
            try {
                execute();
            } catch (Exception e) {
                log.severe(e.toString());
            }

        });
        thread.start();
    }

    protected abstract void execute() throws Exception;

    // get & cache the connected peer since we don't want refusing a connection be costly
    protected ArrayList<HostPort> getCachedPeers() {
        synchronized (connectedPeersCacheLock) {
            if (System.currentTimeMillis() - connectedPeersCacheTime > PEERS_CACHE_TIMEOUT) {
                connectedPeersCache = ConnectionManager.getInstance().getConnectedPeers();
                connectedPeersCacheTime = System.currentTimeMillis();
            }
            return connectedPeersCache;
        }
    }
}
