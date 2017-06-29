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
    private static final double PRICE = 0.1;
    private static final double AMOUNT = 65;
    private static final TradeType TYPE = TradeType.SELL;
    private static final boolean IS_MARGIN = true;
    private static final TradeRequest.TimeInForce TIME_IN_FORCE = TradeRequest.TimeInForce.IMMEDIATE_OR_CANCEL;
    private static final CurrencyPair PAIR = CurrencyPair.of(Currency.ETH, Currency.BTC);

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
        req.setTimeInForce(TradeRequest.TimeInForce.IMMEDIATE_OR_CANCEL);
        MarketResponse resp = polo.processMarketRequest(req);
        System.out.println(resp.getJsonResponse().toString());
        if (!resp.isSuccess()) {
            System.out.println("Error: " + resp.getRequest().toString());
            return;
        }
        System.out.println(resp.toString());
        System.out.println(System.currentTimeMillis());
    }
}
