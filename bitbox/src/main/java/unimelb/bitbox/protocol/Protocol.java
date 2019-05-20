package unimelb.bitbox.protocol;


import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
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


    public static class FileCreateRequest extends Protocol implements IRequest {
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof FileCreateRequest)) {
                return false;
            }
            FileCreateRequest p = (FileCreateRequest) o;
            return p.fileDes.equals((((FileCreateRequest) o).fileDes));
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(fileDes);
        }
    }


    public static class FileCreateResponse extends Protocol implements IResponse  {
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();
        public ProtocolField.Response response = new ProtocolField.Response();
    }


    public static class FileDeleteRequest extends FileCreateRequest {

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof FileDeleteRequest)) {
                return false;
            }
            FileDeleteRequest p = (FileDeleteRequest) o;
            return p.fileDes.equals((((FileDeleteRequest) o).fileDes));
        }
    }


    public static class FileDeleteResponse extends FileCreateResponse {
    }


    public static class FileModifyRequest extends FileCreateRequest {

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof FileModifyRequest)) {
                return false;
            }
            FileModifyRequest p = (FileModifyRequest) o;
            return p.fileDes.equals((((FileModifyRequest) o).fileDes));
        }
    }


    public static class FileModifyResponse extends FileCreateResponse {
    }


    public static class FileBytesRequest extends Protocol implements IRequest  {
        public ProtocolField.FilePosition filePos = new ProtocolField.FilePosition();
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof FileBytesRequest)) {
                return false;
            }
            FileBytesRequest p = (FileBytesRequest) o;
            return p.fileDes.equals((((FileBytesRequest) o).fileDes)) &&
                    p.filePos.equals((((FileBytesRequest) o).filePos));
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileDes,filePos);
        }
    }


    public static class FileBytesResponse extends Protocol implements IResponse  {
        public ProtocolField.FileDes fileDes = new ProtocolField.FileDes();
        public ProtocolField.FileContent fileContent = new ProtocolField.FileContent();
        public ProtocolField.Response response = new ProtocolField.Response();
    }


    public static class DirectoryCreateRequest extends Protocol implements IRequest  {
        public ProtocolField.Path dirPath = new ProtocolField.Path();

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof DirectoryCreateRequest)) {
                return false;
            }
            DirectoryCreateRequest p = (DirectoryCreateRequest) o;
            return p.dirPath.equals((((DirectoryCreateRequest) o).dirPath));
        }

        @Override
        public int hashCode() {
            return Objects.hash(dirPath);
        }
    }


    public static class DirectoryCreateResponse extends Protocol implements IResponse  {
        public ProtocolField.Path dirPath = new ProtocolField.Path();
        public ProtocolField.Response response = new ProtocolField.Response();
    }


    public static class DirectoryDeleteRequest extends DirectoryCreateRequest {

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof DirectoryDeleteRequest)) {
                return false;
            }
            DirectoryDeleteRequest p = (DirectoryDeleteRequest) o;
            return p.dirPath.equals((((DirectoryDeleteRequest) o).dirPath));
        }
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
