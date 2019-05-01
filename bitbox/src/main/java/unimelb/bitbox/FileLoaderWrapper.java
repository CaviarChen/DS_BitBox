package unimelb.bitbox;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolField;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;


public class FileLoaderWrapper {
    private static Logger log = Logger.getLogger(Connection.class.getName());

    private static final long BLOCK_SIZE = Long.parseLong(Configuration.getConfigurationValue("blockSize"));
    private static final int REQUEST_LIMIT = 10;

    private final LinkedList<ProtocolField.FilePosition> pending = new LinkedList<>();
    private final HashSet<ProtocolField.FilePosition> waiting = new HashSet<>();

    private Connection conn;
    private ProtocolField.FileDes fileDes;
    private FileSystemManager fileSystemManager;

    public FileLoaderWrapper(Protocol.FileCreateRequest fileCreateRequest, FileSystemManager fileSystemManager, Connection conn) {

        this.fileDes = fileCreateRequest.fileDes;
        this.conn = conn;
        this.fileSystemManager = fileSystemManager;

        long base = 0, remaining = fileDes.fileSize;

        synchronized (this) {
            while (remaining > 0) {
                ProtocolField.FilePosition pos = new ProtocolField.FilePosition();
                pos.pos = base;
                pos.len = Math.min(remaining, BLOCK_SIZE);
                base += pos.len;
                remaining -= pos.len;
                pending.addLast(pos);
            }
        }

        send(REQUEST_LIMIT);
    }

    public void received(Protocol.FileBytesResponse fileBytesResponse) {
        String filePath = fileBytesResponse.fileDes.path;

        if (!fileDes.md5.equals(fileBytesResponse.fileDes.md5)) {
            return ;
        }

        ProtocolField.FilePosition pos = new ProtocolField.FilePosition();
        pos.len = fileBytesResponse.fileContent.len;
        pos.pos = fileBytesResponse.fileContent.pos;

        synchronized (this) {
            if (!waiting.contains(pos)) {
                return;
            }
        }

        ProtocolField.FileContent fc = fileBytesResponse.fileContent;
        ByteBuffer src = ByteBuffer.wrap(Base64.getDecoder().decode(fc.content));
        try {
            if (!fileSystemManager.writeFile(filePath, src , fc.pos)) {
                cancel();
                return;
            }
        } catch (IOException e) {
            log.warning(e.toString());
            return ;
        }

        send(1);

        synchronized (this) {
            waiting.remove(pos);

            if (!waiting.isEmpty() && !pending.isEmpty()) {
                return ;
            }
        }

        try {
            fileSystemManager.checkWriteComplete(fileBytesResponse.fileDes.path);
        } catch (NoSuchAlgorithmException | IOException e) {
            cancel();
        }
    }

    private void send(int limit) {
        ArrayList<ProtocolField.FilePosition> posList = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            ProtocolField.FilePosition sendPos;
            synchronized (this) {
                sendPos = pending.pollFirst();
                if (sendPos != null) {
                    waiting.add(sendPos);
                    posList.add(sendPos);
                } else {
                    break;
                }
            }
        }

        for (ProtocolField.FilePosition pos : posList) {
            SendFileByteRequest(pos);
        }
    }

    private void SendFileByteRequest(ProtocolField.FilePosition filePosition) {
        Protocol.FileBytesRequest fileBytesRequest = new Protocol.FileBytesRequest();

        fileBytesRequest.fileDes = this.fileDes;
        fileBytesRequest.filePos = filePosition;

        conn.send(ProtocolFactory.marshalProtocol(fileBytesRequest));
    }


    private void cancel() {
        try {
            fileSystemManager.cancelFileLoader(this.fileDes.path);
        } catch (Exception ignored) {
        }

        MessageHandler.removeFileLoaderWrapper(this, fileDes.path);
    }
//
//    private Connection getActiveConnection() {
//
//    }
//
//    public void addConnection() {
//
//    }
//
//    public void clean() {
//
//    }


}
