package unimelb.bitbox;

import unimelb.bitbox.protocal.Protocol;
import unimelb.bitbox.protocal.ProtocolFactory;
import unimelb.bitbox.protocal.ProtocolType;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class IncomingConnectionHelper {
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

            // TODO: catch
            Connection conn = new Connection(Connection.ConnectionType.INCOMING, clientSocket);

            // current design: use thread pool for handshake process, then create its own thread if success
            PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                    "Incoming connection: handshake",
                    Priority.NORMAL,
                    () -> handleHandshake(conn)
            ));
        }

        log.info("Stop listening to incoming connection");
    }

    private void handleHandshake(Connection conn) {
        // TODO: timeout
        String json = conn.waitForOneRequest();
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

        Protocol.InvalidProtocol invalidProtocol = new Protocol.InvalidProtocol();
        invalidProtocol.msg = "unknown";
        conn.send(ProtocolFactory.marshalProtocol(invalidProtocol));
        conn.close();
    }

}
