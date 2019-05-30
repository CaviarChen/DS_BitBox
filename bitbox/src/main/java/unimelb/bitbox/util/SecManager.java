package unimelb.bitbox.util;


import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import unimelb.bitbox.Constants;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


/**
 * Security Manager class for Bitbox, including RSA and AES related methods
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 * <p>
 * Reference:
 * - stackoverflow.com/questions/44681737/get-a-privatekey-from-a-rsa-pem-file
 * - artofcode.wordpress.com/2017/05/26/rsa-signatures-in-java-with-bouncy-castle/
 * - www.javainterviewpoint.com/aes-encryption-and-decryption/
 * - stackoverflow.com/questions/43978146/how-to-convert-ssh-rsa-public-key-to-pem-pkcs1-public-key-format-using-java-7
 */
public class SecManager {

    private static Logger log = Logger.getLogger(SecManager.class.getName());

    private static final int AES_BLOCK_SIZE = 16;
    private static final int INT_SIZE_BYTES = 4;

    // letters in bytes used to pad when encrypting using AES
    private static final byte[] PADDING_LETTERS = {
            0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a
    };
    private static SecManager instance = new SecManager();


    public static SecManager getInstance() {
        return instance;
    }


    private static PrivateKey privateKey;
    private static String privateIdentity;
    private static HashMap<String, PublicKey> publicKeyHashMap;
    private static AESKey aesKey;


    /**
     * The modes of SecManager
     */
    public enum Mode {
        ClientMode,
        ServerMode
    }


    /**
     * Encrypt the JSON string with AES-128 and then encode with base64
     *
     * @param json the JSON wanted to be encoded
     * @return the encrypted string
     * @throws Exception encryption failed
     */
    public String encryptJSON(String json) throws Exception {
        if (aesKey == null) {
            throw new IllegalStateException("No AES key obtained");
        }

        return encryptWithAesKey(aesKey, json);
    }


    /**
     * Decrypt the base64 encoded and AES-128 encrypted string
     *
     * @param payload the payload of the protocol message
     * @return the decrypted message in JSON format
     * @throws Exception decryption failed
     */
    public String decryptPayload(String payload) throws Exception {
        if (aesKey == null) {
            throw new IllegalStateException("No AES key obtained");
        }

        return decryptWithAesKey(aesKey, payload);
    }


    /**
     * Initialize the SecManager, read necessary data into the calss
     *
     * @param m {@link SecManager.Mode}
     * @throws Exception initialization failed
     */
    public void init(Mode m) throws Exception {
        instance = new SecManager();

        Security.addProvider(new BouncyCastleProvider());

        publicKeyHashMap = new HashMap<>();
        aesKey = null;

        switch (m) {
            case ClientMode:
                readPrivateKey();
                break;
            case ServerMode:
                readPublicKeyFromProperties();
                break;
        }
    }


    /**
     * generate a AES-128 secret key with secure random padding
     *
     * @throws Exception generate AES key failed
     */
    public void generateAES() throws Exception {
        // if already exist, replace it
        aesKey = new AESKey();
    }


    /**
     * Encrypt AES-128 key with RSA public key on Server Side
     *
     * @param identity the identity given by the client
     * @return a based64 encoded, RSA encrypted, AES-128 secret key
     * @throws Exception encryption failed
     */
    public String encryptAESWithRSA(String identity) throws Exception {
        if (aesKey == null) {
            generateAES();
        }

        PublicKey publicKey = publicKeyHashMap.get(identity);
        if (publicKey == null) {
            throw new Exception("The public key is not in the config file");
        }

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherText = cipher.doFinal(aesKey.getKey().getEncoded());

        return encodeBase64ToString(cipherText);
    }


    /**
     * Decrypt the base64 encoded, RSA encrypted AES-128 key. It
     * handles the store of AES secret key, to encode
     *
     * @param ciphterText the encrypted text
     * @throws Exception decryption failed
     */
    public void decryptAESWithRSA(String ciphterText) throws Exception {
        byte[] cipherText = decodeBase64(ciphterText.getBytes());

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = cipher.doFinal(cipherText);

        aesKey = new AESKey(new SecretKeySpec(aesKeyBytes, "AES"));
    }


    /**
     * Remove the AES key from the SecManager
     */
    public void removeAES() {
        aesKey = null;
    }


    /**
     * Get the Identity parsed from Private key file
     *
     * @return Identity
     */
    public String getPrivateIdentity() {
        return privateIdentity;
    }


