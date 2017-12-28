package api.binance;

import api.*;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Created by Timothy on 12/20/17.
 */
public final class Binance extends Market {

    private static final String NAME = "Binance";
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA256;

    public Binance(Credentials credentials) {
        super(credentials);
        this.requestRewriter = new BinanceRequestRewriter();
        this.responseParser = new BinanceResponseParser();
        if (!credentials.isPublicOnly()) {
            this.signer = new HmacSigner(ALGORITHM, credentials, true);
        }
    }

    @Override
    protected HttpUriRequest constructHttpRequest(RequestArgs args) {
        HttpUriRequest httpRequest;
        String url = args.asUrl(true);
        if (args.isPrivate()) {
            switch (args.getHttpRequestType()) {
                case GET:
                    httpRequest = new HttpGet(url);
                    break;
                case POST:
                    httpRequest = new HttpPost(args.getUri());
                    httpRequest.addHeader("signature", signer.getHexDigest(args.getQueryString().getBytes()));
                    break;
                case DELETE:
                    httpRequest = new HttpDelete(url);
                    httpRequest.addHeader("signature", signer.getHexDigest(args.getQueryString().getBytes()));
                    break;
                default:
                    System.out.println("(" + NAME + ")" + "Invalid HttpRequestType: " + args.getHttpRequestType());
                    return null;
            }
            httpRequest.addHeader("X-MBX-APIKEY", apiKey);
        } else {
            if (args.getHttpRequestType() != RequestArgs.HttpRequestType.GET) {
                System.out.println("Public requests must use HttpGet. Type was: " + args.getHttpRequestType().toString().toUpperCase());
                return null;
            }
            httpRequest = new HttpGet(url);
        }
        return httpRequest;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }
}
