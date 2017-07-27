package api.request;

import api.CurrencyPair;
import api.tmp_trade.CompletedTrade;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Response containing the list of {@link api.tmp_trade.CompletedTrade Completed Trades} as specified by the
 * {@code TradeHistoryRequest}
 */
public final class TradeHistoryResponse extends MarketResponse {
    private final Map<CurrencyPair, List<CompletedTrade>> completedTrades;

    public TradeHistoryResponse(Map<CurrencyPair, List<CompletedTrade>> completedTrades, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.completedTrades = Collections.unmodifiableMap(completedTrades);
    }

    public Map<CurrencyPair, List<CompletedTrade>> getCompletedTrades() { return completedTrades; }
}
