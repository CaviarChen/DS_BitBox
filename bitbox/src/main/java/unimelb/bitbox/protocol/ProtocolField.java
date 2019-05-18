package unimelb.bitbox.protocol;


import unimelb.bitbox.util.Document;

import java.util.Objects;

import static unimelb.bitbox.Constants.*;


/**
 * Protocol Field contains all the files in the protocols of the system
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public abstract class ProtocolField implements IProtocol {

//  subclass  ------------------------------------


    public static class Path extends ProtocolField {
        public String path;         // pathName


        @Override
        public void unmarshalFromJson(Document doc) {
            this.path = doc.getString(PROTOCOL_FIELD_PATH_NAME);
        }


        @Override
        public void marshalToJson(Document doc) {
            doc.append(PROTOCOL_FIELD_PATH_NAME, this.path);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Path )) {
                return false;
            }
            Path  p = (Path ) o;
            return p.path.equals(((Path) o).path);
        }


        // for hashmap
        @Override
        public int hashCode() {
            return Objects.hash(path);
        }
    }


    public static class FileDes extends Path {
        public String md5;
        public long lastModified;
        public long fileSize;


        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            Document subDoc = (Document) doc.get(PROTOCOL_FIELD_FILE_DES);
            this.md5 = subDoc.getString(PROTOCOL_FIELD_MD5);
            this.lastModified = subDoc.getLong(PROTOCOL_FIELD_LAST_MODIFIED);
            this.fileSize = subDoc.getLong(PROTOCOL_FIELD_FILE_SIZE);
            this.path = doc.getString(PROTOCOL_FIELD_PATH_NAME);
        }


        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            Document subDoc = new Document();
            subDoc.append(PROTOCOL_FIELD_MD5, this.md5);
            subDoc.append(PROTOCOL_FIELD_LAST_MODIFIED, this.lastModified);
            subDoc.append(PROTOCOL_FIELD_FILE_SIZE, this.fileSize);
            doc.append(PROTOCOL_FIELD_FILE_DES, subDoc);
        }


        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Path )) {
                return false;
            }
            FileDes  p = (FileDes ) o;
            return p.md5.equals(((FileDes) o).md5) &&
                    p.lastModified == ((FileDes) o).lastModified &&
                    p.fileSize == ((FileDes) o).fileSize;

        }


        // for hashmap
        @Override
        public int hashCode() {
            return Objects.hash(md5,lastModified,fileSize);
        }
    }


    public static class Response extends ProtocolField {
        public String msg;         // message
        public Boolean status;      // status


        @Override
        public void unmarshalFromJson(Document doc) {
            this.msg = doc.getString(PROTOCOL_FIELD_MSG);
            this.status = doc.getBoolean(PROTOCOL_FIELD_STATUS);
        }


        @Override
        public void marshalToJson(Document doc) {
            doc.append(PROTOCOL_FIELD_MSG, this.msg);
            doc.append(PROTOCOL_FIELD_STATUS, this.status);
        }
    }


    public static class FilePosition extends ProtocolField {
        public long pos;         // position
        public long len;         // length


        @Override
        public void unmarshalFromJson(Document doc) {
            this.pos = doc.getLong(PROTOCOL_FIELD_POSITION);
            this.len = doc.getLong(PROTOCOL_FIELD_LENGTH);
        }


        @Override
        public void marshalToJson(Document doc) {
            doc.append(PROTOCOL_FIELD_POSITION, this.pos);
            doc.append(PROTOCOL_FIELD_LENGTH, this.len);
        }


        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof FilePosition)) {
                return false;
            }
            FilePosition p = (FilePosition) o;
            return p.pos == pos && p.len == len;
        }


        // for hashmap
        @Override
        public int hashCode() {
            return Objects.hash(pos, len);
        }
    }


    public static class FileContent extends FilePosition {
        public String content;      // content


        @Override
        public void unmarshalFromJson(Document doc) {
            super.unmarshalFromJson(doc);
            this.content = doc.getString(PROTOCOL_FIELD_CONTENT);
        }


        @Override
        public void marshalToJson(Document doc) {
            super.marshalToJson(doc);
            doc.append(PROTOCOL_FIELD_CONTENT, this.content);
        }
    }

}
