package unimelb.bitbox.protocol;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static unimelb.bitbox.Constants.*;
import static unimelb.bitbox.Constants.PROTOCOL_FIELD_PORT;

/**
 * ClientProtocol construct all client protocols in the system.
 * Using reflection to get all related fields value.
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class ClientProtocol implements IProtocol{
    @Override
    public void unmarshalFromJson(Document doc) {
        for (ProtocolField protocolField : getAllProtocolFields()) {
            protocolField.unmarshalFromJson(doc);
        }
    }


    @Override
    public void marshalToJson(Document doc) {
        for (ProtocolField protocolField : getAllProtocolFields()) {
            protocolField.marshalToJson(doc);
        }
    }


    public static class AuthRequest extends ClientProtocol {
        public ProtocolField.AuthIdentity authIdentity = new ProtocolField.AuthIdentity();
    }

    public static class AuthResponse extends ClientProtocol {
        public ProtocolField.Response response = new ProtocolField.Response();
        public ProtocolField.AuthKey authKey = new ProtocolField.AuthKey();
    }

    public static class ListPeersRequest extends ClientProtocol  {
    }

    public static class ListPeersResponse extends ClientProtocol {
        public ProtocolField.Peers peers = new ProtocolField.Peers();
    }

    public static class ConnectPeerRequest extends ClientProtocol {
        public HostPort hostPort = new HostPort();

        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            this.hostPort = new HostPort(doc.getString(PROTOCOL_FIELD_HOST),
                    (int)doc.getLong(PROTOCOL_FIELD_PORT));
        }


        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            doc.append(PROTOCOL_FIELD_HOST, hostPort.host);
            doc.append(PROTOCOL_FIELD_PORT, hostPort.port);
        }
    }

    public static class ConnectPeerResponse extends ClientProtocol.ConnectPeerRequest {
        public ProtocolField.Response response = new ProtocolField.Response();
    }

    public static class DisconnectPeerRequest extends ClientProtocol.ConnectPeerRequest {
    }

    public static class DisconnectPeerResponse extends ClientProtocol.ConnectPeerResponse {
    }


    // get all public ProtocolField properties of current instance
    private ArrayList<ProtocolField> getAllProtocolFields() {
        ArrayList<ProtocolField> protocolFields = new ArrayList<>();

        // get fields from parent
        Field[] child_fields = this.getClass().getDeclaredFields();
        Field[] parent_fields = this.getClass().getSuperclass().getDeclaredFields();
        Field[] fields = Stream.concat(Arrays.stream(child_fields), Arrays.stream(parent_fields))
                .toArray(Field[]::new);

        // add fields to the class
        for (Field field : fields) {
            try {
                Object obj = field.get(this);
                if (obj instanceof ProtocolField) {
                    protocolFields.add((ProtocolField) obj);
                }
            } catch (Exception ignored) {
            }
        }

        return protocolFields;
    }
}
