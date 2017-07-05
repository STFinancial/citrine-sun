package api.bitfinex;

import api.HmacAlgorithm;
import api.HmacSigner;
import api.Market;
import api.MarketConstants;
import api.request.MarketRequest;
import api.request.MarketResponse;
import org.apache.http.impl.client.HttpClients;

/**
 * Created by Timothy on 3/25/17.
 */
public final class Bitfinex extends Market {
    // TODO(stfinancial): Handle decimal precision here (5 sigfigs) and other places (8 decimal places on Polo).

    private static final String MARKET_NAME = "Bitfinex";
    private static final String PUBLIC_URI = "https://api.bitfinex.com/v2/";
//    private static final String PRIVATE_URI =

    private static final HmacAlgorithm algorithm = HmacAlgorithm.HMACSHA384;

    // TODO(stfinancial): Take in credentials instead of api and secret key.
    public Bitfinex(String apiKey, String secretKey) {
        // TODO(stfinancial): Can this be done in the superclass constructor?
        this.apiKey = apiKey;
//        this.signer = new HmacSigner(algorithm, secretKey);
        this.httpClient = HttpClients.createDefault();

    }

    @Override
    public MarketResponse processMarketRequest(MarketRequest request) {
        BitfinexRequestRewriter.rewriteRequest(request);
        return null;
    }

    @Override
    public String getName() {
        return MARKET_NAME;
    }

    @Override
    public MarketConstants getConstants() {
        return null;
    }

    @Override
    protected MarketResponse sendRequest(MarketRequest request) {
        return null;
    }
}
