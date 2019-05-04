package unimelb.bitbox;


/**
 * Common constants used in bitbox
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class Constants {

    // Thread pool
    public static final int THREAD_POOL_CORE_POOL_SIZE = 200;
    public static final int THREAD_POOL_MAX_POOL_SZIE = 5000;
    public static final int THREAD_POOL_QUEUE_SIZE= 5000;
    public static final long THREAD_POOL_KEEP_ALIVE_TIME = 2000L;

    // Configurations
    public static final String CONFIG_HOSTNAME_PORT_SEPARATOR = ":";
    public static final String CONFIG_PEERS_SEPARATOR = ",";
    public static final String CONFIG_FIELD_PEERS = "peers";
    public static final String CONFIG_FIELD_PORT = "port";
    public static final String CONFIG_FIELD_AD_NAME = "advertisedName";
    public static final String CONFIG_FIELD_BLOCKSIZE = "blockSize";
    public static final String CONFIG_FIELD_PATH = "path";

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

    // Protocol response messages
    public static final String PROTOCOL_RESPONSE_MESSAGE_INVALID_PATH = "invalid path";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_CREATE_SHORTCUT_USED = "file created successfully using shortcut";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_MODIFY_SHORTCUT_USED = "file modified successfully using shortcut";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_CREATE_LOADER_READY = "file create loader opened";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_MODIFY_LOADER_READY = "file modify loader opened";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_DOES_NOT_EXIST = "file already exists";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_ALREADY_EXISTS = "file already exists";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_ANOTHER_IS_TRANSMITTING = "file with the same path and name is transmitting";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_READ_SUCCESS = "file read successfully";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_READ_FAIL = "file read failed";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_CREATE_FAIL_PREFIX = "failed to create file: ";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_DELETE_FAIL_PREFIX = "failed to delete file: ";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_CREATE_LOADER_COLLABORATING = "file create loader opened: collaborating.";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_MODIFY_LOADER_COLLABORATING = "file modify loader opened: collaborating.";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_DELETE_SUCCESS = "file deleted successfully";
    public static final String PROTOCOL_RESPONSE_MESSAGE_FILE_DELETE_FAIL = "file deleted failed";
    public static final String PROTOCOL_RESPONSE_MESSAGE_DIR_NOT_EXIST = "directory does not exist";
    public static final String PROTOCOL_RESPONSE_MESSAGE_DIR_ALREADY_EXISTS = "directory already exist";
    public static final String PROTOCOL_RESPONSE_MESSAGE_DIR_CREATED = "directory created";
    public static final String PROTOCOL_RESPONSE_MESSAGE_DIR_DELETED = "directory deleted";
    public static final String PROTOCOL_RESPONSE_MESSAGE_DIR_CREATE_FAIL = "failed to create directory";
    public static final String PROTOCOL_RESPONSE_MESSAGE_DIR_DELETE_FAIL = "failed to create directory";
    public static final String PROTOCOL_RESPONSE_MESSAGE_CONNECTION_REFUSED_LIMIT_REACHED = "Incoming connection limit reached";
    public static final String PROTOCOL_RESPONSE_MESSAGE_CONNECTION_REFUSED_ALREADY_EXIST = "Connection with the same hostname and port already exists";
}
