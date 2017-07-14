package api.bitfinex;

import api.*;
import api.request.MarketRequest;
import api.request.MarketResponse;

/**
 * Class representing the Bitfinex {@code Market}.
 */
public final class Bitfinex extends Market {
    // TODO(stfinancial): Handle decimal precision here (5 sigfigs) and other places (8 decimal places on Polo).
    private static final String MARKET_NAME = "Bitfinex";
    private static final HmacAlgorithm algorithm = HmacAlgorithm.HMACSHA384;

    private final BitfinexRequestRewriter requestRewriter;
    private final BitfinexResponseParser responseParser;

    public Bitfinex(Credentials credentials) {
        super(credentials);
        this.signer = new HmacSigner(algorithm, credentials.getSecretKey(), false);
        this.requestRewriter = new BitfinexRequestRewriter();
        this.responseParser = new BitfinexResponseParser();
    }

    @Override
    public MarketResponse processMarketRequest(MarketRequest request) {
        requestRewriter.rewriteRequest(request);
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
