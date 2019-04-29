package unimelb.bitbox;


import unimelb.bitbox.protocol.*;
import unimelb.bitbox.util.FileSystemManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;


public class MessageHandler {

    private static FileSystemManager fileSystemManager = null;
    private static Logger log = Logger.getLogger(MessageHandler.class.getName());


    // TODO: may need refator
    // not thread-safe, set this at the initialization stage
    public static void setFileSystemManager(FileSystemManager fsm) {
        fileSystemManager = fsm;
    }

    public static void handleMessage(String message, Connection conn) {

        try{
            Protocol protocol = ProtocolFactory.parseProtocol(message);

            ProtocolType protocolType = ProtocolType.typeOfProtocol(protocol);
            if (protocolType != null) {
                switch (protocolType) {
                    case INVALID_PROTOCOL:
                        // log then disconnect (?)
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


                    // log then ignored
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
                        break;
                }
            }
        }catch (InvalidProtocolException e){
            //Todo:send invalid protocol
        }

    }

    private static void handleSpecificProtocol(Protocol.FileCreateRequest fileCreateRequest, Connection conn) {

        Protocol.FileCreateResponse response = new Protocol.FileCreateResponse();
        response.fileDes = fileCreateRequest.fileDes;

        ProtocolField.FileDes fd = fileCreateRequest.fileDes;
        if (fileSystemManager.fileNameExists(fd.path)) {
            response.response.status = false;
            response.response.msg = "File already exists";
        } else {
            try{
                response.response.status = fileSystemManager.createFileLoader(fd.path,fd.md5,fd.fileSize,fd.lastModified);
                response.response.msg = response.response.status ? "File created" : "unknown error";
            }catch (Exception e){
                response.response.status = false;
                response.response.msg = "Failed to create file Error:"+e.getMessage();
            }
        }

        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.FileDeleteRequest fileDeleteRequest, Connection conn) {

        Protocol.FileDeleteResponse response = new Protocol.FileDeleteResponse();
        response.fileDes = fileDeleteRequest.fileDes;

        ProtocolField.FileDes fd = fileDeleteRequest.fileDes;
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

        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.FileModifyRequest fileModifyRequest, Connection conn) {

        Protocol.FileModifyResponse response = new Protocol.FileModifyResponse();
        response.fileDes = fileModifyRequest.fileDes;

        ProtocolField.FileDes fd = fileModifyRequest.fileDes;
        if (!fileSystemManager.fileNameExists(fd.path)) {
            response.response.status = false;
            response.response.msg = "Can't find the specified file";
        } else {
            try{
                response.response.status = fileSystemManager.modifyFileLoader(fd.path,fd.md5,fd.lastModified);
                response.response.msg = response.response.status ? "File modified" : "unknown error";
            }catch (Exception e){
                response.response.status = false;
                response.response.msg = "Failed to modify file Error:"+e.getMessage();
            }
        }

        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.FileBytesRequest fileBytesRequest, Connection conn) {

        // discard the message if the path not safe;
        if (!fileSystemManager.isSafePathName(fileBytesRequest.fileDes.path)) {
            return ;
        }

        Protocol.FileBytesResponse response = new Protocol.FileBytesResponse();
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
            return ;
        }

        response.response.status = false;
        response.response.msg = "unsuccessful read";
        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.FileBytesResponse fileBytesResponse, Connection conn) {

        String filePath = fileBytesResponse.fileDes.path;

        // discard the message if the path not safe
        if (!fileSystemManager.isSafePathName(filePath)) {
            return ;
        }

        if (fileBytesResponse.response.status) {
            // write to file
            ProtocolField.FileContent fc = fileBytesResponse.fileContent;
            ByteBuffer byteBuffer = ByteBuffer.allocate((int)fc.len);
            byteBuffer.put(Base64.getDecoder().decode(fc.content));

            try {
                fileSystemManager.writeFile(filePath, byteBuffer, fc.pos);
            } catch (IOException e) {
                log.warning(e.toString());
            }

            // check complete
            boolean isComplete = false;
            try {
                isComplete = fileSystemManager.checkWriteComplete(filePath);
            } catch (NoSuchAlgorithmException e) {
                log.severe(e.toString());
            } catch (IOException e) {
                log.warning(e.toString());
            }

            if (!isComplete) {
                //TODO: monitor missing part of the file after long time, retry?
            }
        } else {
            //TODO: retry after a few seconds
        }
    }

    private static void handleSpecificProtocol(Protocol.DirectoryCreateRequest directoryCreateRequest, Connection conn) {

        Protocol.DirectoryCreateResponse response = new Protocol.DirectoryCreateResponse();
        response.dirPath = directoryCreateRequest.dirPath;

        String path = directoryCreateRequest.dirPath.path;
        if (fileSystemManager.dirNameExists(path)) {
            response.response.status = false;
            response.response.msg = "Directory already exists";
        } else {
            response.response.status = fileSystemManager.makeDirectory(path);
            response.response.msg = response.response.status ? "Directory created" : "Unknown error";
        }

        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.DirectoryDeleteRequest directoryDeleteRequest, Connection conn) {

        Protocol.DirectoryDeleteResponse response = new Protocol.DirectoryDeleteResponse();
        response.dirPath = directoryDeleteRequest.dirPath;

        String path = directoryDeleteRequest.dirPath.path;
        if (!fileSystemManager.dirNameExists(path)) {
            response.response.status = false;
            response.response.msg = "Can't find specified directory";
        } else {
            response.response.status = fileSystemManager.deleteDirectory(path);
            response.response.msg = response.response.status ? "Directory deleted" : "Unknown error";
        }

        conn.send(ProtocolFactory.marshalProtocol(response));
    }
}
