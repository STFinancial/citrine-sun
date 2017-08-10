package api.kraken;

import api.*;
import api.request.AssetPairRequest;
import api.request.AssetPairResponse;
import api.request.MarketResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class representing the Kraken {@code Market}.
 */
public final class Kraken extends Market {
    // TODO(stfinancial): Kraken has a nonce window depending on the api key.

    private static final String NAME = "Kraken";
    private static final String ENCODING = "UTF-8";

    // TODO(stfinancial): Make this into a superclass field?
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA512;
    private MessageDigest digest;
    private final KrakenData data;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }

    protected KrakenData getData() { return data; }

    public Kraken(Credentials credentials) {
        super(credentials);
        if (!credentials.isPublicOnly()) {
            this.signer = new HmacSigner(ALGORITHM, credentials, true);
        }
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        requestRewriter = new KrakenRequestRewriter(this);
        responseParser = new KrakenResponseParser(this);
        AssetPairRequest apr = new AssetPairRequest();
        MarketResponse r = processMarketRequest(apr);
        int retryCount = 5;
        while (!r.isSuccess() && retryCount-- > 0) {
            System.out.println("Could not get market data for " + NAME + ": " + r.getJsonResponse());
            r = processMarketRequest(apr);
        }
        if (!r.isSuccess()) {
            data = new KrakenData(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
        } else {
            System.out.println(r.getJsonResponse());
            data = new KrakenData(((AssetPairResponse) r).getAssetPairs(), ((AssetPairResponse) r).getAssetPairNames(), ((AssetPairResponse) r).getAssetPairKeys());
        }
    }

    @Override
    protected HttpUriRequest constructHttpRequest(RequestArgs args) {
        long timestamp = System.currentTimeMillis();
        HttpUriRequest httpRequest;
        System.out.println("Json: " + args.asJson(mapper).toString());
        String url;
        switch (args.getHttpRequestType()) {
            case GET:
                url = args.asUrl(true);
                httpRequest = new HttpGet(url);
                break;
            case POST:
                if (args.isPrivate()) {
                    url = args.asUrl(false);
                } else {
                    // TODO(stfinancial): First of all, can this case ever happen? Second is this always true? May need another refactor of RequestArgs...
                    url = args.asUrl(true);
                }
                httpRequest = new HttpPost(url);
                break;
            default:
                System.out.println("(" + NAME + ")" + "Invalid HttpRequestType: " + args.getHttpRequestType());
                return null;
        }
        System.out.println("URL: " + url);
        if (args.isPrivate()) {
            // TODO(stfinancial): Something here is not being encoded correctly, issues with commas and spaces.
            String baseQueryString = args.getQueryString();
            System.out.println("Base QueryString: " + baseQueryString);
//            try { baseQueryString = URLEncoder.encode(baseQueryString, ENCODING); } catch (Exception e) {}
//            System.out.println("Base QueryString (UrlEncoder): " + baseQueryString);
            String path = args.getResourcePath();
            System.out.println("Resource Path: " + path);

            String postData = "nonce=" + timestamp;
            if (baseQueryString != null && !baseQueryString.isEmpty()) {
                postData += "&" + baseQueryString;
            }
            System.out.println("Postdata: " + postData);

            byte[] encoded = (timestamp + postData).getBytes(Charset.forName("UTF-8"));
            System.out.println("Encoded: " + new String(encoded));

            byte[] noncePostdata = digest.digest(encoded);
            System.out.println("SHA256 of Encoded: " + new String(noncePostdata));

            byte[] pathBytes = path.getBytes(Charset.forName("UTF-8"));
            System.out.println("PathBytes: " + new String(pathBytes));

            byte[] message = new byte[pathBytes.length + noncePostdata.length];
            System.arraycopy(pathBytes, 0, message, 0, pathBytes.length);
            System.arraycopy(noncePostdata, 0, message, pathBytes.length, noncePostdata.length);
            System.out.println("Message: " + new String(message));

            String signature = signer.getBase64Digest(message);
            System.out.println("Signature: " + signature);

            httpRequest.addHeader("API-Key", apiKey);
            httpRequest.addHeader("API-Sign", signature);
//            httpRequest.addHeader("ContentType", "application/x-www-form-urlencoded");
            try {
                NameValuePair p = new BasicNameValuePair("nonce", String.valueOf(timestamp));
                ArrayList<NameValuePair> nvps = new ArrayList<>();
                nvps.add(p);
                nvps.addAll(args.asNameValuePairs());
                nvps.forEach((nvp)->System.out.println(nvp));
                ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(nvps, ENCODING));
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unsupported encoding: " + ENCODING);
                e.printStackTrace();
                return null;
            }
        }
        System.out.println(httpRequest.toString());
        return httpRequest;
    }
}
