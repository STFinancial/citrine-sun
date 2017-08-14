package api.bittrex;

import api.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;

/**
 *  Class representing the Bittrex {@code Market}.
 */
public final class Bittrex extends Market {
    private static final String NAME = "Bittrex";
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA512;

    public Bittrex(Credentials credentials) {
        super(credentials);
        if (credentials != Credentials.publicOnly()) {
            this.signer = new HmacSigner(ALGORITHM, credentials, false);
        }
        this.requestRewriter = new BittrexRequestRewriter(this);
        this.responseParser = new BittrexResponseParser();
        // TODO(stfinancial): Something similar to Kraken to get Currencies and markets available.
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }

    @Override
    protected HttpUriRequest constructHttpRequest(RequestArgs args) {
        HttpUriRequest httpRequest;
        String url = args.asUrl(true);
        // TODO(stfinancial): Should we actually check the httprequesttype?
        if (args.isPrivate()) {
            httpRequest = new HttpGet(url);
            String sign = signer.getHexDigest(url.getBytes());
            httpRequest.addHeader(new BasicHeader("apisign", sign));
        } else {
            httpRequest = new HttpGet(url);
        }
        return httpRequest;
    }
}
