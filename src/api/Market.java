package api;

import api.request.MarketRequest;
import api.request.MarketResponse;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Abstract class for
 */
public abstract class Market {
    // TODO(stfinancial): Need to make these non-protected, these are too visible. How can we abstract this stuff away?

    // TODO(stfinancial): How does the client know to initialize these if they are here? What is the alternative?
    protected HmacSigner signer;
    // TODO(stfinancial): Will there be one of these per thread? Do we need a different thing for multiple threads?
    protected CloseableHttpClient httpClient;

    // TODO(stfinancial): Is this the correct visibility on this?
    protected String apiKey;

    // TODO(stfinancial): Constructor that takes in credentials (how do we handle public only interfaces)

    public abstract MarketResponse processMarketRequest(MarketRequest request);

    public abstract String getName();

    public abstract MarketConstants getConstants();

//    public static Market getInstance();

    // TODO(stfinancial): Do we want a separate method for creating a price alert? Does this at all break anything else we're doing?
    // TODO(stfinancial): It does perhaps mean we need a separate thread listening to this channel. I'm not sure though.

}
