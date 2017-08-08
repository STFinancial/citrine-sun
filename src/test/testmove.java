package test;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.TradeRequest;
import api.request.MoveOrderRequest;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Timothy on 2/16/17.
 */
public class testmove {
    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";

    public static void main(String[] args) {
        testmove t = new testmove();
        t.run();
    }

    private void run() {
        Credentials c = Credentials.fromFileString(API_KEYS);
        Poloniex polo = new Poloniex(c);

        TradeRequest t = new TradeRequest(new Trade(1, 0.00015000, CurrencyPair.of(Currency.MAID, Currency.BTC), TradeType.BUY));
        t.setIsMargin(true);
        JsonNode j = polo.processMarketRequest(t).getJsonResponse();


        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        polo.processMarketRequest(new MoveOrderRequest(j.get("orderNumber").asText(), 0.0001750));
    }
}
