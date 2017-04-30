package api.request;

import api.CurrencyPair;
import api.tmp_trade.Trade;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 4/27/17.
 */
public class OrderBookResponse extends MarketResponse {
    // TODO(stfinancial): Sequence number?

    private final Map<CurrencyPair, List<Trade>> asks; // sells
    private final Map<CurrencyPair, List<Trade>> bids; // buys
//    private boolean isFrozen = false;

    public OrderBookResponse(Map<CurrencyPair, List<Trade>> asks, Map<CurrencyPair, List<Trade>> bids, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.asks = asks;
        this.bids = bids;
//        this.isFrozen = isFrozen;
    }

    // *** NOTE *** NO GUARANTEES ABOUT THE GRANULARITY OF THE ORDER BOOK, WHETHER AGGREGATED OR NOT.

    public Map<CurrencyPair, List<Trade>> getAsks() {
        return Collections.unmodifiableMap(asks);
    }

    public Map<CurrencyPair, List<Trade>> getBids() {
        return Collections.unmodifiableMap(bids);
    }

//    public boolean isFrozen() {
//        return isFrozen;
//    }
}
