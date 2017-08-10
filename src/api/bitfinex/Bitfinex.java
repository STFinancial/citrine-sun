package api.bitfinex;

import api.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.nio.charset.Charset;

// TODO(stfinancial): Apparently you can't place a trade through the rest API... so you need to get the websocket shit working.
/**
 * Class representing the Bitfinex {@code Market}.
 */
public final class Bitfinex extends Market {
    // TODO(stfinancial): Handle decimal precision here (5 sigfigs) and other places (8 decimal places on Polo).
    private static final String NAME = "Bitfinex";
    private static final HmacAlgorithm algorithm = HmacAlgorithm.HMACSHA384;

    public Bitfinex(Credentials credentials) {
        super(credentials);
        if (!credentials.isPublicOnly()) {
            this.signer = new HmacSigner(algorithm, credentials, false);
        }
        this.requestRewriter = new BitfinexRequestRewriter();
        this.responseParser = new BitfinexResponseParser();
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
        long timestamp = System.currentTimeMillis();
        if (args.isPrivate()) {
            String url = args.asUrl(true);
            httpRequest = new HttpPost(url);
            httpRequest.addHeader("bfx-nonce", String.valueOf(timestamp));
            System.out.println("Nonce: " + String.valueOf(timestamp));
            httpRequest.addHeader("bfx-apikey", apiKey);
            System.out.println("ApiKey: " + apiKey);
            String stuff = "/api" + args.getResourcePath() + String.valueOf(timestamp) + args.getQueryString();
//            String stuff = "/api/" + url + String.valueOf(timestamp) + args.getQueryString();
            System.out.println("Body: " + stuff);
//            String sign = signer.getHexDigest(stuff.getBytes());
            String sign = signer.getHexDigest(stuff.getBytes(Charset.forName("UTF-8")));
            httpRequest.addHeader("bfx-signature", sign);
//            ((HttpPost) httpRequest).setEntity(new StringEntity("{}", ContentType.APPLICATION_JSON));
//            try { ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(Arrays.asList(new NameValuePair[]{new BasicNameValuePair("json", "{}")}), "UTF-8")); } catch (Exception e) {};
//            ((HttpPost) httpRequest).setEntity(new StringEntity(args.asJson().toString(), ContentType.APPLICATION_JSON));
            ((HttpPost) httpRequest).setEntity(new StringEntity(args.asJson(mapper).toString(), ContentType.APPLICATION_FORM_URLENCODED));
        } else {
            if (args.getHttpRequestType() != RequestArgs.HttpRequestType.GET) {
                return null;
            }
            httpRequest = new HttpGet(args.asUrl(true));
        }
        return httpRequest;
    }
}
