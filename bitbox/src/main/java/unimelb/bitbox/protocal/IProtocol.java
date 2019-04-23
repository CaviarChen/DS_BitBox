package unimelb.bitbox.protocal;

import unimelb.bitbox.util.Document;

public interface IProtocol {
    void unmarshalFromJson(Document doc);
    void marshalToJson(Document doc);
}
