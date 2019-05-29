package unimelb.bitbox.protocol;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.SecManager;

import static unimelb.bitbox.Constants.PROTOCOL_FIELD_CMD;


/**
 * A factory of client protocols.
 * Used to convert from JSON string to corresponding protocol class
 * and from protocol class to JSON string
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class ClientProtocolFactory {

    /**
     * un-marshal parse a JSON string to the corresponding protocol class
     */
    public static ClientProtocol parseProtocol(String json) throws InvalidProtocolException {
        try {

            Document doc = Document.parse(json);

            boolean isEncrypted = !doc.containsKey(PROTOCOL_FIELD_CMD) && doc.containsKey("payload");
            if (isEncrypted) {
                // encrypted
                String payload = SecManager.getInstance().decryptPayload(doc.getString("payload"));
                doc = Document.parse(payload);
            }

            // unencrypted
            String command = doc.getString(PROTOCOL_FIELD_CMD);
            ClientProtocolType protocolType = ClientProtocolType.typeOfCommand(command);

            if (protocolType.isNeedEncryption() != isEncrypted) {
                throw new InvalidProtocolException("Security protocol does not match", null);
            }

            ClientProtocol protocol = (ClientProtocol) protocolType.getValue().newInstance();
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
    public static String marshalProtocol(ClientProtocol protocol) throws Exception {

        ClientProtocolType protocolType = ClientProtocolType.typeOfProtocol(protocol);
        Document doc = new Document();
        doc.append(PROTOCOL_FIELD_CMD, protocolType.getKey());
        protocol.marshalToJson(doc);
        String msg = doc.toJson();

        if (protocolType.isNeedEncryption()) {
            msg = SecManager.getInstance().encryptJSON(msg);
            doc = new Document();
            doc.append("payload", msg);
            msg = doc.toJson();
        }

        return msg;
    }

    public static void validateProtocolType(ClientProtocol protocol, ClientProtocolType clientProtocolType) throws Exception {
        ClientProtocolType protocolType = ClientProtocolType.typeOfProtocol(protocol);

        if (!protocolType.equals(clientProtocolType)) {
            throw new Exception("Protocol Type not matched: " + "expected: " + clientProtocolType + "actual: " + protocolType);
        }
    }

}