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
                    break;
                case CONNECTION_REFUSED:
                    break;
                case HANDSHAKE_REQUEST:
                    break;
                case HANDSHAKE_RESPONSE:
                    break;
                case FILE_CREATE_REQUEST:
                    break;
                case FILE_CREATE_RESPONSE:
                    break;
                case FILE_DELETE_REQUEST:
                    break;
                case FILE_DELETE_RESPONSE:
                    break;
                case FILE_MODIFY_REQUEST:
                    break;
                case FILE_MODIFY_RESPONSE:
                    break;
                case FILE_BYTES_REQUEST:
                    break;
                case FILE_BYTES_RESPONSE:
                    break;
                case DIRECTORY_CREATE_REQUEST:
                    break;
                case DIRECTORY_CREATE_RESPONSE:
                    break;
                case DIRECTORY_DELETE_REQUEST:
                    break;
                case DIRECTORY_DELETE_RESPONSE:
                    break;
                default:
                    break;
            }
        }
    }
}
