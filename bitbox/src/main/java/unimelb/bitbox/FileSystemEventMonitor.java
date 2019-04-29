package unimelb.bitbox;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;


public class FileSystemEventMonitor {

    private FileSystemEvent fileSystemEvent;

    public FileSystemEventMonitor(FileSystemEvent fileSystemEvent) {
        this.fileSystemEvent = fileSystemEvent;
    }

    public void broadcast() {
        String msg = "";

        switch (fileSystemEvent.event) {
            case FILE_CREATE:
                Protocol.FileCreateRequest fileCreateRequest = new Protocol.FileCreateRequest();
                fileCreateRequest.fileDes.md5 = fileSystemEvent.fileDescriptor.md5;
                fileCreateRequest.fileDes.fileSize = fileSystemEvent.fileDescriptor.fileSize;
                fileCreateRequest.fileDes.lastModified = fileSystemEvent.fileDescriptor.lastModified;
                msg = ProtocolFactory.marshalProtocol(fileCreateRequest);
                break;
            case FILE_DELETE:
                Protocol.FileDeleteRequest fileDeleteRequest = new Protocol.FileDeleteRequest();
                fileDeleteRequest.fileDes.md5 = fileSystemEvent.fileDescriptor.md5;
                fileDeleteRequest.fileDes.fileSize = fileSystemEvent.fileDescriptor.fileSize;
                fileDeleteRequest.fileDes.lastModified = fileSystemEvent.fileDescriptor.lastModified;
                msg = ProtocolFactory.marshalProtocol(fileDeleteRequest);
                break;
            case FILE_MODIFY:
                Protocol.FileModifyRequest fileModifyRequest = new Protocol.FileModifyRequest();
                fileModifyRequest.fileDes.md5 = fileSystemEvent.fileDescriptor.md5;
                fileModifyRequest.fileDes.fileSize = fileSystemEvent.fileDescriptor.fileSize;
                fileModifyRequest.fileDes.lastModified = fileSystemEvent.fileDescriptor.lastModified;
                msg = ProtocolFactory.marshalProtocol(fileModifyRequest);
                break;
            case DIRECTORY_CREATE:
                Protocol.DirectoryCreateRequest directoryCreateRequest = new Protocol.DirectoryCreateRequest();
                directoryCreateRequest.dirPath.path = fileSystemEvent.path;
                msg = ProtocolFactory.marshalProtocol(directoryCreateRequest);
                break;
            case DIRECTORY_DELETE:
                Protocol.DirectoryDeleteRequest directoryDeleteRequest = new Protocol.DirectoryDeleteRequest();
                directoryDeleteRequest.dirPath.path = fileSystemEvent.path;
                msg = ProtocolFactory.marshalProtocol(directoryDeleteRequest);
                break;
        }

        ConnectionManager.getInstance().broadcastMsg(msg);
    }

}
