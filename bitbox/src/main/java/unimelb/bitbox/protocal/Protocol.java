package unimelb.bitbox.protocal;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static unimelb.bitbox.protocal.Constants.*;

public abstract class Protocol implements IProtocol {

    // get all public ProtocolField properties of current instance
    private ArrayList<ProtocolField> getAllProtocolFields() {
        ArrayList<ProtocolField> protocolFields = new ArrayList<>();

        Field[] fields = this.getClass().getDeclaredFields();
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
}
