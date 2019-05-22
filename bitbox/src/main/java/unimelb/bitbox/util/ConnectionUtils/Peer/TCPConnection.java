package unimelb.bitbox.util.ConnectionUtils.Peer;


import unimelb.bitbox.protocol.IResponse;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.ConnectionManager;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.MessageHandler;
import unimelb.bitbox.util.SyncManager;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.logging.Logger;


/**
 * Class for a specific connection with other peer
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
class TCPConnection extends Connection {

    private static Logger log = Logger.getLogger(TCPConnection.class.getName());

    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;
    private final LinkedList<String> sendingQueue = new LinkedList<>();

    private Thread thread;

    private boolean active;
    private boolean isClosed = false;
    private TCPOutgoingConnectionHelper outgoingConnectionHelper = null;

    // main constructor
    private TCPConnection(ConnectionType type, Socket socket) throws IOException {
        super(type);
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        active = false;
        hostPort = null;
    }

    /**
     * Constructor
     * @param socket the established socket
     * @throws IOException
     */
    public TCPConnection(Socket socket) throws IOException {
        this(ConnectionType.INCOMING, socket);
    }


    /**
     * Constructor
     * @param socket the established socket
     * @param outgoingConnectionHelper the helper which established the given connection
     * @throws IOException
     */
    public TCPConnection(Socket socket, TCPOutgoingConnectionHelper outgoingConnectionHelper) throws IOException {
        this(ConnectionType.OUTGOING, socket);
        this.outgoingConnectionHelper = outgoingConnectionHelper;
    }


    /**
     * Wait and get one message form this connection
     * blocking & not thread-safe, should only be called outside during the handshake
     * should only be used internally after the handshake
     * @return a message string or null
     */
    public String waitForOneMessage() {
        String msg = "";
        try {
            // infinite waiting time
            msg = this.waitForOneMessage(0);
        } catch (SocketTimeoutException e) {
            // no gonna happen
        }
        return msg;
    }


    /**
     * Wait and get one message form this connection
     * blocking & not thread-safe, should only be called outside during the handshake
     * should only be used internally after the handshake
     * @param timeout in millis
     * @return  a message string or null
     * @throws SocketTimeoutException if timeout
     */
    public String waitForOneMessage(int timeout) throws SocketTimeoutException {
        String msg;
        try {
            this.socket.setSoTimeout(timeout);
            msg = bufferedReader.readLine();
            if (msg != null)
                log.info(currentHostPort() + " Message Received: "
                        + msg.substring(0, Math.min(MAX_LOG_LEN, msg.length())));
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                throw (SocketTimeoutException) e;
            }
            close();
            return null;
        }
        return msg;
    }

    protected void send(String msg) {
        synchronized (bufferedWriter) {
            try {
                bufferedWriter.write(msg + '\n');
                bufferedWriter.flush();
                log.info(currentHostPort() + " Message Sent: "
                        + msg.substring(0, Math.min(MAX_LOG_LEN, msg.length())));
            } catch (IOException e) {
                // log
                close();
            }
        }
    }


    @Override
    public void sendAsync(Protocol protocol) {
        String msg = ProtocolFactory.marshalProtocol(protocol);
        boolean isEmpty;
        synchronized (sendingQueue) {
            isEmpty = sendingQueue.isEmpty();
            sendingQueue.addLast(msg);
        }

        if (isEmpty) {
            // first in a while, need to start sending thread
            PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                    "Connection: SendingQueue",
                    Priority.HIGH,
                    this::asyncSendingThread
            ));
        }
    }

    /**
     * send InvalidProtocol and close this connection
     * @param additionalMsg additional message in the InvalidProtocol
     */
    @Override
    public void abortWithInvalidProtocol(String additionalMsg) {
        Protocol.InvalidProtocol invalidProtocol = new Protocol.InvalidProtocol();
        invalidProtocol.msg = additionalMsg;
        send(ProtocolFactory.marshalProtocol(invalidProtocol));
        close();
    }

    @Override
    public void markRequestAsDone(IResponse response) {
        // do nothing, no retry for TCP
    }

    @Override
    public boolean allowInvalidMessage() {
        return false;
    }


    /**
     * close this connection
     */
    @Override
    public void close(Boolean allowReconnect) {

        synchronized (socket) {
            if (isClosed) {
                return;
            }
            isClosed = true;
        }

        log.info(currentHostPort() + " Connection Closed");

        try {
            socket.close();
        } catch (IOException e) {
            log.severe(e.toString());
        }

        // Reentrant
        synchronized (sendingQueue) {
            sendingQueue.clear();
        }

        if (active) {
            active = false;
            thread.interrupt();
            // unregister from ConnectionManager
            ConnectionManager.getInstance().removeConnection(this);

            // if it is an outgoing connection, and allowReconnect is true
            // add back to queue for retry this hostPort later
            if (type == ConnectionType.OUTGOING && allowReconnect) {
                outgoingConnectionHelper.scheduleConnectionTask(hostPort, OutgoingConnectionHelper.RECONNECT_INTERVAL);
            }
        }
    }

    /**
     * active connection will create its own thread for waiting for request
     * A better way could be waiting all requests using non-blocking method
     * @param hostPort the host&port got from handshake process
     */
    public void active(HostPort hostPort) {
        if (!active) {
            active = true;
            this.hostPort = hostPort;
            thread = new Thread(this::work);
            thread.start();
        }
    }

    /**
     * @return true if the connection is active (passed handshake)
     */
    public boolean isActive() {
        return active;
    }


    // function for async sending
    private void asyncSendingThread() {
        boolean isLastOne = false;
        // stop when there is no more jobs
        // this will be trigger again when there is a job after a while
        while (!isLastOne) {
            String msg;
            synchronized (sendingQueue) {
                if (sendingQueue.isEmpty()) {
                    return;
                }
                msg = sendingQueue.removeFirst();
                isLastOne = sendingQueue.isEmpty();
            }
            send(msg);
        }
    }

    // work thread for waiting request
    private void work() {
        try {

            // first sync
            SyncManager.getInstance().syncWithOneAsync(this);

            while (!thread.isInterrupted()) {
                try {

                    String msg = this.waitForOneMessage();
                    if (msg == null) break;

                    PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                            "Connection: MessageHandler",
                            Priority.NORMAL,
                            () -> MessageHandler.handleMessage(msg, this)
                    ));

                } catch (Exception e) {
                    log.warning(currentHostPort() + ", Exception: " + e.toString() + "");
                    break;
                }
            }

            this.close();

        } catch (Exception e) {
            log.warning(currentHostPort() + ", Exception: " + e.toString() + "");
            this.close();
        }
    }


}