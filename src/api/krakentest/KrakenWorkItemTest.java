package api.krakentest;

import api.request.MarketRequest;
import api.request.MarketResponse;

import java.util.concurrent.Callable;

/**
 * Created by Timothy on 7/1/17.
 */
// TODO(stfinancial): This would probably be a static inner class of the market.
public class KrakenWorkItemTest implements Callable<MarketResponse> {


    KrakenWorkItemTest(MarketRequest req) {

    }

    @Override
    public MarketResponse call() throws Exception {
        return null;
    }
}
