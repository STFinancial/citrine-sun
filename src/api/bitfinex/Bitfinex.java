package api.bitfinex;

import api.*;
import api.request.MarketRequest;
import api.request.MarketResponse;
import org.apache.http.impl.client.HttpClients;

/**
 * Class representing the Bitfinex {@code Market}.
 */
public final class Bitfinex extends Market {
    // TODO(stfinancial): Handle decimal precision here (5 sigfigs) and other places (8 decimal places on Polo).
    private static final String MARKET_NAME = "Bitfinex";

    private static final HmacAlgorithm algorithm = HmacAlgorithm.HMACSHA384;

    public Bitfinex(Credentials credentials) {
        super(credentials);
//        this.signer = new HmacSigner(algorithm, secretKey);
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
