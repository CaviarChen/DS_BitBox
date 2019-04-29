package unimelb.bitbox;


import unimelb.bitbox.protocol.*;
import unimelb.bitbox.protocol.ProtocolType.*;


public class MessageHandler {
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

    }

    private static void handleSpecificProtocol(Protocol.DirectoryDeleteRequest directoryDeleteRequest, Connection conn) {

    }
}
