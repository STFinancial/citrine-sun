package api.book;

import api.tmp_trade.Trade;

/**
 * Created by Timothy on 8/11/17.
 */
public class OrderBookUpdate {
    private final UpdateType type;

    /* Cancel Parameters */
    private String id;

    /* Insert/Update Parameters */
    private Trade trade;

    private OrderBookUpdate(UpdateType type) {
        this.type = type;
    }

    public static OrderBookUpdate removeOrder(String id) {
        OrderBookUpdate u = new OrderBookUpdate(UpdateType.REMOVE);
        u.id = id;
        return u;
    }

    public static OrderBookUpdate insertOrder(String id, Trade t) {
        OrderBookUpdate u = new OrderBookUpdate(UpdateType.INSERT);
        u.trade = t;
        return u;
    }

    public static OrderBookUpdate updateOrder(String id, Trade newTrade) {
        OrderBookUpdate u = new OrderBookUpdate(UpdateType.UPDATE);
        u.trade = newTrade;
        return u;
    }

    UpdateType getType() { return type; }
    String getId() { return id; }
    Trade getTrade() { return trade; }
}
