package unimelb.bitbox.protocol;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static unimelb.bitbox.protocol.Constants.*;


public abstract class Protocol implements IProtocol {

    // get all public ProtocolField properties of current instance
    private ArrayList<ProtocolField> getAllProtocolFields() {
        ArrayList<ProtocolField> protocolFields = new ArrayList<>();

        Field[] child_fields = this.getClass().getDeclaredFields(); //这个没有拿到parent 的field
        Field[] parent_fields = this.getClass().getSuperclass().getDeclaredFields();
        Field[] fields = Stream.concat(Arrays.stream(child_fields), Arrays.stream(parent_fields))
                .toArray(Field[]::new);
        for (Field field: fields) {
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

    @Override
    public void unmarshalFromJson(Document doc) {
        for (ProtocolField protocolField: getAllProtocolFields()) {
            protocolField.unmarshalFromJson(doc);
        }
    }

    @Override
    public void marshalToJson(Document doc) {
        for (ProtocolField protocolField: getAllProtocolFields()) {
            protocolField.marshalToJson(doc);
        }
    }


    public static class InvalidProtocol extends Protocol {
        public String msg; //message

        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            this.msg = doc.getString(MSG);
        }

        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            doc.append(MSG, msg);
        }

    }


    public static class ConnectionRefused extends Protocol {
        public String msg;                // message
        public ArrayList<HostPort> peers = new ArrayList<>(); // list of peers

        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            for (Document subdoc: (ArrayList<Document>) doc.get(PEER)) {
                peers.add(new HostPort(subdoc));
            }
            this.msg = doc.getString(MSG);
        }

        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);

            ArrayList<Document> peerDocs = new ArrayList<>();
            for(HostPort hostPort: peers) {
                peerDocs.add(hostPort.toDoc());
            }
            doc.append(PEER, peerDocs);
            doc.append(MSG, msg);
        }
    }


    public static class HandshakeRequest extends Protocol {
        public HostPort peer = new HostPort();

        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);

            this.peer = new HostPort((Document) doc.get(HOST_PORT));
        }

        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            doc.append(HOST_PORT, this.peer.toDoc());
        }
    }

    public static class HandshakeResponse extends HandshakeRequest {
    }

    public static class FileCreateRequest extends Protocol{
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();
    }

    public static class FileCreateResponse extends Protocol{
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();
        public ProtocolField.Response response = new ProtocolField.Response();
    }

    public static class FileDeleteRequest extends FileCreateRequest {
    }

    public static class FileDeleteResponse extends FileCreateResponse{
    }

    public static class FileModifyRequest extends FileCreateRequest {
    }

    public static class FileModifyResponse extends FileCreateResponse{
    }

    public static class FileBytesRequest extends Protocol{
        public ProtocolField.FilePosition filePos = new ProtocolField.FilePosition();
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();
    }

    public static class FileBytesResponse extends Protocol{
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

    public static class DirectoryDeleteResponse extends DirectoryCreateResponse{
    }

}
