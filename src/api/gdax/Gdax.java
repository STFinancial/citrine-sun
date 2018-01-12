package api.gdax;

import api.*;
import api.bitfinex.BitfinexSocketClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class representing the Gdax {@code Market}.
 */
public final class Gdax extends Market {
    private static final String NAME = "Gdax";
    private static final String WS_ENDPOINT = "wss://ws-feed.gdax.com";
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
//        ObjectNode root = JsonNodeFactory.instance.objectNode();
//        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
//        String payload = timestamp + "GET" + "/users/self";
//        root.put("type", "subscribe");
//        root.set("product_ids", JsonNodeFactory.instance.arrayNode().add("BTC-USD"));
//        root.put("signature", signer.getBase64Digest(payload.getBytes()));
//        root.put("key", getApiKey());
//        root.put("passphrase", passphrase);
//        root.put("timestamp", timestamp);
////        root.put()
//        try {
//            GdaxSocketClient socket = new GdaxSocketClient(new URI(WS_ENDPOINT), root);
////            socket.
////            socket.connect();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//        }
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
            String CB_ACCESS_TIMESTAMP = String.valueOf(System.currentTimeMillis() / 1000);
            JsonNode json = args.asJson(mapper);
            System.out.println(json);
            String what;
            if (args.getHttpRequestType() != RequestArgs.HttpRequestType.POST || json.isNull()) {
                if (/* args.getQueryString() != null || */!args.getQueryString().isEmpty()) {
                    System.out.println("Getting here.");
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
