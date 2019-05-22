package unimelb.bitbox.util.ConnectionUtils.Peer;

import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.InvalidProtocolException;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolType;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.Scheduler;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UDPIncomingConnectionHelper extends IncomingConnectionHelper {
    private static Logger log = Logger.getLogger(UDPIncomingConnectionHelper.class.getName());
    private static final int BUFFER_SIZE = 2048;

    private final int port;

    private final DatagramSocket serverSocket;

    public UDPIncomingConnectionHelper(String advertisedName, int port) throws SocketException {
        super(advertisedName, port);
        this.port = port;
        serverSocket = new DatagramSocket(port);
    }

    public DatagramSocket getServerSocket() {
        return serverSocket;
    }

    @Override
    protected void execute() throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];

        log.info(String.format("Start listening to port: %d", port));

        // register UDP connection timeout check
        Scheduler.getInstance().addTask(400, TimeUnit.MILLISECONDS,
                new PriorityTask( "check timeout UDP request",
                        Priority.LOW,
                        UDPConnection::checkTimeoutRequest
                ));

        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                boolean handled = UDPConnection.distributeMessage(packet);
                if (!handled) {
                    // new connection, try handshake
                    // make a copy
                    String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                            "Incoming connection: handshake",
                            Priority.NORMAL,
                            () -> handleHandshake(serverSocket, msg, packet.getAddress(), packet.getPort())
                    ));
                }

            } catch (Exception e) {
                log.warning(e.toString());
            }
        }
    }

    private void handleHandshake(DatagramSocket serverSocket, String msg, InetAddress hostAddress, int actualPort) {
        String replyMsg = "";
        int res = -1;
        try {
            Protocol protocol = ProtocolFactory.parseProtocol(msg);
            if (ProtocolType.typeOfProtocol(protocol) == ProtocolType.HANDSHAKE_REQUEST) {
                Protocol.HandshakeRequest handshakeRequest = (Protocol.HandshakeRequest) protocol;
                UDPConnection conn = new UDPConnection(serverSocket, handshakeRequest.peer, hostAddress, actualPort);

                res = ConnectionManager.getInstance().addConnection(conn, handshakeRequest.peer);
                if (res == 0) {
                    // success
                    replyMsg = handshakeResponseJsonCache;
                    conn.active();

                } else {
                    Protocol.ConnectionRefused connectionRefused = new Protocol.ConnectionRefused();
                    connectionRefused.peers = getCachedPeers();
                    if (res == -1) {
                        // over limit
                        connectionRefused.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_CONNECTION_REFUSED_LIMIT_REACHED;
                    } else if (res == -2) {
                        // already connected
                        connectionRefused.msg = Constants.PROTOCOL_RESPONSE_MESSAGE_CONNECTION_REFUSED_ALREADY_EXIST;
                    }
                    replyMsg = ProtocolFactory.marshalProtocol(connectionRefused);
                }
            } else {
                throw new InvalidProtocolException("Expected HandshakeRequest but got: " + ((protocol == null) ? "null" : protocol.getClass().getName()), null);
            }

        } catch (InvalidProtocolException | UDPConnection.CException e) {
            Protocol.InvalidProtocol invalidProtocol = new Protocol.InvalidProtocol();
            invalidProtocol.msg = e.getMessage();
            replyMsg = ProtocolFactory.marshalProtocol(invalidProtocol);
        }

        // send reply
        log.info("Handshake Finished Result: " + res +
                " reply: " + replyMsg.substring(0, Math.min(UDPConnection.MAX_LOG_LEN, replyMsg.length())));

        byte[] buffer = replyMsg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, hostAddress, actualPort);
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            log.warning("unable reply to: " + hostAddress.getHostAddress() + ":" + actualPort);
        }

    }
}
