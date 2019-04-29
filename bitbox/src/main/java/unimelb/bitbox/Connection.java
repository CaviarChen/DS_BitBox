package unimelb.bitbox;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.ThreadPool.Priority;
import unimelb.bitbox.util.ThreadPool.PriorityTask;
import unimelb.bitbox.util.ThreadPool.PriorityThreadPool;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

// TODO: make necessary methods thread-safe

public class Connection {
    private static Logger log = Logger.getLogger(Connection.class.getName());

    public final ConnectionType type;

    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;

    private Thread thread;

    private boolean active;
    private HostPort hostPort;

    public Connection(ConnectionType type, Socket socket) throws IOException {
        this.type = type;
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter =  new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        active = false;
        hostPort = null;
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
            } catch (IOException e) {
                // log
                close();
            }
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
