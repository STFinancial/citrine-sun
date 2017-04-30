package utils;

import api.*;
import api.poloniex.Poloniex;
import api.request.MarketResponse;
import api.request.TradeRequest;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;

/**
 * Created by Timothy on 1/7/17.
 */
public class TradeTester {
//    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
    private static final String API_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";
    private static final double PRICE = 0.01;
    private static final double AMOUNT = 1;
    private static final TradeType TYPE = TradeType.BUY;
    private static final boolean IS_MARGIN = true;
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.LTC, Currency.BTC);

    public static void main(String[] args) {
        TradeTester t = new TradeTester();
        t.run();
    }

    private void run() {
        Credentials c = Credentials.fromFileString(API_KEYS);
        Poloniex polo = new Poloniex(c);
        Trade t = new Trade(AMOUNT, PRICE, PAIR, TYPE);
        TradeRequest req = new TradeRequest(t, 1, System.currentTimeMillis());
        req.setIsMargin(IS_MARGIN);
        MarketResponse resp = polo.processMarketRequest(req);
        System.out.println(resp.getJsonResponse().toString());
        if (!resp.isSuccess()) {
            System.out.println("Error: " + ((TradeRequest) resp.getRequest()).toString());
            return;
        }
        System.out.println(resp.toString());
        System.out.println(System.currentTimeMillis());
    }
}
