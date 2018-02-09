package api.binance;

import api.*;
import api.request.AssetPairRequest;
import api.request.AssetPairResponse;
import api.request.MarketResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import java.nio.charset.Charset;
import java.util.Collections;

/**
 * {@code Market} implementation for Binance.
 */
public final class Binance extends Market {

    private static final String NAME = "Binance";
    private static final HmacAlgorithm ALGORITHM = HmacAlgorithm.HMACSHA256;
    private final BinanceData data;

    public Binance(Credentials credentials) {
        super(credentials);
        this.requestRewriter = new BinanceRequestRewriter(this);
        this.responseParser = new BinanceResponseParser(this);
        if (!credentials.isPublicOnly()) {
            this.signer = new HmacSigner(ALGORITHM, credentials, true);
        }

        AssetPairRequest apr = new AssetPairRequest();
        MarketResponse r = processMarketRequest(apr);
        int retryCount = 5;
        while (!r.isSuccess() && retryCount-- > 0) {
            System.out.println("Could not get market data for " + NAME + ": " + r.getJsonResponse());
            r = processMarketRequest(apr);
        }
        if (!r.isSuccess()) {
            data = new BinanceData(Collections.emptyList());
        } else {
            System.out.println(r.getJsonResponse());
            data = new BinanceData(((AssetPairResponse) r).getAssetPairs());
        }
    }

    @Override
    protected HttpUriRequest constructHttpRequest(RequestArgs args) {
        HttpUriRequest httpRequest;
        String url = args.asUrl(true);
        if (args.isPrivate()) {
            System.out.println("Query String: " + args.getQueryString());
            String signature = signer.getHexDigest(args.getQueryString().getBytes());
            System.out.println("Signature: " + signature);
            System.out.println("Api-Key: " + apiKey);
            switch (args.getHttpRequestType()) {
                case GET:
                    httpRequest = new HttpGet(url);
                    break;
                case POST:
                    httpRequest = new HttpPost(url + "&signature=" + signer.getHexDigest(args.getQueryString().getBytes()));
//                    httpRequest.addHeader("signature", signer.getHexDigest(args.getQueryString().getBytes()));
                    break;
                case DELETE:
                    httpRequest = new HttpDelete(url + "&signature=" + signer.getHexDigest(args.getQueryString().getBytes()));
//                    httpRequest.addHeader("signature", signer.getHexDigest(args.getQueryString().getBytes()));
                    break;
                default:
                    System.out.println("(" + NAME + ")" + "Invalid HttpRequestType: " + args.getHttpRequestType());
                    return null;
            }
            httpRequest.addHeader("X-MBX-APIKEY", apiKey);
            httpRequest.addHeader("Content-type", "application/x-www-form-urlencoded");
            System.out.println("Url: " + url);
            System.out.println(httpRequest.toString());
        } else {
            if (args.getHttpRequestType() != RequestArgs.HttpRequestType.GET) {
                System.out.println("Public requests must use HttpGet. Type was: " + args.getHttpRequestType().toString().toUpperCase());
                return null;
            }
            System.out.println(url);
            httpRequest = new HttpGet(url);
        }
        return httpRequest;
    }

    protected BinanceData getData() { return data; }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }
}
