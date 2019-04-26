package unimelb.bitbox;

public class Constants {

    // Configurations
    public static final String CONFIG_HOSTNAME_PORT_SEPARATOR=":";
    public static final String CONFIG_PEERS_SEPARATOR=",";
    public static final String CONFIG_FIELD_PEERS ="peers";
    public static final String CONFIG_FIELD_PORT="port";
    public static final String CONFIG_FIEDL_AD_NAME= "advertisedName";

    // Protocol fields
    public static final String PROTOCOL_FIELD_CMD = "command";
    public static final String PROTOCOL_FIELD_MSG = "message";
    public static final String PROTOCOL_FIELD_PEER = "peers";
    public static final String PROTOCOL_FIELD_HOST = "host";
    public static final String PROTOCOL_FIELD_PORT = "port";
    public static final String PROTOCOL_FIELD_HOST_PORT = "hostPort";
    public static final String PROTOCOL_FIELD_FILE_DES = "fileDescriptor";
    public static final String PROTOCOL_FIELD_MD5 = "md5";
    public static final String PROTOCOL_FIELD_LAST_MODIFIED = "lastModified";
    public static final String PROTOCOL_FIELD_FILE_SIZE = "fileSize";
    public static final String PROTOCOL_FIELD_PATH_NAME = "pathName";
    public static final String PROTOCOL_FIELD_STATUS = "status";
    public static final String PROTOCOL_FIELD_POSITION = "position";
    public static final String PROTOCOL_FIELD_LENGTH = "length";
    public static final String PROTOCOL_FIELD_CONTENT = "content";

    // Protocol types
    public static final String PROTOCOL_TYPE_INVALID_PROTOCOL = "INVALID_PROTOCOL";
    public static final String PROTOCOL_TYPE_CONNECTION_REFUSED = "CONNECTION_REFUSED";
    public static final String PROTOCOL_TYPE_HANDSHAKE_REQUEST = "HANDSHAKE_REQUEST";
    public static final String PROTOCOL_TYPE_HANDSHAKE_RESPONSE = "HANDSHAKE_RESPONSE";
    public static final String PROTOCOL_TYPE_FILE_CREATE_REQUEST = "FILE_CREATE_REQUEST";
    public static final String PROTOCOL_TYPE_FILE_CREATE_RESPONSE = "FILE_CREATE_RESPONSE";
    public static final String PROTOCOL_TYPE_FILE_DELETE_REQUEST = "FILE_DELETE_REQUEST";
    public static final String PROTOCOL_TYPE_FILE_DELETE_RESPONSE = "FILE_DELETE_RESPONSE";
    public static final String PROTOCOL_TYPE_FILE_MODIFY_REQUEST = "FILE_MODIFY_REQUEST";
    public static final String PROTOCOL_TYPE_FILE_MODIFY_RESPONSE = "FILE_MODIFY_RESPONSE";
    public static final String PROTOCOL_TYPE_FILE_BYTES_REQUEST = "FILE_BYTES_REQUEST";
    public static final String PROTOCOL_TYPE_FILE_BYTES_RESPONSE = "FILE_BYTES_RESPONSE";
    public static final String PROTOCOL_TYPE_DIRECTORY_CREATE_REQUEST = "DIRECTORY_CREATE_REQUEST";
    public static final String PROTOCOL_TYPE_DIRECTORY_CREATE_RESPONSE = "DIRECTORY_CREATE_RESPONSE";
    public static final String PROTOCOL_TYPE_DIRECTORY_DELETE_REQUEST = "DIRECTORY_DELETE_REQUEST";
    public static final String PROTOCOL_TYPE_DIRECTORY_DELETE_RESPONSE = "DIRECTORY_DELETE_RESPONSE";
}
