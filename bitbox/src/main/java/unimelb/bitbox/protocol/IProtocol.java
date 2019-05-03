package unimelb.bitbox.protocol;


import unimelb.bitbox.util.Document;


/**
 *
 *
 * @author Wenqing Xue (813044)
 * @author Weizhi Xu (752454)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public interface IProtocol {
    void unmarshalFromJson(Document doc);


    void marshalToJson(Document doc);
}
