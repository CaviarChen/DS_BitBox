package unimelb.bitbox;

import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;
import unimelb.bitbox.util.FileSystemObserver;


public class FileSystemEventMonitor implements FileSystemObserver {

    @Override
    public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
        Protocol protocol = null;

        switch (fileSystemEvent.event) {
            case FILE_CREATE:
                Protocol.FileCreateRequest fileCreateRequest = new Protocol.FileCreateRequest();
                fileCreateRequest.fileDes.md5 = fileSystemEvent.fileDescriptor.md5;
                fileCreateRequest.fileDes.fileSize = fileSystemEvent.fileDescriptor.fileSize;
                fileCreateRequest.fileDes.lastModified = fileSystemEvent.fileDescriptor.lastModified;
                protocol = fileCreateRequest;
                break;
            case FILE_DELETE:
                Protocol.FileDeleteRequest fileDeleteRequest = new Protocol.FileDeleteRequest();
                fileDeleteRequest.fileDes.md5 = fileSystemEvent.fileDescriptor.md5;
                fileDeleteRequest.fileDes.fileSize = fileSystemEvent.fileDescriptor.fileSize;
                fileDeleteRequest.fileDes.lastModified = fileSystemEvent.fileDescriptor.lastModified;
                protocol = fileDeleteRequest;
                break;
            case FILE_MODIFY:
                Protocol.FileModifyRequest fileModifyRequest = new Protocol.FileModifyRequest();
                fileModifyRequest.fileDes.md5 = fileSystemEvent.fileDescriptor.md5;
                fileModifyRequest.fileDes.fileSize = fileSystemEvent.fileDescriptor.fileSize;
                fileModifyRequest.fileDes.lastModified = fileSystemEvent.fileDescriptor.lastModified;
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
        ConnectionManager.getInstance().broadcastMsg(ProtocolFactory.marshalProtocol(protocol));
    }
}
