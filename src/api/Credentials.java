package api;

import com.sun.istack.internal.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// TODO(stfinancial): How do we support API keys with restricted access to certain methods? Decompose API calls into interfaces?
/**
 * Contains the API key, Secret key, and maybe password for accessing a {@link Market Market's} API.
 */
public class Credentials {
    // TODO(stfinancial): How is this actually going to be checked in the Market class?
    private static final Credentials PUBLIC_ONLY = new Credentials("", "");
    // TODO(stfinancial): Support for separate market credentials (maybe)... this may clash with having multiple accounts per exchange.

    // TODO(stfinancial): Likely move this into a separate package with the signer interface (?) at which point we can perhaps better encapsulate.
    private final String apiKey;
    private final String secretKey;

    // TODO(stfinancial): Need to figure out how to actually handle this.
    private final String passphrase;

    private Credentials(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.passphrase = "";
    }

    private Credentials(String apiKey, String secretKey, String passphrase) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.passphrase = passphrase;
    }

    @Nullable
    // TODO(stfinancial): Specify the structure that this file should have.
    public static Credentials fromFileString(String fileString) {
        BufferedReader r;
        Credentials c;
        // TODO(stfinancial): Try a try with resources block.
        try {
            r = new BufferedReader(new FileReader(new File(fileString)));
            String apiKey = r.readLine();
            // TODO(stfinancial): Some markets (GDAX) requires decoding base64 secret string. I believe this is done automatically by the signer, but if not that needs to be an option here.
            String secretKey = r.readLine();
            String passphrase = r.readLine();
            // TODO(stfinancial): Figure out a better way for this.
            if (passphrase != null && !passphrase.isEmpty()) {
                c = new Credentials(apiKey, secretKey, passphrase);
            } else {
                c = new Credentials(apiKey, secretKey);
            }
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        }
        return c;
    }

    // TODO(stfinancial): Does this make sense or just having an empty market?
    /** @return A {@code Credentials} object allowing access to only public methods of the {@link Market}. */
    public static Credentials publicOnly() {
        return PUBLIC_ONLY;
    }
//
////     TODO(stfinancial): How does the signer get access to the secret key spec without recreating it every time?
//    public byte[] signData(Signer signer, byte[] data) {
//        return signer.sign(data);
//    }
//
//    public String getHexDigest(Signer signer, byte[] data) {
//        return signer.getHexDigest(data);
//    }

    // TODO(stfinancial): Remove these, or significantly reduce access.
    String getApiKey() { return apiKey; }
    String getSecretKey() { return secretKey; }
    public String getPassphrase() { return passphrase; }
    /** @return {@code true} if this object was returned from {@link api.Credentials#publicOnly()}, false otherwise. */
    public boolean isPublicOnly() { return this == PUBLIC_ONLY; }
}
