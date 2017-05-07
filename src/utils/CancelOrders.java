package utils;

import api.Credentials;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.CancelRequest;
import api.request.MarketResponse;
import api.request.OpenOrderRequest;
import api.request.OpenOrderResponse;
import api.tmp_trade.TradeOrder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static api.Currency.*;

/**
 * Created by Timothy on 3/31/17.
 */
class CancelOrders {
    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
//    private static final String API_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";
    private static final CurrencyPair PAIR = CurrencyPair.of(MAID, BTC);

    public static void main(String[] args) {
        CancelOrders o = new CancelOrders();
        o.run();
    }

    private void run() {
        Credentials c = Credentials.fromFileString(API_KEYS);
        Poloniex p = new Poloniex(c);
        MarketResponse r = p.processMarketRequest(new OpenOrderRequest(1, 1, PAIR));
        if (!r.isSuccess()) {
            System.out.println("Failure to get Orders: " + r.getJsonResponse());
            return;
        }
        System.out.println(r.getJsonResponse());
        Map<CurrencyPair, List<TradeOrder>> orders = ((OpenOrderResponse) r).getOpenOrders();

        orders.getOrDefault(PAIR, Collections.emptyList()).forEach((order) -> {
            p.processMarketRequest(new CancelRequest(order.getOrderId(), CancelRequest.CancelType.TRADE, 1, 1));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

            }
        });
    }
}
