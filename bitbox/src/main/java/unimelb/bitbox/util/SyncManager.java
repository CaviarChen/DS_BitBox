package unimelb.bitbox.util;


import unimelb.bitbox.util.ConnectionUtils.TCPConnection;
import unimelb.bitbox.util.ConnectionUtils.ConnectionManager;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolField;
import unimelb.bitbox.util.FileSystem.FileSystemManager;
import unimelb.bitbox.util.FileSystem.FileSystemManager.FileSystemEvent;

import java.util.logging.Logger;


/**
 * Sync Manager used to generate and send sync events
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class SyncManager {
    private static SyncManager instance = new SyncManager();


    public static SyncManager getInstance() {
        return instance;
    }


    private FileSystemManager fileSystemManager = null;
    private static Logger log = Logger.getLogger(SyncManager.class.getName());


    private SyncManager() {
    }


    /**
     * initialize this manager
     * @param fileSystemManager
     */
    public void init(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }


    /**
     * Sync everything with all the peers
     * Async method
     */
    public void syncWithAllAsync() {
        log.info("Sync with all");
        for (FileSystemEvent event : fileSystemManager.generateSyncEvents()) {
            sendEventToAllAsync(event);
        }
    }

    /**
     * Sync everything with a given peer
     * Async method
     * @param conn the connection of the given peer
     */
    public void syncWithOneAsync(TCPConnection conn) {
        log.info("Sync with: " + conn.getHostPort().toString());

        for (FileSystemEvent event : fileSystemManager.generateSyncEvents()) {
            sendEventToOneAsync(event, conn);
        }
    }


    /**
     * Send a given fileSystemEvent to all the peers
     * Async method
     * @param fileSystemEvent the given fileSystemEvent
     */
    public void sendEventToAllAsync(FileSystemEvent fileSystemEvent) {
        Protocol protocol = eventToProtocol(fileSystemEvent);
        ConnectionManager.getInstance().broadcastMsgAsync(ProtocolFactory.marshalProtocol(protocol));
    }

    // Send a given fileSystemEvent to a given peer
    private void sendEventToOneAsync(FileSystemEvent fileSystemEvent, TCPConnection conn) {
        Protocol protocol = eventToProtocol(fileSystemEvent);
        conn.sendAsync(ProtocolFactory.marshalProtocol(protocol));
    }

    // generate a message using the given fileSystemEvent
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

    // fill ProtocolField.FileDes based on the given event
    private void eventToFileDes(ProtocolField.FileDes fileDes, FileSystemEvent fileSystemEvent) {
        fileDes.path = fileSystemEvent.pathName;
        fileDes.md5 = fileSystemEvent.fileDescriptor.md5;
        fileDes.fileSize = fileSystemEvent.fileDescriptor.fileSize;
        fileDes.lastModified = fileSystemEvent.fileDescriptor.lastModified;
    }
}