    /**
     * Set the Identity of current Client
     *
     * @param identity
     */
    public void setPrivateIdentity(String identity) {
        privateIdentity = identity;
    }


    private void readPublicKeyFromProperties() throws Exception {
        // read a list of public keys from properties
        String config = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_AUTHORIZED_KEYS);
        String[] keys = config.split(",");
        for (String key : keys) {
            String[] parts = key.split(" ");
            if (parts.length >= 3) {
                // generate public key
                PublicKey publickey = getPublicKey(parts[1].trim());
                String identity = parts[2].trim();

                publicKeyHashMap.put(identity, publickey);
            }
        }
    }


    private void readPrivateKey() throws Exception {
        // read private key from file
        PEMParser pem = new PEMParser(new FileReader(Constants.SECURITY_PRIVATE_KEY_FILENAME));
        PrivateKeyInfo privateKeyInfo = ((PEMKeyPair) pem.readObject()).getPrivateKeyInfo();

        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        privateKey = converter.getPrivateKey(privateKeyInfo);

        pem.close();
    }


    private PublicKey getPublicKey(String key) throws Exception {
        // convert key to Java Public key
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodeBase64(key.getBytes()));
        AtomicInteger pos = new AtomicInteger();
        String algs = readString(byteBuffer, pos);

        if (!algs.equals("ssh-rsa")) {
            throw new Exception("Invalid public key format. Actual: " + algs);
        }

        // read exponent part
        BigInteger exp = readMpint(byteBuffer, pos);

        // read modulus part
        BigInteger mod = readMpint(byteBuffer, pos);

        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(mod, exp);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec);

        return publicKey;
    }


    private BigInteger readMpint(ByteBuffer buffer, AtomicInteger pos) {
        // read big integer from buffer
        byte[] bytes = readBytes(buffer, pos);
        if (bytes.length == 0) {
            return BigInteger.ZERO;
        }
        return new BigInteger(bytes);
    }


    private String readString(ByteBuffer buffer, AtomicInteger pos) {
        // read string from buffer
        byte[] bytes = readBytes(buffer, pos);
        if (bytes.length == 0) {
            return "";
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }


    private byte[] readBytes(ByteBuffer buffer, AtomicInteger pos) {
        // read given number of bytes
        int len = buffer.getInt(pos.get());
        byte[] buff = new byte[len];
        for (int i = 0; i < len; i++) {
            buff[i] = buffer.get(i + pos.get() + INT_SIZE_BYTES);
        }
        pos.set(pos.get() + INT_SIZE_BYTES + len);
        return buff;
    }


    private String encryptWithAesKey(AESKey key, String text) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getKey().getEncoded(), "AES");

        // separate text and padding as required
        text += "\n";

        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

        // padding
        int remainingBytes = textBytes.length % AES_BLOCK_SIZE;
        int paddingLength = 0;
        if (remainingBytes != 0) {
            paddingLength = AES_BLOCK_SIZE - remainingBytes;
        }

        // securely random letters as padding
        byte[] paddingBytes = new byte[paddingLength];
        SecureRandom rand = new SecureRandom();
        for (int i = 0; i < paddingLength; i++) {
            int index = rand.nextInt(PADDING_LETTERS.length);
            paddingBytes[i] = PADDING_LETTERS[index];
        }

        // copy bytes
        byte[] finalBytes = new byte[textBytes.length + paddingLength];
        System.arraycopy(textBytes, 0, finalBytes, 0, textBytes.length);
        System.arraycopy(paddingBytes, 0, finalBytes, textBytes.length, paddingBytes.length);

        // encrypt
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] cipherText = cipher.doFinal(finalBytes);

        return encodeBase64ToString(cipherText);
    }


    private String decryptWithAesKey(AESKey key, String text) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getKey().getEncoded(), "AES");

        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decryptedBytes = cipher.doFinal(decodeBase64(text.getBytes()));

        // find the last newline character
        String decryptedText = new String(decryptedBytes);
        int lastNewLinePos = decryptedText.lastIndexOf('\n');

        return decryptedText.substring(0, lastNewLinePos);
    }


    private byte[] decodeBase64(byte[] text) {
        return Base64.getDecoder().decode(text);
    }


    private String encodeBase64ToString(byte[] text) {
        return Base64.getEncoder().encodeToString(text);
    }


    private class AESKey {
        // class containing AES key
        private SecretKey key;


        private AESKey() throws Exception {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);

            key = keyGenerator.generateKey();
        }


        private AESKey(SecretKey aesKey) {
            key = aesKey;
        }


        private SecretKey getKey() {
            return key;
        }
    }
}
