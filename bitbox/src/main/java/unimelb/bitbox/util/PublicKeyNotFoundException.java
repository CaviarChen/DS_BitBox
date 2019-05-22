package unimelb.bitbox.util;

/**
 * Exception Wrapper for using Public Key to encrypt AES key
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 */
public class PublicKeyNotFoundException extends Exception {
    public PublicKeyNotFoundException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
