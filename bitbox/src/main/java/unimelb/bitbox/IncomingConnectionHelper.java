package unimelb.bitbox;

import unimelb.bitbox.protocol.InvalidProtocolException;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolType;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

public class IncomingConnectionHelper {
    private static final int HANDSHAKE_TIMEOUT = 10000;
    private static Logger log = Logger.getLogger(IncomingConnectionHelper.class.getName());

    private Thread thread;
    private String handshakeResponseJson;

    public IncomingConnectionHelper(String advertisedName, int port) {

        Protocol.HandshakeResponse handshakeResponse = new Protocol.HandshakeResponse();
        handshakeResponse.peer.host = advertisedName;
        handshakeResponse.peer.port = port;
        handshakeResponseJson = ProtocolFactory.marshalProtocol(handshakeResponse);

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

            try {
                Connection conn = new Connection(clientSocket);

                // current design: use thread pool for handshake process, then create its own thread if success
                PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                        "Incoming connection: handshake",
                        Priority.NORMAL,
                        () -> handleHandshake(conn)
                ));
            } catch (Exception e) {
                log.warning(e.toString());
            }
        }

        log.info("Stop listening to incoming connection");
    }

    private void handleHandshake(Connection conn) {

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
                    conn.send(handshakeResponseJson);
                    conn.active(hostPort);
                    return;
                }

                if (res == -1) {
                    // exceed limit
                } else if (res == -2) {
                    // already exists
                }
                conn.close();
                return;
            }

            conn.abortWithInvalidProtocol("Expected HandashakeRequest but got: " +
                    ((protocol == null) ? "null" : protocol.getClass().getName()));


        } catch (InvalidProtocolException e) {
            conn.abortWithInvalidProtocol(e.getMessage());
        }

    }

}
