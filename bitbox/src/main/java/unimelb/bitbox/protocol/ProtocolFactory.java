package unimelb.bitbox.protocol;


import unimelb.bitbox.Constants;
import unimelb.bitbox.util.Document;

import static unimelb.bitbox.Constants.PROTOCOL_FIELD_CMD;


/**
 * A factory of protocols.
 * Used to convert from JSON string to corresponding protocol class
 * and from protocol class to JSON string
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class ProtocolFactory {

    /**
     * un-marshal parse a JSON string to the corresponding protocol class
     */
    public static Protocol parseProtocol(String json) throws InvalidProtocolException {
        try {

            Document doc = Document.parse(json);
            String command = doc.getString(PROTOCOL_FIELD_CMD);


            ProtocolType protocolType = ProtocolType.typeOfCommand(command);
            Protocol protocol = (Protocol) protocolType.getValue().newInstance();

            protocol.unmarshalFromJson(doc);

            return protocol;
        } catch (Exception e) {
            throw (e instanceof InvalidProtocolException) ? (InvalidProtocolException) e :
                    new InvalidProtocolException("Parse error", e);
        }

    }


    /**
     * marshal protocol class to JSON string
     * @param protocol
     * @return
     */
    public static String marshalProtocol(Protocol protocol) {
        Document doc = new Document();
        doc.append(PROTOCOL_FIELD_CMD, ProtocolType.typeOfProtocol(protocol).getKey());
        protocol.marshalToJson(doc);
        return doc.toJson();
    }

    public static Protocol identifyReq(Protocol protocol){
        String command = ProtocolType.typeOfProtocol(protocol).getKey();
        System.out.println(ProtocolType.typeOfProtocol(protocol).getKey());
        if (protocol instanceof IRequest){
            return protocol;
        } else if (command.equals(Constants.PROTOCOL_TYPE_FILE_CREATE_RESPONSE)){
            Protocol.FileCreateResponse response = (Protocol.FileCreateResponse)protocol;
            Protocol.FileCreateRequest request= new Protocol.FileCreateRequest();
            request.fileDes = response.fileDes;
            return request;
        } else if (command.equals(Constants.PROTOCOL_TYPE_FILE_DELETE_RESPONSE)){
            Protocol.FileDeleteResponse response = (Protocol.FileDeleteResponse)protocol;
            Protocol.FileDeleteRequest request= new Protocol.FileDeleteRequest();
            request.fileDes = response.fileDes;
            return request;
        } else if (command.equals(Constants.PROTOCOL_TYPE_FILE_MODIFY_RESPONSE)){
            Protocol.FileModifyResponse response = (Protocol.FileModifyResponse)protocol;
            Protocol.FileModifyRequest request= new Protocol.FileModifyRequest();
            request.fileDes = response.fileDes;
            return request;
        } else if (command.equals(Constants.PROTOCOL_TYPE_FILE_BYTES_RESPONSE)){
            Protocol.FileBytesResponse response = (Protocol.FileBytesResponse)protocol;
            Protocol.FileBytesRequest request= new Protocol.FileBytesRequest();
            ProtocolField.FilePosition filePosition = new ProtocolField.FilePosition();
            filePosition.len = response.fileContent.len;
            filePosition.pos = response.fileContent.pos;
            request.filePos = filePosition;
            request.fileDes = response.fileDes;
            return request;
        } else if (command.equals(Constants.PROTOCOL_TYPE_DIRECTORY_CREATE_RESPONSE)){
            Protocol.DirectoryCreateResponse response = (Protocol.DirectoryCreateResponse)protocol;
            Protocol.DirectoryCreateRequest request= new Protocol.DirectoryCreateRequest();
            request.dirPath = response.dirPath;
            return request;
        } else if (command.equals(Constants.PROTOCOL_TYPE_DIRECTORY_DELETE_RESPONSE)){
            Protocol.DirectoryDeleteResponse response = (Protocol.DirectoryDeleteResponse)protocol;
            Protocol.DirectoryDeleteRequest request= new Protocol.DirectoryDeleteRequest();
            request.dirPath = response.dirPath;
            return request;
        } else {
            // unhandled protocols:
            //  - INVALID_PROTOCOL
            //  - CONNECTION_REFUSED
            //  - HANDSHAKE_REQUEST
            //  - HANDSHAKE_RESPONSE
            return protocol;
        }
    }
}




