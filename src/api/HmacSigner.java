package api;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Object containing the secret key and HMAC hashing algorithm used for a {@link Market}.
 * This class's primary responsibility is hashing encoded URL query strings with a secret
 * key and a hashing algorithm.
 */
public final class HmacSigner {
    private final HmacAlgorithm algorithm;
    private Mac mac;
    private boolean status;

    // TODO(stfinancial): More graceful way of handling whether we need to decode the secret key (some may need to be decoded in hex, base64, etc.)
    public HmacSigner(HmacAlgorithm algorithm, String secretKey, boolean decode) {
        this.algorithm = algorithm;
        try {
            mac = Mac.getInstance(algorithm.getStandardName());
            SecretKeySpec sk;
            if (decode) {
                sk = new SecretKeySpec(Base64.decodeBase64(secretKey), algorithm.getStandardName());
            } else {
                sk = new SecretKeySpec(secretKey.getBytes(), algorithm.getStandardName());
            }
            mac.init(sk);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("No such MAC algorithm.");
            e.printStackTrace();
            mac = null;
            status = false;
        } catch (InvalidKeyException e) {
            System.out.println("Cannot initialize MAC with specified Key.");
            e.printStackTrace();
            status = false;
        }
    }

    /**
     * Signs the data using the secret key and algorithm specified and converts result to a hex string.
     * @param data The data to sign. This typically will be the encoded URL query string.
     * @return Signed version of the specified data. Hashed with this object's algorithm and secret key.
     */
    public String getHexDigest(byte[] data) { return Hex.encodeHexString(mac.doFinal(data)); }

    public String getBase64Digest(byte[] data) { return Base64.encodeBase64String(mac.doFinal(data)); }

    /**
     * Signs the data using the secret key and algorithm specified.
     * @param data The data to sign. This typically will be the encoded URL query string.
     * @return Signed version of the specified data. Hashed with this object's algorithm and secret key.
     */
    public byte[] sign(byte[] data) {
        return mac.doFinal(data);
    }

    /**
     * @return True if the Mac object could be initialized given the algorithm and secret key.
     * False otherwise.
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * @return The {@link HmacAlgorithm} used by this object.
     */
    public HmacAlgorithm getAlgorithm() {
        return algorithm;
    }
}
