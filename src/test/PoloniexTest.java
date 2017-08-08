package test;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import keys.KeyManager;

/**
 * Created by Timothy on 7/24/17.
 */
public class PoloniexTest {
    // TODO(stfinanical): Create a more formal way of testing rewriters and such.
    public static void main(String[] args) {
        PoloniexTest test = new PoloniexTest();
        test.run();
    }

    public void run() {
        Poloniex p = new Poloniex(Credentials.fromFileString(KeyManager.getKeyForMarket("Poloniex", KeyManager.Machine.LAPTOP)));
//        TradeHistoryRequest r = new TradeHistoryRequest(0, 0);

        TradeRequest r = new TradeRequest(new Trade(1, 0.1, CurrencyPair.of(Currency.XMR, Currency.BTC), TradeType.BUY));
        r.setIsMargin(true);
        MarketResponse resp = p.processMarketRequest(r);
        resp = p.processMarketRequest(new OrderTradesRequest(((TradeResponse) resp).getOrderNumber()));
//        System.out.println(resp.getJsonResponse());
//        resp = p.processMarketRequest(new MoveOrderRequest(((TradeResponse) resp).getOrderNumber(), 0.2));
        System.out.println(resp.getJsonResponse());
    }
}
