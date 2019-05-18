package unimelb.bitbox.util.ConnectionUtils.Helper;


import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.InvalidProtocolException;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolType;
import unimelb.bitbox.util.ConnectionUtils.Connection.TCPConnection;
import unimelb.bitbox.util.ConnectionUtils.ConnectionManager;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.logging.Logger;


/**
 * IncomingConnectionHelper deals with all incoming connections
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class TCPIncomingConnectionHelper extends IncomingConnectionHelper{
    private static Logger log = Logger.getLogger(TCPIncomingConnectionHelper.class.getName());

    private static final int HANDSHAKE_TIMEOUT = 10000;
    private static final long PEERS_CACHE_TIMEOUT = 10000;

    private Thread thread = null;
    private String handshakeResponseJson;
    private ArrayList<HostPort> connectedPeersCache;
    private long connectedPeersCacheTime = 0;
    private final Object connectedPeersCacheLock = new Object();

    private int port;

    /**
     * Constructor
     * @param advertisedName from config
     * @param port listening port from config
     */
    public TCPIncomingConnectionHelper(String advertisedName, int port) {

        Protocol.HandshakeResponse handshakeResponse = new Protocol.HandshakeResponse();
        handshakeResponse.peer.host = advertisedName;
        handshakeResponse.peer.port = port;
        handshakeResponseJson = ProtocolFactory.marshalProtocol(handshakeResponse);

        this.port = port;
    }

    /**
     * start working thread
     */
    public void start() {
        if (thread != null) throw new RuntimeException("Already started");

        thread = new Thread(() -> {
            try {
                execute(port);
            } catch (Exception e) {
                log.severe(e.toString());
            }

        });
        thread.start();
    }


    // main work thread
    private void execute(int port) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        log.info(String.format("Start listening to port: %d", port));

        while (!Thread.currentThread().isInterrupted()) {
            Socket clientSocket = serverSocket.accept();

            try {
                TCPConnection conn = new TCPConnection(clientSocket);

                // if the incoming connection limit is exceed (roughly), then lower the priority
                // since the connection needs to be rejected eventually
                Priority priority = (ConnectionManager.getInstance().isIncommingConnectionFull())
                        ? Priority.LOW : Priority.NORMAL;

                // current design: use thread pool for handshake process, then create its own thread if success
                PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                        "Incoming connection: handshake",
                        priority,
                        () -> handleHandshake(conn)
                ));
            } catch (Exception e) {
                log.warning(e.toString());
            }
        }

        log.info("Stop listening to incoming connection");
    }

    // handle the handshake process (run in thread pool)
    private void handleHandshake(TCPConnection conn) {

        try {

            String json;
            try {
                json = conn.waitForOneMessage(HANDSHAKE_TIMEOUT);
            } catch (SocketTimeoutException e) {
                conn.abortWithInvalidProtocol("No handshake until timeout");
                return;
            }

            Protocol protocol = ProtocolFactory.parseProtocol(json);

            if (ProtocolType.typeOfProtocol(protocol) == ProtocolType.HANDSHAKE_REQUEST) {
                Protocol.HandshakeRequest handshakeRequest = (Protocol.HandshakeRequest) protocol;
                HostPort hostPort = handshakeRequest.peer;
                int res = ConnectionManager.getInstance().addConnection(conn, hostPort);
                if (res == 0) {
                    // success
                    conn.send(handshakeResponseJson);
                    conn.active(hostPort);
                    return;
                }

                Protocol.ConnectionRefused connectionRefused = new Protocol.ConnectionRefused();
                connectionRefused.peers = getCachedPeers();
                if (res == -1) {
                    // over limit
                    connectionRefused.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_CONNECTION_REFUSED_LIMIT_REACHED;
                } else if (res == -2) {
                    // already connected
                    connectionRefused.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_CONNECTION_REFUSED_ALREADY_EXIST;
                }
                conn.send(ProtocolFactory.marshalProtocol(connectionRefused));
                conn.close();
                return;
            }

            conn.abortWithInvalidProtocol("Expected HandashakeRequest but got: " +
                    ((protocol == null) ? "null" : protocol.getClass().getName()));


        } catch (InvalidProtocolException e) {
            conn.abortWithInvalidProtocol(e.getMessage());
        }

    }

    // get & cache the connected peer since we don't want refuse a connection be costly
    private ArrayList<HostPort> getCachedPeers() {
        synchronized (connectedPeersCacheLock) {
            if (System.currentTimeMillis() - connectedPeersCacheTime > PEERS_CACHE_TIMEOUT) {
                connectedPeersCache = ConnectionManager.getInstance().getConnectedPeers();
                connectedPeersCacheTime = System.currentTimeMillis();
            }
            return connectedPeersCache;
        }
    }

}
