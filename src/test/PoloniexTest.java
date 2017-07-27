package test;

import api.Credentials;
import api.poloniex.Poloniex;
import api.request.MarketResponse;
import api.request.TradeHistoryRequest;
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
        TradeHistoryRequest r = new TradeHistoryRequest(0, 0);

        MarketResponse resp = p.processMarketRequest(r);
        System.out.println(resp.getJsonResponse());
    }
}
