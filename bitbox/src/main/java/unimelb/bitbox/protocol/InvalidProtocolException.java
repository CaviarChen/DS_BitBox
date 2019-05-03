package unimelb.bitbox.protocol;


/**
 *
 *
 * @author Wenqing Xue (813044)
 * @author Weizhi Xu (752454)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class InvalidProtocolException extends Exception {
    public InvalidProtocolException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
