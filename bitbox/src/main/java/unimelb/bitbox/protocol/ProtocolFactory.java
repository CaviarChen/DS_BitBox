package unimelb.bitbox.protocol;


import unimelb.bitbox.Constants;
import unimelb.bitbox.util.Document;



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
            String command = doc.getString(Constants.PROTOCOL_FIELD_CMD);


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
        doc.append(Constants.PROTOCOL_FIELD_CMD, ProtocolType.typeOfProtocol(protocol).getKey());
        protocol.marshalToJson(doc);
        return doc.toJson();
    }

    public static IRequest identifyRes(IResponse protocol){
        switch (ProtocolType.typeOfProtocol((Protocol) protocol)) {
            case FILE_CREATE_RESPONSE:
                Protocol.FileCreateResponse fileCreateResponse = (Protocol.FileCreateResponse) protocol;
                Protocol.FileCreateRequest fileCreateRequest = new Protocol.FileCreateRequest();
                fileCreateRequest.fileDes = fileCreateResponse.fileDes;
                return fileCreateRequest;
            case FILE_DELETE_RESPONSE:
                Protocol.FileDeleteResponse fileDeleteResponse = (Protocol.FileDeleteResponse) protocol;
                Protocol.FileDeleteRequest fileDeleteRequest = new Protocol.FileDeleteRequest();
                fileDeleteRequest.fileDes = fileDeleteResponse.fileDes;
                return fileDeleteRequest;
            case FILE_MODIFY_RESPONSE:
                Protocol.FileModifyResponse fileModifyResponse = (Protocol.FileModifyResponse) protocol;
                Protocol.FileModifyRequest fileModifyRequest= new Protocol.FileModifyRequest();
                fileModifyRequest.fileDes = fileModifyResponse.fileDes;
                return fileModifyRequest;
            case FILE_BYTES_RESPONSE:
                Protocol.FileBytesResponse fileBytesResponse = (Protocol.FileBytesResponse) protocol;
                Protocol.FileBytesRequest fileBytesRequest = new Protocol.FileBytesRequest();
                ProtocolField.FilePosition filePosition = new ProtocolField.FilePosition();
                filePosition.len = fileBytesResponse.fileContent.len;
                filePosition.pos = fileBytesResponse.fileContent.pos;
                fileBytesRequest.filePos = filePosition;
                fileBytesRequest.fileDes = fileBytesResponse.fileDes;
                return fileBytesRequest;
            case DIRECTORY_CREATE_RESPONSE:
                Protocol.DirectoryCreateResponse directoryCreateResponse = (Protocol.DirectoryCreateResponse) protocol;
                Protocol.DirectoryCreateRequest directoryCreateRequest= new Protocol.DirectoryCreateRequest();
                directoryCreateRequest.dirPath = directoryCreateResponse.dirPath;
                return directoryCreateRequest;
            case DIRECTORY_DELETE_RESPONSE:
                Protocol.DirectoryDeleteResponse directoryDeleteResponse = (Protocol.DirectoryDeleteResponse) protocol;
                Protocol.DirectoryDeleteRequest directoryDeleteRequest= new Protocol.DirectoryDeleteRequest();
                directoryDeleteRequest.dirPath = directoryDeleteResponse.dirPath;
                return directoryDeleteRequest;
        }
        return null;
    }
}




