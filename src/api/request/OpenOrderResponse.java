package api.request;

import api.CurrencyPair;
import api.tmp_trade.TradeOrder;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO(stfinancial): Rename to show that this is specific to trades.
/**
 * {@code MarketResponse} containing the set of {@link TradeOrder TradeOrders}.
 */
public class OpenOrderResponse extends MarketResponse {
    // TODO(stfinancial): This should be a map from currencypair to list of trade order, but how do we handle all
    private Map<CurrencyPair, List<TradeOrder>> orders;
    private Map<String, TradeOrder> ordersByKey;

    // TODO(stfinancial): We need fields for limit, loansavailable(?) [this is probably stuff that happens when there are insufficient loans at our price], and stoplimit.

    // TODO(stfinancial): Make this constructor consistent. Are the non superclass parameters first or last?
    public OpenOrderResponse(Map<CurrencyPair, List<TradeOrder>> orders, JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        super(jsonResponse, request, timestamp, error);
        this.orders = Collections.unmodifiableMap(orders);
    }

    public Map<CurrencyPair, List<TradeOrder>> getOpenOrders() {
        return orders;
    }

    public Map<String, TradeOrder> getOpenOrdersById() {
        if (ordersByKey == null) {
            ordersByKey = new HashMap<>();
            orders.forEach((pair, orderList) -> {
                orderList.forEach((order) -> ordersByKey.put(order.getOrderId(), order));
            });
            ordersByKey = Collections.unmodifiableMap(ordersByKey); // Does this make sense?
        }
        return ordersByKey;
    }
}
