package unimelb.bitbox.util.FileSystem;


import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolField;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.ConnectionUtils.Peer.Connection;
import unimelb.bitbox.util.MessageHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;


/**
 * FileLoaderWrapper provides methods to receive and request file bytes.
 * A number of requests (REQUEST_LIMIT) are sent firstly and then when we get a response we send a request.
 * In other words, a 'window' is used to get all the file bytes.
 * Additionally, it also tries to get file bytes from multiple connections if possible.
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class FileLoaderWrapper {
    private static Logger log = Logger.getLogger(Connection.class.getName());

    private static final long BLOCK_SIZE =
            Long.parseLong(Configuration.getConfigurationValue(Constants.CONFIG_FIELD_BLOCKSIZE));
    private static final int REQUEST_LIMIT = 10; // the number of requests in the first batch
    private static final long TIMEOUT_IN_MILLIS = 20000;

    // messages that are pending to be sent
    private final LinkedList<ProtocolField.FilePosition> pending = new LinkedList<>();
    private final HashMap<Connection, ConnectionInfo> connectionInfoMap = new HashMap<>();

    private ProtocolField.FileDes fileDes;
    private FileSystemManager fileSystemManager;


    /**
     * Constructor of FileLoaderWrapper
     *
     * @param fileDes           the file descriptor
     * @param fileSystemManager the file system manager
     * @param conn              the connection that requests to send the file
     */
    public FileLoaderWrapper(ProtocolField.FileDes fileDes, FileSystemManager fileSystemManager, Connection conn) {

        this.fileDes = fileDes;
        this.fileSystemManager = fileSystemManager;

        long base = 0, remaining = fileDes.fileSize;

        synchronized (this) {

            connectionInfoMap.put(conn, new ConnectionInfo());

            // Split the whole file into blocks and save it to the pending list
            while (remaining > 0) {
                ProtocolField.FilePosition pos = new ProtocolField.FilePosition();
                pos.pos = base;
                pos.len = Math.min(remaining, BLOCK_SIZE);
                base += pos.len;
                remaining -= pos.len;
                pending.addLast(pos);
            }
        }

        send(REQUEST_LIMIT, conn);
    }


    /**
     * Check if the MD5 is equal to the current file which is transmitting
     *
     * @param md5 The MD5 to check
     * @return true if the MD5 is equal, false otherwise
     */
    public boolean checkMd5(String md5) {
        return this.fileDes.md5.equals(md5);
    }


    /**
     * Add a new connection to transmitting the same file
     *
     * @param conn the connection wants to transmit the file
     */
    public void addNewConnection(Connection conn) {

        synchronized (this) {
            if (connectionInfoMap.containsKey(conn)) return;
            ConnectionInfo connectionInfo = new ConnectionInfo();
            connectionInfoMap.put(conn, connectionInfo);
        }

        send(REQUEST_LIMIT, conn);
    }


    /**
     * Deal with the situation when we receive a file byte response
     *
     * @param fileBytesResponse the file byte response we received
     * @param conn              the connection that is transmitting the file
     */
    public void received(Protocol.FileBytesResponse fileBytesResponse, Connection conn) {
        String filePath = fileBytesResponse.fileDes.path;

        // discard the response when MD5 is not equal to the file requested to be sent
        if (!fileDes.md5.equals(fileBytesResponse.fileDes.md5)) {
            return;
        }

        ProtocolField.FilePosition pos = new ProtocolField.FilePosition();
        pos.len = fileBytesResponse.fileContent.len;
        pos.pos = fileBytesResponse.fileContent.pos;

        ConnectionInfo connectionInfo;

        // check if the request is the one we are waiting for and if yes, update last active time
        synchronized (this) {
            connectionInfo = connectionInfoMap.get(conn);

            if (connectionInfo == null || !connectionInfo.waiting.contains(pos)) {
                return;
            }
            connectionInfo.lastActiveTime = System.currentTimeMillis();
        }

        // write to the file according to the response received
        ProtocolField.FileContent fc = fileBytesResponse.fileContent;
        ByteBuffer src = ByteBuffer.wrap(Base64.getDecoder().decode(fc.content));
        try {
            if (!fileSystemManager.writeFile(filePath, src, fc.pos)) {
                cancel();
                return;
            }
        } catch (IOException e) {
            log.warning(e.toString());
            return;
        }


        // send the next request
        send(1, conn);

        synchronized (this) {
            connectionInfo.waiting.remove(pos);

            // only check complete when there is nothing in the pending list or waiting sets
            if (!pending.isEmpty()) return;
            for (ConnectionInfo info : connectionInfoMap.values()) {
                if (!info.waiting.isEmpty()) return;
            }
        }

        // check if the whole file is completed for transmitting
        try {
            fileSystemManager.checkWriteComplete(fileBytesResponse.fileDes.path);
        } catch (NoSuchAlgorithmException | IOException ignored) {
            cancel();
        }

        MessageHandler.removeFileLoaderWrapper(this, fileDes.path);
    }


    // clean up the connection that is timeout
    public void clean() {
        // not accurate since this will be triggered roughly every syncInterval and with low priority
        synchronized (this) {

            Iterator<Map.Entry<Connection, ConnectionInfo>> it = connectionInfoMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<Connection, ConnectionInfo> entry = it.next();
                // time out, remove this connection and add everything back to pending list
                if (System.currentTimeMillis() - entry.getValue().lastActiveTime > TIMEOUT_IN_MILLIS) {
                    pending.addAll(entry.getValue().waiting);
                    it.remove();

                    log.info("Connection cleaned, path:" + fileDes.path
                            + ", Connection: " + entry.getKey().getHostPort().toString());
                }
            }

            // no active connection, cancel
            if (connectionInfoMap.isEmpty()) {
                log.info("Task cleaned, path:" + fileDes.path);
                cancel();
            }
        }
    }


    // stores the last active time and waiting set (requests have been set and still waiting for response
    private static class ConnectionInfo {
        HashSet<ProtocolField.FilePosition> waiting;
        long lastActiveTime;


        public ConnectionInfo() {
            waiting = new HashSet<>();
            lastActiveTime = System.currentTimeMillis();
        }
    }


    // send pending messages up to the limit
    private void send(int limit, Connection conn) {
        ArrayList<ProtocolField.FilePosition> posList = new ArrayList<>();

        synchronized (this) {
            ConnectionInfo connectionInfo = connectionInfoMap.get(conn);
            if (connectionInfo == null) return;

            for (int i = 0; i < limit; i++) {
                ProtocolField.FilePosition sendPos;
                sendPos = pending.pollFirst();
                if (sendPos != null) {
                    connectionInfo.waiting.add(sendPos);
                    posList.add(sendPos);
                } else {
                    break;
                }
            }
        }

        for (ProtocolField.FilePosition pos : posList) {
            SendFileByteRequest(pos, conn);
        }
    }


    // send the file byte request to the connection
    private void SendFileByteRequest(ProtocolField.FilePosition filePosition, Connection conn) {
        Protocol.FileBytesRequest fileBytesRequest = new Protocol.FileBytesRequest();

        fileBytesRequest.fileDes = this.fileDes;
        fileBytesRequest.filePos = filePosition;

        conn.sendAsync(fileBytesRequest);
    }


    // cancel transmitting this file and close the file loader
    private void cancel() {
        try {
            fileSystemManager.cancelFileLoader(this.fileDes.path);
        } catch (Exception ignored) {
        }

        MessageHandler.removeFileLoaderWrapper(this, fileDes.path);
    }
}
