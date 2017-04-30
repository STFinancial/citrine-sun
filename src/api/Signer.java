package api;

import org.apache.commons.codec.binary.Hex;

/**
 * Created by Timothy on 2/8/17.
 */
public interface Signer {
    // TODO(stfinancial): Does this interface make sense? Do we take in credentials (break their encapsulation) or pass this like a lambda function
    // to the credentials and allow them to sign? But that doesn't really make sense then... How do the credentials know how to sign properly...
    // The signer also needs to initialize itself with the credentials... so what does that entail exactly?

    // TODO(stfinancial): Fix the javadocs for this.

    /**
     * Signs the data using the secret key and algorithm specified and converts result to a hex string.
     * @param data The data to sign. This typically will be the encoded URL query string.
     * @return Signed version of the specified data. Hashed with this object's algorithm and secret key.
     */
    String getHexDigest(byte[] data); // { return Hex.encodeHexString(mac.doFinal(data)); }

    String getBase64Digest(byte[] data);

    /**
     * Signs the data using the secret key and algorithm specified.
     * @param data The data to sign. This typically will be the encoded URL query string.
     * @return Signed version of the specified data. Hashed with this object's algorithm and secret key.
     */
    byte[] sign(byte[] data); // {return mac.doFinal(data);}

//    /**
//     * @return True if the Mac object could be initialized given the algorithm and secret key.
//     * False otherwise.
//     */
//    public boolean getStatus() {
//        return status;
//    }
}
