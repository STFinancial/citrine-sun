package api.request;

import api.tmp_trade.CompletedTrade;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;

/**
 * Returns {@link api.tmp_trade.CompletedTrade CompletedTrades} associated with a given order id.
 */
public final class OrderTradesResponse extends MarketResponse {
    private final List<CompletedTrade> trades;

    public OrderTradesResponse(List<CompletedTrade> trades, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.trades = trades;
    }

    public List<CompletedTrade> getTrades() {
        return Collections.unmodifiableList(trades);
    }
}
