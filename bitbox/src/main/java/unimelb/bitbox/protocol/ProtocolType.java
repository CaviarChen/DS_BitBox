package unimelb.bitbox.protocol;

public enum ProtocolType{
    INVALID_PROTOCOL("INVALID_PROTOCOL", Protocol.InvalidProtocol.class),
    CONNECTION_REFUSED("CONNECTION_REFUSED", Protocol.ConnectionRefused.class),
    HANDSHAKE_REQUEST("HANDSHAKE_REQUEST", Protocol.HandshakeRequest.class),
    HANDSHAKE_RESPONSE("HANDSHAKE_RESPONSE", Protocol.HandshakeResponse.class),
    FILE_CREATE_REQUEST("FILE_CREATE_REQUEST", Protocol.FileCreateRequest.class),
    FILE_CREATE_RESPONSE("FILE_CREATE_RESPONSE", Protocol.FileCreateResponse.class);


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
