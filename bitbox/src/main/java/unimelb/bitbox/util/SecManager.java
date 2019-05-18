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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Logger;


// RSA Signature:
// https://stackoverflow.com/questions/44681737/get-a-privatekey-from-a-rsa-pem-file
// https://artofcode.wordpress.com/2017/05/26/rsa-signatures-in-java-with-bouncy-castle/
// https://www.javainterviewpoint.com/aes-encryption-and-decryption/
public class SecManager {

    public enum  Mode {
        ClientMode,
        ServerMode
    }

    private static Logger log = Logger.getLogger(SecManager.class.getName());
    private static SecManager instance = new SecManager();
    private static PrivateKey privateKey;
    private static HashMap<String, PublicKey> publicKeyHashMap;
    private static HashMap<String, AESKey> aesKeyHashMap;

    public void init(Mode m) throws Exception {
        switch (m) {
            case ClientMode:
                readPrivateKey();
                break;
            case ServerMode:
                readPublicKeyFromProperties();
                break;
        }

        Security.addProvider(new BouncyCastleProvider());

        instance = new SecManager();
    }

    public static SecManager getInstance() {
        return instance;
    }

    private void SecurityHelper() {
    }

    private void readPublicKeyFromProperties() {
        String config = Configuration.getConfigurationValue(Constants.CONFIG_FIELD_AUTHORIZED_KEYS);
        String[] keys = config.split(",");
        for (String key : keys) {

            // decode open-ssh format
            String[] parts = key.split(" ");
            if (parts.length == 3 && !((parts[0].trim()).equals("ssh-rsa"))) {
                PublicKey publickey = getPublicKey(parts[1].trim());
                String identity = parts[2].trim();

                publicKeyHashMap.put(identity, publickey);
            }
        }
    }

    private void readPrivateKey() throws Exception {
        // read private key from file
        PEMParser pem = new PEMParser(new FileReader(Constants.SECURITY_PRIVATE_KEY_FILENAME));
        PrivateKeyInfo privateKeyInfo = ((PEMKeyPair)pem.readObject()).getPrivateKeyInfo();

        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        privateKey = converter.getPrivateKey(privateKeyInfo);

        pem.close();
    }

    private PublicKey getPublicKey(String key) {
        try{
            byte[] byteKey = Base64.getDecoder().decode(key.getBytes());

            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            return KeyFactory.getInstance("RSA").generatePublic(X509publicKey);
        }
        catch(NoSuchAlgorithmException | InvalidKeySpecException e){
            log.severe(e.toString());
        }

        return null;
    }


    /**
     * Encrypt the JSON string with AES-128 and then encode with base64
     * @param identity the identity of the keypair
     * @param json the JSON wanted to be encoded
     * @return the encrypted string
     * @throws Exception encryption failed
     */
    public static String encryptJSON(String identity, String json) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKeyHashMap.get(identity).getKey().getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(aesKeyHashMap.get(identity).getInitializeVector());

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] cipherText = cipher.doFinal(json.getBytes());

        return encodeBase64ToString(cipherText);
    }


    /**
     * Decrypt the base64 encoded and AES-128 encrypted string
     * @param identity the identity of the keypair
     * @param payload the payload of the protocol message
     * @return the decrypted message in JSON format
     * @throws Exception decryption failed
     */
    public static String decryptPayload(String identity, String payload) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKeyHashMap.get(identity).getKey().getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(aesKeyHashMap.get(identity).getInitializeVector());

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decryptedText = cipher.doFinal(decodeBase64(payload.getBytes()));

        return new String(decryptedText);
    }


    /**
     * generate a AES-128 secret key with secure random padding
     * @param identity the identity of the keypair
     * @throws Exception generate AES key failed
     */
    public void generateAES(String identity) throws Exception {
        // if already exist, replace it
        aesKeyHashMap.put(identity, new AESKey());
    }

    public String encryptAESWithRSA(String identity) throws Exception {
        if (!aesKeyHashMap.containsKey(identity)) {
            generateAES(identity);
        }

        AESKey aesKey = aesKeyHashMap.get(identity);
        if (aesKey == null) {
            // unlikely to happen
            throw new Exception("Cannot get AES Key from map");
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

    public void decryptAESWithRSA(String identity, String ciphterText) throws Exception {
        byte[] cipherText = decodeBase64(ciphterText.getBytes());

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKey = cipher.doFinal(cipherText);

        aesKeyHashMap.put(identity, new AESKey(new SecretKeySpec(aesKey, "AES")));
    }

    public void removeAES(String identity) {
        aesKeyHashMap.remove(identity);
    }

    private static byte[] decodeBase64(byte[] text) {
        return Base64.getDecoder().decode(text);
    }

    private static String encodeBase64ToString(byte[] text) {
        return Base64.getEncoder().encodeToString(text);
    }


    private class AESKey {
        private byte[] init_vt;
        private SecretKey key;

        public AESKey() throws Exception {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);

            key = keyGenerator.generateKey();

            generateInitVector();
        }

        public AESKey(SecretKey aesKey) {
            key = aesKey;

            generateInitVector();
        }

        public SecretKey getKey() {
            return key;
        }

        public byte[] getInitializeVector() {
            return init_vt;
        }

        private void generateInitVector() {
            init_vt = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(init_vt);
        }
    }
}
