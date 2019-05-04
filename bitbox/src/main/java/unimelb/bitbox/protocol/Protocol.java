package unimelb.bitbox.protocol;


import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static unimelb.bitbox.Constants.*;


/**
 * Protocol construct all protocols in the system.
 * Using reflection to get all related fields value.
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public abstract class Protocol implements IProtocol {


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


    public static class InvalidProtocol extends Protocol {
        public String msg;


        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            this.msg = doc.getString(PROTOCOL_FIELD_MSG);
        }


        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            doc.append(PROTOCOL_FIELD_MSG, msg);
        }

    }


    public static class ConnectionRefused extends Protocol {
        public String msg;
        public ArrayList<HostPort> peers = new ArrayList<>(); // list of peers


        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            for (Document subdoc : (ArrayList<Document>) doc.get(PROTOCOL_FIELD_PEER)) {
                peers.add(new HostPort(subdoc));
            }
            this.msg = doc.getString(PROTOCOL_FIELD_MSG);
        }


        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);

            ArrayList<Document> peerDocs = new ArrayList<>();
            for (HostPort hostPort : peers) {
                peerDocs.add(hostPort.toDoc());
            }
            doc.append(PROTOCOL_FIELD_PEER, peerDocs);
            doc.append(PROTOCOL_FIELD_MSG, msg);
        }
    }


    public static class HandshakeRequest extends Protocol {
        public HostPort peer = new HostPort();


        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);

            this.peer = new HostPort((Document) doc.get(PROTOCOL_FIELD_HOST_PORT));
        }


        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            doc.append(PROTOCOL_FIELD_HOST_PORT, this.peer.toDoc());
        }
    }


    public static class HandshakeResponse extends HandshakeRequest {
    }


    public static class FileCreateRequest extends Protocol {
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();
    }


    public static class FileCreateResponse extends Protocol {
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();
        public ProtocolField.Response response = new ProtocolField.Response();
    }


    public static class FileDeleteRequest extends FileCreateRequest {
    }


    public static class FileDeleteResponse extends FileCreateResponse {
    }


    public static class FileModifyRequest extends FileCreateRequest {
    }


    public static class FileModifyResponse extends FileCreateResponse {
    }


    public static class FileBytesRequest extends Protocol {
        public ProtocolField.FilePosition filePos = new ProtocolField.FilePosition();
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();
    }


    public static class FileBytesResponse extends Protocol {
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();
        public ProtocolField.FileContent fileContent = new ProtocolField.FileContent();
        public ProtocolField.Response response = new ProtocolField.Response();
    }


    public static class DirectoryCreateRequest extends Protocol {
        public ProtocolField.Path dirPath = new ProtocolField.Path();
    }


    public static class DirectoryCreateResponse extends Protocol {
        public ProtocolField.Path dirPath = new ProtocolField.Path();
        public ProtocolField.Response response = new ProtocolField.Response();
    }


    public static class DirectoryDeleteRequest extends DirectoryCreateRequest {
    }


    public static class DirectoryDeleteResponse extends DirectoryCreateResponse {
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
