package unimelb.bitbox;


import unimelb.bitbox.protocol.*;
import unimelb.bitbox.util.FileSystemManager;


public class MessageHandler {

    private static FileSystemManager fileSystemManager = null;


    // TODO: may need refator
    // not thread-safe, set this at the initialization stage
    public static void setFileSystemManager(FileSystemManager fsm) {
        fileSystemManager = fsm;
    }

    public static void handleMessage(String message, Connection conn) {

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
                response.response.msg = response.response.status ? "Directory created" : "unknown error";
            }catch (Exception e){
                response.response.status = false;
                response.response.msg = "Failed to create file Error:"+e.getMessage();
            }
        }

        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.FileDeleteRequest fileDeleteRequest, Connection conn) {

    }

    private static void handleSpecificProtocol(Protocol.FileModifyRequest fileModifyRequest, Connection conn) {

    }

    private static void handleSpecificProtocol(Protocol.FileBytesRequest fileBytesRequest, Connection conn) {

    }

    private static void handleSpecificProtocol(Protocol.FileBytesResponse fileBytesResponse, Connection conn) {

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
            response.response.msg = response.response.status ? "Directory created" : "unknown error";
        }

        conn.send(ProtocolFactory.marshalProtocol(response));
    }

    private static void handleSpecificProtocol(Protocol.DirectoryDeleteRequest directoryDeleteRequest, Connection conn) {

    }
}
