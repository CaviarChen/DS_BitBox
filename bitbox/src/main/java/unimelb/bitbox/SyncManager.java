package unimelb.bitbox;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolField;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

import java.util.logging.Logger;

public class SyncManager {
    private static SyncManager instance = new SyncManager();

    public static SyncManager getInstance() {
        return instance;
    }

    private FileSystemManager fileSystemManager = null;
    private static Logger log = Logger.getLogger(SyncManager.class.getName());

    private SyncManager() {
    }

    public void init(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }


    public void syncWithAllAsync() {
        log.info("Sync with all");
        for (FileSystemEvent event : fileSystemManager.generateSyncEvents()) {
            sendEventToAllAsync(event);
        }
    }

    public void syncWithOneAsync(Connection conn) {
        log.info("Sync with: " + conn.getHostPort().toString());

        for (FileSystemEvent event : fileSystemManager.generateSyncEvents()) {
            sendEventToOneAsync(event, conn);
        }
    }

    public void sendEventToAllAsync(FileSystemEvent fileSystemEvent) {
        Protocol protocol = eventToProtocol(fileSystemEvent);
        ConnectionManager.getInstance().broadcastMsgAsync(ProtocolFactory.marshalProtocol(protocol));
    }

    private void sendEventToOneAsync(FileSystemEvent fileSystemEvent, Connection conn) {
        Protocol protocol = eventToProtocol(fileSystemEvent);
        conn.sendAsync(ProtocolFactory.marshalProtocol(protocol));
    }


    private Protocol eventToProtocol(FileSystemEvent fileSystemEvent) {
        Protocol protocol = null;

        switch (fileSystemEvent.event) {
            case FILE_CREATE:
                Protocol.FileCreateRequest fileCreateRequest = new Protocol.FileCreateRequest();
                eventToFileDes(fileCreateRequest.fileDes, fileSystemEvent);
                protocol = fileCreateRequest;
                break;
            case FILE_DELETE:
                Protocol.FileDeleteRequest fileDeleteRequest = new Protocol.FileDeleteRequest();
                eventToFileDes(fileDeleteRequest.fileDes, fileSystemEvent);
                protocol = fileDeleteRequest;
                break;
            case FILE_MODIFY:
                Protocol.FileModifyRequest fileModifyRequest = new Protocol.FileModifyRequest();
                eventToFileDes(fileModifyRequest.fileDes, fileSystemEvent);
                protocol = fileModifyRequest;
                break;
            case DIRECTORY_CREATE:
                Protocol.DirectoryCreateRequest directoryCreateRequest = new Protocol.DirectoryCreateRequest();
                directoryCreateRequest.dirPath.path = fileSystemEvent.pathName;
                protocol = directoryCreateRequest;
                break;
            case DIRECTORY_DELETE:
                Protocol.DirectoryDeleteRequest directoryDeleteRequest = new Protocol.DirectoryDeleteRequest();
                directoryDeleteRequest.dirPath.path = fileSystemEvent.pathName;
                protocol = directoryDeleteRequest;
                break;
        }
        return protocol;
    }

    private void eventToFileDes(ProtocolField.FileDes fileDes, FileSystemEvent fileSystemEvent) {
        fileDes.path = fileSystemEvent.pathName;
        fileDes.md5 = fileSystemEvent.fileDescriptor.md5;
        fileDes.fileSize = fileSystemEvent.fileDescriptor.fileSize;
        fileDes.lastModified = fileSystemEvent.fileDescriptor.lastModified;
    }
}
