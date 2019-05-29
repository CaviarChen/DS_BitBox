package unimelb.bitbox.util;


import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import unimelb.bitbox.Constants;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


// https://stackoverflow.com/questions/44681737/get-a-privatekey-from-a-rsa-pem-file
// https://artofcode.wordpress.com/2017/05/26/rsa-signatures-in-java-with-bouncy-castle/
// https://www.javainterviewpoint.com/aes-encryption-and-decryption/
// https://stackoverflow.com/questions/43978146/how-to-convert-ssh-rsa-public-key-to-pem-pkcs1-public-key-format-using-java-7
public class SecManager {

    private static Logger log = Logger.getLogger(SecManager.class.getName());

    private static final int INT_SIZE_BYTES = 4;
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
                //readIdentityFromPrivateKey();
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
     * @return Identity
     */
    public String getPrivateIdentity() {
        return privateIdentity;
    }

    public void setPrivateIdentity(String identity) {
        privateIdentity = identity;
    }


    private void SecurityHelper() {
    }


    private void readPublicKeyFromProperties() throws Exception {
        String config = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_AUTHORIZED_KEYS);
        String[] keys = config.split(",");
        for (String key : keys) {
            String[] parts = key.split(" ");
            if (parts.length >= 3) {
                PublicKey publickey = getPublicKey(parts[1].trim());
                String identity = parts[2].trim();

                publicKeyHashMap.put(identity, publickey);
            }


//            String[] parts = key.split(" ");
//            if (parts.length == 3 && !((parts[0].trim()).equals("ssh-rsa"))) {
//                PublicKey publickey = getPublicKey(parts[1].trim());
//                String identity = parts[2].trim();
//
//                publicKeyHashMap.put(identity, publickey);
//            }
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


    private void readIdentityFromPrivateKey() throws Exception {
        byte[] textBytes = Files.readAllBytes(Paths.get(Constants.SECURITY_PRIVATE_KEY_FILENAME));
        String text = new String(textBytes);

        // remove BEGIN Header and END footer in private key file
        text = text.trim();
        String[] parts = text.split("\n");
        text = "";
        for (int i = 1; i < parts.length-1; i++) {
            text += parts[i];
        }

        System.out.println(text);
        text = new String(decodeBase64(text.getBytes()));
        System.out.println(text);

        parts = text.split(" ");
        if (parts.length < 3) {
            throw new Exception("Invalid private key from file " + Constants.SECURITY_PRIVATE_KEY_FILENAME);
        }

        privateIdentity = parts[parts.length - 1];
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

    private BigInteger readMpint(ByteBuffer buffer, AtomicInteger pos){
        byte[] bytes = readBytes(buffer, pos);
        if(bytes.length == 0){
            return BigInteger.ZERO;
        }
        return new BigInteger(bytes);
    }

    private String readString(ByteBuffer buffer, AtomicInteger pos){
        byte[] bytes = readBytes(buffer, pos);
        if(bytes.length == 0){
            return "";
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private byte[] readBytes(ByteBuffer buffer, AtomicInteger pos){
        int len = buffer.getInt(pos.get());
        byte buff[] = new byte[len];
        for(int i = 0; i < len; i++) {
            buff[i] = buffer.get(i + pos.get() + INT_SIZE_BYTES);
        }
        pos.set(pos.get() + INT_SIZE_BYTES + len);
        return buff;
    }


    private String encryptWithAesKey(AESKey key, String text) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getKey().getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(key.getInitializeVector());

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] cipherText = cipher.doFinal(text.getBytes());

        return encodeBase64ToString(cipherText);
    }


    private String decryptWithAesKey(AESKey key, String text) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getKey().getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(key.getInitializeVector());

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decryptedText = cipher.doFinal(decodeBase64(text.getBytes()));

        return new String(decryptedText);
    }


    private byte[] decodeBase64(byte[] text) {
        return Base64.getDecoder().decode(text);
    }


    private String encodeBase64ToString(byte[] text) {
        return Base64.getEncoder().encodeToString(text);
    }


    private class AESKey {
        private byte[] init_vt;
        private SecretKey key;


        private AESKey() throws Exception {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);

            key = keyGenerator.generateKey();

            generateInitVector();
        }


        private AESKey(SecretKey aesKey) {
            key = aesKey;

            generateInitVector();
        }


        private SecretKey getKey() {
            return key;
        }


        private byte[] getInitializeVector() {
            return init_vt;
        }


        private void generateInitVector() {
            init_vt = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(init_vt);
        }
    }
}
