package utils;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import keys.KeyManager;

/**
 * Created by Timothy on 7/30/17.
 */
public class PoloCheat implements Runnable {
    // TODO(stfinancial): Doesn't work as I had hoped...

    public static void main(String[] args) {
        PoloCheat p = new PoloCheat();
        p.run();
    }

    @Override
    public void run() {
        Poloniex p = new Poloniex(Credentials.fromFileString(KeyManager.getKeyForMarket("Poloniex", KeyManager.Machine.LAPTOP)));
        TradeRequest t = new TradeRequest(new Trade(1.51, 0.02, CurrencyPair.of(Currency.MAID, Currency.BTC), TradeType.BUY));
        t.setIsMargin(true);
        t.setMaxRate(0.00001);
//        t.setTimeInForce(TradeRequest.TimeInForce.IMMEDIATE_OR_CANCEL);
        t.setTimeInForce(TradeRequest.TimeInForce.FILL_OR_KILL);
//        t.setIsPostOnly(true);
        MarketResponse r;
        String id;
        OpenOrderRequest o = new OpenOrderRequest(CurrencyPair.of(Currency.MAID, Currency.BTC));
        while (true) {
            p.processMarketRequest(t);
            sleep(200);
            r = p.processMarketRequest(o);
            System.out.println(r.getJsonResponse());
            sleep(200);
            if (r.isSuccess()) {
                ((OpenOrderResponse) r).getOpenOrders().get(CurrencyPair.of(Currency.MAID, Currency.BTC)).forEach((order) -> {
                    p.processMarketRequest(new CancelRequest(order.getOrderId(), CancelRequest.CancelType.TRADE));
                    sleep(200);
                });
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
}
