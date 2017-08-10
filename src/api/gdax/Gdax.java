package api.gdax;

import api.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * Class representing the Gdax {@code Market}.
 */
public final class Gdax extends Market {
    private static final String NAME = "Gdax";
    private static final String WAMP_ENDPOINT = "wss://ws-feed.gdax.com";

    // TODO(stfinancial): Make this into a superclass field?
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA256;
    private String passphrase;

    public Gdax(Credentials credentials) {
        super(credentials);
        if (!credentials.isPublicOnly()) {
            this.passphrase = credentials.getPassphrase();
            this.signer = new HmacSigner(ALGORITHM, credentials, true);
        }
        this.requestRewriter = new GdaxRequestRewriter();
        this.responseParser = new GdaxResponseParser();
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
        if (args.isPrivate()) {
            switch (args.getHttpRequestType()) {
                case GET:
                    httpRequest = new HttpGet(url);
                    break;
                case POST:
                    httpRequest = new HttpPost(url);
                    // TODO(stfinancial): Check that this is actually needed.
                    ((HttpPost) httpRequest).setEntity(new StringEntity(args.asJson(mapper).toString(), ContentType.APPLICATION_JSON));
                    break;
                case DELETE:
                    httpRequest = new HttpDelete(url);
                    break;
                default:
                    System.out.println("(" + NAME + ")" + "Invalid HttpRequestType: " + args.getHttpRequestType());
                    return null;
            }
            // TODO(stfinancial): Will change these names after getting a working implementation.
            String CB_ACCESS_TIMESTAMP = String.valueOf(System.currentTimeMillis() / 1000);
//            System.out.println("Timestamp: " + CB_ACCESS_TIMESTAMP);
            JsonNode json = args.asJson(mapper);
            System.out.println(json);
            String what;
            if (args.getHttpRequestType() != RequestArgs.HttpRequestType.POST || json.isNull()) {
                if (args.getQueryString() != null || !args.getQueryString().isEmpty()) {
                    what = String.valueOf(CB_ACCESS_TIMESTAMP) + args.getHttpRequestType().toString().toUpperCase() + args.getResourcePath() + "?" + args.getQueryString();
                } else {
                    what = String.valueOf(CB_ACCESS_TIMESTAMP) + args.getHttpRequestType().toString().toUpperCase() + args.getResourcePath();
                }
            } else {
                what = String.valueOf(CB_ACCESS_TIMESTAMP) + args.getHttpRequestType().toString().toUpperCase() + args.getResourcePath() + args.asJson(mapper).toString();
            }
            System.out.println("what: " + what);
            httpRequest.addHeader("CB-ACCESS-KEY", apiKey);
            httpRequest.addHeader("CB-ACCESS-SIGN", signer.getBase64Digest(what.getBytes()));
            httpRequest.addHeader("CB-ACCESS-TIMESTAMP", CB_ACCESS_TIMESTAMP);
            httpRequest.addHeader("CB-ACCESS-PASSPHRASE", passphrase);
        } else {
            if (args.getHttpRequestType() != RequestArgs.HttpRequestType.GET) {
                System.out.println("Public requests must use HttpGet. Type was: " + args.getHttpRequestType().toString().toUpperCase());
                return null;
            }
            httpRequest = new HttpGet(url);
        }
        return httpRequest;
    }
}
