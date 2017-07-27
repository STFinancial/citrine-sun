package utils;

import api.Credentials;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.CancelRequest;
import api.request.MarketResponse;
import api.request.OpenOrderRequest;
import api.request.OpenOrderResponse;
import api.tmp_trade.TradeOrder;
import api.tmp_trade.TradeType;
import keys.KeyManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static api.Currency.*;

/**
 * Cancels all orders for a given {@link CurrencyPair} on {@link Poloniex}.
 */
class CancelOrders {
    private static final CurrencyPair PAIR = CurrencyPair.of(XMR, BTC);
    private static final boolean RESTRICT_TO_TYPE = true;
    private static final TradeType TYPE = TradeType.BUY;
    private static final boolean ENABLE_CANCEL_ABOVE = false;
    private static final double CANCEL_ABOVE_PRICE = 0.000101;
    private static final boolean ENABLE_CANCEL_BELOW = false;
    private static final double CANCEL_BELOW_PRICE = 0.000104;

    public static void main(String[] args) {
        CancelOrders o = new CancelOrders();
        o.run();
    }

    private void run() {
        Credentials c = Credentials.fromFileString(KeyManager.getKeyForMarket("Poloniex", KeyManager.Machine.DESKTOP));
        Poloniex p = new Poloniex(c);
        MarketResponse r = p.processMarketRequest(new OpenOrderRequest(PAIR));
        if (!r.isSuccess()) {
            System.out.println("Failure to get Orders: " + r.getJsonResponse());
            return;
        }
        System.out.println(r.getJsonResponse());
        Map<CurrencyPair, List<TradeOrder>> orders = ((OpenOrderResponse) r).getOpenOrders();

        orders.getOrDefault(PAIR, Collections.emptyList()).forEach((order) -> {
            if (RESTRICT_TO_TYPE && order.getTrade().getType() != TYPE) return;
            if (ENABLE_CANCEL_ABOVE && order.getTrade().getRate() <= CANCEL_ABOVE_PRICE) return;
            if (ENABLE_CANCEL_BELOW && order.getTrade().getRate() >= CANCEL_BELOW_PRICE) return;
            System.out.println(p.processMarketRequest(new CancelRequest(order.getOrderId(), CancelRequest.CancelType.TRADE)).getJsonResponse());
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {}
        });
    }
}
