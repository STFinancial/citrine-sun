package api.request;

import api.CurrencyPair;
import api.tmp_trade.CompletedTrade;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Timothy on 2/16/17.
 */
public class MoveOrderResponse extends MarketResponse {
    private final long orderNumber;
    private final Map<CurrencyPair, List<CompletedTrade>> completedTrades;

    public MoveOrderResponse(long orderNumber, Map<CurrencyPair, List<CompletedTrade>> completedTrades, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.orderNumber = orderNumber;
        this.completedTrades = completedTrades;
    }

    public long getOrderNumber() { return orderNumber; }
    public List<CompletedTrade> getCompletedTrades(CurrencyPair pair) {
        return Collections.unmodifiableList(completedTrades.get(pair));
    }
}
