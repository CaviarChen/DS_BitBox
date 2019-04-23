package unimelb.bitbox.protocol;

public enum ProtocolType{
    INVALID_PROTOCOL("INVALID_PROTOCOL", Protocol.InvalidProtocol.class),
    CONNECTION_REFUSED("CONNECTION_REFUSED", Protocol.ConnectionRefused.class),
    HANDSHAKE_REQUEST("HANDSHAKE_REQUEST", Protocol.HandshakeRequest.class),
    HANDSHAKE_RESPONSE("HANDSHAKE_RESPONSE", Protocol.HandshakeResponse.class),
    FILE_CREATE_REQUEST("FILE_CREATE_REQUEST", Protocol.FileCreateRequest.class),
    FILE_CREATE_RESPONSE("FILE_CREATE_RESPONSE", Protocol.FileCreateResponse.class),
    FILE_DELETE_REQUEST("FILE_DELETE_REQUEST", Protocol.FileDeleteRequest.class),
    FILE_DELETE_RESPONSE("FILE_DELETE_RESPONSE", Protocol.FileDeleteResponse.class),
    FILE_MODIFY_REQUEST("FILE_MODIFY_REQUEST", Protocol.FileModifyRequest.class),
    FILE_MODIFY_RESPONSE("FILE_MODIFY_RESPONSE", Protocol.FileModifyResponse.class),
    FILE_BYTES_REQUEST("FILE_BYTES_REQUEST", Protocol.FileBytesRequest.class),
    FILE_BYTES_RESPONSE("FILE_BYTES_RESPONSE", Protocol.FileBytesResponse.class),
    DIRECTORY_CREATE_REQUEST("DIRECTORY_CREATE_REQUEST", Protocol.DirectoryCreateRequest.class),
    DIRECTORY_CREATE_RESPONSE("DIRECTORY_CREATE_RESPONSE", Protocol.DirectoryCreateResponse.class),
    DIRECTORY_DELETE_REQUEST("DIRECTORY_DELETE_REQUEST", Protocol.DirectoryDeleteRequest.class),
    DIRECTORY_DELETE_RESPONSE("DIRECTORY_DELETE_RESPONSE", Protocol.DirectoryDeleteResponse.class);



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

    public static ProtocolType typeOfCommand(String command) {
        for (ProtocolType e : values()) {
            if (e.key.equals(command)) {
                return e;
            }
        }
        return null;
    }

    public static ProtocolType typeOfProtocol(Protocol protocol) {
        for (ProtocolType e : values()) {
            if (e.value == protocol.getClass()) {
                return e;
            }
        }
        return null;
    }
}
