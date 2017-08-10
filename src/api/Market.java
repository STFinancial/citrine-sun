package api;

import api.request.MarketRequest;
import api.request.MarketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Abstract class for
 */
public abstract class Market {
    // TODO(stfinancial): Need to make these non-protected, these are too visible. How can we abstract this stuff away?

    // TODO(stfinancial): How does the client know to initialize these if they are here? What is the alternative?
    protected HmacSigner signer;
    // TODO(stfinancial): Will there be one of these per thread? Do we need a different thing for multiple threads?
    protected CloseableHttpClient httpClient;


    protected final ObjectMapper mapper;
    // TODO(stfinancial): Is this the correct visibility on this?
    protected final String apiKey;
    protected final Credentials credentials;

    public Market(Credentials credentials) {
        this.credentials = credentials;
        this.apiKey = credentials.getApiKey();
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }

    // TODO(stfinancial): Constructor that takes in credentials (how do we handle public only interfaces)

    public abstract MarketResponse processMarketRequest(MarketRequest request);

    public abstract String getName();

    // TODO(stfinancial): Handle this a little better.
    public abstract MarketConstants getConstants();

    // TODO(stfinancial): Does protected here make sense?
    protected abstract MarketResponse sendRequest(MarketRequest request);

    // TODO(stfinancial): Does this make sense?
    public String getApiKey() { return apiKey; }

//    public static Market getInstance();

    // TODO(stfinancial): Do we want a separate method for creating a price alert? Does this at all break anything else we're doing?
    // TODO(stfinancial): It does perhaps mean we need a separate thread listening to this channel. I'm not sure though.

}
