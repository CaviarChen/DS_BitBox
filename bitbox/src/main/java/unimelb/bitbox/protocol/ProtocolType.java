package unimelb.bitbox.protocol;


import unimelb.bitbox.Constants;


/**
 * ProtocolType contains all the protocols class type
 *
 * @author Wenqing Xue (813044)
 * @author Weizhi Xu (752454)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public enum ProtocolType {
    INVALID_PROTOCOL(Constants.PROTOCOL_TYPE_INVALID_PROTOCOL, Protocol.InvalidProtocol.class),
    CONNECTION_REFUSED(Constants.PROTOCOL_TYPE_CONNECTION_REFUSED, Protocol.ConnectionRefused.class),
    HANDSHAKE_REQUEST(Constants.PROTOCOL_TYPE_HANDSHAKE_REQUEST, Protocol.HandshakeRequest.class),
    HANDSHAKE_RESPONSE(Constants.PROTOCOL_TYPE_HANDSHAKE_RESPONSE, Protocol.HandshakeResponse.class),
    FILE_CREATE_REQUEST(Constants.PROTOCOL_TYPE_FILE_CREATE_REQUEST, Protocol.FileCreateRequest.class),
    FILE_CREATE_RESPONSE(Constants.PROTOCOL_TYPE_FILE_CREATE_RESPONSE, Protocol.FileCreateResponse.class),
    FILE_DELETE_REQUEST(Constants.PROTOCOL_TYPE_FILE_DELETE_REQUEST, Protocol.FileDeleteRequest.class),
    FILE_DELETE_RESPONSE(Constants.PROTOCOL_TYPE_FILE_DELETE_RESPONSE, Protocol.FileDeleteResponse.class),
    FILE_MODIFY_REQUEST(Constants.PROTOCOL_TYPE_FILE_MODIFY_REQUEST, Protocol.FileModifyRequest.class),
    FILE_MODIFY_RESPONSE(Constants.PROTOCOL_TYPE_FILE_MODIFY_RESPONSE, Protocol.FileModifyResponse.class),
    FILE_BYTES_REQUEST(Constants.PROTOCOL_TYPE_FILE_BYTES_REQUEST, Protocol.FileBytesRequest.class),
    FILE_BYTES_RESPONSE(Constants.PROTOCOL_TYPE_FILE_BYTES_RESPONSE, Protocol.FileBytesResponse.class),
    DIRECTORY_CREATE_REQUEST(Constants.PROTOCOL_TYPE_DIRECTORY_CREATE_REQUEST, Protocol.DirectoryCreateRequest.class),
    DIRECTORY_CREATE_RESPONSE(Constants.PROTOCOL_TYPE_DIRECTORY_CREATE_RESPONSE, Protocol.DirectoryCreateResponse.class),
    DIRECTORY_DELETE_REQUEST(Constants.PROTOCOL_TYPE_DIRECTORY_DELETE_REQUEST, Protocol.DirectoryDeleteRequest.class),
    DIRECTORY_DELETE_RESPONSE(Constants.PROTOCOL_TYPE_DIRECTORY_DELETE_RESPONSE, Protocol.DirectoryDeleteResponse.class);


    private final String key;
    private final Class value;


    ProtocolType(String key, Class value) {
        this.key = key;
        this.value = value;
    }


    public String getKey() {
        return key;
    }


    public Class getValue() {
        return value;
    }


    /**
     * Get the protocol type of the given command
     * @param command the command string to match
     * @return a corresponding protocol type
     * @throws InvalidProtocolException the command does not match any of the protocol in the system
     */
    public static ProtocolType typeOfCommand(String command) throws InvalidProtocolException {
        for (ProtocolType e : values()) {
            if (e.key.equals(command)) {
                return e;
            }
        }
        throw new InvalidProtocolException("Unknown command: " + command, null);
    }


    /**
     * Get the protocol type from a protocol
     * @param protocol a protocol
     * @return protocol type
     */
    public static ProtocolType typeOfProtocol(Protocol protocol) {
        for (ProtocolType e : values()) {
            if (e.value == protocol.getClass()) {
                return e;
            }
        }
        // shouldn't happen
        throw new RuntimeException("Unknown protocol class");
    }
}
