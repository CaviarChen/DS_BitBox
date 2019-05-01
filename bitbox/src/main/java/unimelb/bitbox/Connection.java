package unimelb.bitbox;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.FileByteMonitor;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.logging.Logger;


public class Connection {
    private static Logger log = Logger.getLogger(Connection.class.getName());

    public final ConnectionType type;

    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;
    private final LinkedList<String> sendingQueue = new LinkedList<>();

    private Thread thread;

    private FileByteMonitor fileByteMonitor;

    private boolean active;
    private HostPort hostPort;

    public Connection(ConnectionType type, Socket socket) throws IOException {
        this.type = type;
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter =  new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        active = false;
        hostPort = null;

        fileByteMonitor = new FileByteMonitor();
    }

    public FileByteMonitor GetFileByteMonitor() {
        return fileByteMonitor;
    }

    // not thread-safe
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

    // not thread-safe
    public String waitForOneMessage(int timeout) throws SocketTimeoutException {
        String msg;
        try {
            this.socket.setSoTimeout(timeout);
             msg = bufferedReader.readLine();
            log.info("Message Received: " + msg);
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                throw (SocketTimeoutException) e;
            }
            // log
            close();
            return null;
        }
        return msg;
    }

    public void send(String msg) {
        synchronized (bufferedWriter) {
            try {
                bufferedWriter.write(msg);
                bufferedWriter.flush();
                log.info("Message Sent: " + msg);
            } catch (IOException e) {
                // log
                close();
            }
        }
    }

    public void sendAsync(String msg) {
        boolean isEmpty;
        synchronized (sendingQueue) {
            isEmpty = sendingQueue.isEmpty();
            sendingQueue.addLast(msg);
        }

        if (isEmpty) {
            // first in a while, need to start sending thread
            PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                    "Connection: SendingQueue",
                    Priority.NORMAL,
                    this::asyncSendingThread
            ));
        }
    }


    private void asyncSendingThread() {
        boolean isLastOne = false;
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



    public void abortWithInvalidProtocol(String additionalMsg) {
        Protocol.InvalidProtocol invalidProtocol = new Protocol.InvalidProtocol();
        invalidProtocol.msg = additionalMsg;
        send(ProtocolFactory.marshalProtocol(invalidProtocol));
        close();
    }

    public void close() {
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
        }
    }

    // active connection will create its own thread for waiting for request
    // non-blocking method might be better here
    public void active(HostPort hostPort) {
        if (!active) {
            active = true;
            this.hostPort = hostPort;
            thread = new Thread(this::work);
            thread.start();
        }
    }

    private void work() {
        try {
            while (!thread.isInterrupted()) {
                String msg = this.waitForOneMessage();

                PriorityThreadPool.getInstance().submitTask(new PriorityTask(
                        "Connection: MessageHandler",
                        Priority.NORMAL,
                        () -> MessageHandler.handleMessage(msg, this)
                ));
            }
        } catch (Exception e) {
            // TODO: log
            this.close();
        }
    }

    public HostPort getHostPort() {
        return hostPort;
    }

    public boolean isActive() {
        return active;
    }

    public enum ConnectionType {
        INCOMING,
        OUTGOING
    }
}
