package unimelb.bitbox;


import unimelb.bitbox.protocol.*;
import unimelb.bitbox.util.FileSystemManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class MessageHandler {

    private static FileSystemManager fileSystemManager = null;
    private static Logger log = Logger.getLogger(MessageHandler.class.getName());
    private static ConcurrentHashMap<String, FileLoaderWrapper> fileLoaderWrapperMap = new ConcurrentHashMap<>();


    // not thread-safe, set this at the initialization stage
    public static void setFileSystemManager(FileSystemManager fsm) {
        fileSystemManager = fsm;
    }

    public static void handleMessage(String message, Connection conn) {

        try{
            Protocol protocol = ProtocolFactory.parseProtocol(message);

            ProtocolType protocolType = ProtocolType.typeOfProtocol(protocol);
            switch (protocolType) {
                case INVALID_PROTOCOL:
                    log.warning("received INVALID_PROTOCOL " + conn.getHostPort().toString());
                    conn.close();
                    break;

                case FILE_CREATE_REQUEST:
                    handleSpecificProtocol((Protocol.FileCreateRequest) protocol, conn);
                    break;
                case FILE_DELETE_REQUEST:
                    handleSpecificProtocol((Protocol.FileDeleteRequest) protocol, conn);
                    break;
                case FILE_MODIFY_REQUEST:
                    handleSpecificProtocol((Protocol.FileModifyRequest) protocol, conn);
                    break;
                case FILE_BYTES_REQUEST:
                    handleSpecificProtocol((Protocol.FileBytesRequest) protocol, conn);
                    break;
                case FILE_BYTES_RESPONSE:
                    handleSpecificProtocol((Protocol.FileBytesResponse) protocol, conn);
                    break;
                case DIRECTORY_CREATE_REQUEST:
                    handleSpecificProtocol((Protocol.DirectoryCreateRequest) protocol, conn);
                    break;
                case DIRECTORY_DELETE_REQUEST:
                    handleSpecificProtocol((Protocol.DirectoryDeleteRequest) protocol, conn);
                    break;

                // ignored
                case DIRECTORY_DELETE_RESPONSE:
                case DIRECTORY_CREATE_RESPONSE:
                case FILE_CREATE_RESPONSE:
                case FILE_DELETE_RESPONSE:
                case FILE_MODIFY_RESPONSE:
                    break;

                // invalid
                case CONNECTION_REFUSED:
                case HANDSHAKE_REQUEST:
                case HANDSHAKE_RESPONSE:
                default:
                    throw new InvalidProtocolException("Unexpected command: " + protocol.getClass().getName(), null);
            }

        } catch (InvalidProtocolException e){
            conn.abortWithInvalidProtocol(e.getMessage());
        }

    }

    private static void handleSpecificProtocol(Protocol.FileCreateRequest fileCreateRequest, Connection conn) {

        ProtocolField.FileDes fd = fileCreateRequest.fileDes;
        Protocol.FileCreateResponse response = new Protocol.FileCreateResponse();
        response.fileDes = fd;

        if (!fileSystemManager.isSafePathName(fd.path)) {
            response.response.status = false;
            response.response.msg = "Invalid path";
            conn.send(ProtocolFactory.marshalProtocol(response));
        }

        try{
            FileLoaderWrapper fileLoaderWrapper = fileLoaderWrapperMap.get(fd.path);

            if (fileLoaderWrapper == null) {
                if (fileSystemManager.createFileLoader(fd.path, fd.md5, fd.fileSize, fd.lastModified)) {
                    response.response.status = true;
                    if (fileSystemManager.checkShortcut(fd.path)) {
                        response.response.msg = "File created (shortcut)";
                    } else {
                        response.response.msg = "File create loader opened";
                        conn.send(ProtocolFactory.marshalProtocol(response));
                        fileLoaderWrapper = new FileLoaderWrapper(fd, fileSystemManager, conn);
                        fileLoaderWrapperMap.put(fd.path, fileLoaderWrapper);
                        return;
                    }
                } else {
                    response.response.status = false;
                    response.response.msg = "File already exists";
                }

            } else {
                if (fileLoaderWrapper.checkMd5(fd.md5)) {
                    response.response.status = true;
                    response.response.msg = "File create loader opened (share)";
                    conn.send(ProtocolFactory.marshalProtocol(response));
                    fileLoaderWrapper.addNewConnection(fd, conn);
                    return;
                } else {
                    response.response.status = false;
                    response.response.msg = "File conflict";
                }
            }
        }catch (IOException | NoSuchAlgorithmException e){
            response.response.status = false;
            response.response.msg = "Failed to create file Error:"+e.getMessage();
        }
        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.FileDeleteRequest fileDeleteRequest, Connection conn) {

        Protocol.FileDeleteResponse response = new Protocol.FileDeleteResponse();
        response.fileDes = fileDeleteRequest.fileDes;
        ProtocolField.FileDes fd = fileDeleteRequest.fileDes;

        if (fileSystemManager.isSafePathName(fd.path)) {
            if (!fileSystemManager.fileNameExists(fd.path)) {
                response.response.status = false;
                response.response.msg = "Can't find the specified file";
            } else {
                try{
                    response.response.status = fileSystemManager.deleteFile(fd.path,fd.lastModified,fd.md5);
                    response.response.msg = response.response.status ? "File Deleted" : "unknown error";
                }catch (Exception e){
                    response.response.status = false;
                    response.response.msg = "Failed to delete file Error:"+e.getMessage();
                }
            }
        } else {
            response.response.status = false;
            response.response.msg = "invalid path";
        }

        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.FileModifyRequest fileModifyRequest, Connection conn) {
        ProtocolField.FileDes fd = fileModifyRequest.fileDes;
        Protocol.FileCreateResponse response = new Protocol.FileCreateResponse();
        response.fileDes = fd;

        if (!fileSystemManager.isSafePathName(fd.path)) {
            response.response.status = false;
            response.response.msg = "Invalid path";
            conn.send(ProtocolFactory.marshalProtocol(response));
            return;
        }

        try{
            FileLoaderWrapper fileLoaderWrapper = fileLoaderWrapperMap.get(fd.path);

            if (fileLoaderWrapper == null) {
                if (fileSystemManager.modifyFileLoader(fd.path, fd.md5, fd.fileSize, fd.lastModified)) {
                    response.response.status = true;
                    if (fileSystemManager.checkShortcut(fd.path)) {
                        response.response.msg = "File modified (shortcut)";
                    } else {
                        response.response.msg = "File modified loader opened";
                        conn.send(ProtocolFactory.marshalProtocol(response));
                        fileLoaderWrapper = new FileLoaderWrapper(fd, fileSystemManager, conn);
                        fileLoaderWrapperMap.put(fd.path, fileLoaderWrapper);
                        return;
                    }
                } else {
                    response.response.status = false;
                    response.response.msg = "Don't have the original file";
                }

            } else {
                if (fileLoaderWrapper.checkMd5(fd.md5)) {
                    response.response.status = true;
                    response.response.msg = "File modified loader opened (share)";
                    conn.send(ProtocolFactory.marshalProtocol(response));
                    fileLoaderWrapper.addNewConnection(fd, conn);
                    return;
                } else {
                    response.response.status = false;
                    response.response.msg = "File conflict";
                }
            }
        }catch (IOException | NoSuchAlgorithmException e){
            response.response.status = false;
            response.response.msg = "Failed to create file Error: " + e.getMessage();
        }
        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.FileBytesRequest fileBytesRequest, Connection conn) {
        Protocol.FileBytesResponse response = new Protocol.FileBytesResponse();
        response.fileDes = fileBytesRequest.fileDes;
        response.fileContent.len = fileBytesRequest.filePos.len;
        response.fileContent.pos = fileBytesRequest.filePos.pos;

        // discard the message if the path not safe;
        if (!fileSystemManager.isSafePathName(fileBytesRequest.fileDes.path)) {
            response.response.status = false;
            response.response.msg = "Invalid path";
            conn.send(ProtocolFactory.marshalProtocol(response));
            return;
        }

        ProtocolField.FileDes fd = fileBytesRequest.fileDes;
        ProtocolField.FilePosition fp = fileBytesRequest.filePos;
        ByteBuffer byteBuffer = null;

        try {
            byteBuffer = fileSystemManager.readFile(fd.md5, fp.pos, fp.len);
        } catch (NoSuchAlgorithmException e) {
            log.severe(e.toString());
        } catch (IOException e) {
            log.warning(e.toString());
        }

        if (byteBuffer != null) {
            // send the bytes successfully
            response.fileContent.content = Base64.getEncoder().encodeToString(byteBuffer.array());
            response.response.status = true;
            response.response.msg = "successful read";
            conn.send(ProtocolFactory.marshalProtocol(response));
            return;
        }

        response.response.status = false;
        response.response.msg = "unsuccessful read";
        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.FileBytesResponse fileBytesResponse, Connection conn) {

        String filePath = fileBytesResponse.fileDes.path;

        if (fileBytesResponse.response.status) {
            FileLoaderWrapper fileLoaderWrapper = fileLoaderWrapperMap.get(filePath);

            if (fileLoaderWrapper != null) {
                fileLoaderWrapper.received(fileBytesResponse, conn);
            }
        }
    }

    private static void handleSpecificProtocol(Protocol.DirectoryCreateRequest directoryCreateRequest, Connection conn) {

        Protocol.DirectoryCreateResponse response = new Protocol.DirectoryCreateResponse();
        response.dirPath = directoryCreateRequest.dirPath;

        String path = directoryCreateRequest.dirPath.path;

        if (fileSystemManager.isSafePathName(path)) {
            if (fileSystemManager.dirNameExists(path)) {
                response.response.status = false;
                response.response.msg = "Directory already exists";
            } else {
                response.response.status = fileSystemManager.makeDirectory(path);
                response.response.msg = response.response.status ? "Directory created" : "Unknown error";
            }
        } else {
            response.response.status = false;
            response.response.msg = "Invalid path";
        }

        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.DirectoryDeleteRequest directoryDeleteRequest, Connection conn) {

        Protocol.DirectoryDeleteResponse response = new Protocol.DirectoryDeleteResponse();
        response.dirPath = directoryDeleteRequest.dirPath;

        String path = directoryDeleteRequest.dirPath.path;

        if (fileSystemManager.isSafePathName(path)) {
            if (!fileSystemManager.dirNameExists(path)) {
                response.response.status = false;
                response.response.msg = "Can't find specified directory";
            } else {
                response.response.status = fileSystemManager.deleteDirectory(path);
                response.response.msg = response.response.status ? "Directory deleted" : "Unknown error";
            }
        } else {
            response.response.status = false;
            response.response.msg = "Invalid path";
        }

        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    public static void removeFileLoaderWrapper(FileLoaderWrapper fileLoaderWrapper, String filePath) {
        fileLoaderWrapperMap.remove(filePath, fileLoaderWrapper);
    }

    public static void cleanUpFileLoaderWrapper() {
        // thread-safe, the iterator is a snapshot
        for(Map.Entry<String, FileLoaderWrapper> entry : fileLoaderWrapperMap.entrySet()) {
            entry.getValue().clean();
        }
    }
}
