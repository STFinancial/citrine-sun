package test;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.binance.Binance;
import api.request.MarketRequest;
import api.request.MarketResponse;
import api.request.TickerRequest;
import api.request.TickerResponse;

import java.util.Arrays;

/**
 * Created by Timothy on 12/27/17.
 */
public class BinanceTest {

    public static void main(String[] args) {
        BinanceTest b = new BinanceTest();
        b.test();
    }

    private void test() {
        Binance b = new Binance(Credentials.publicOnly());
        MarketResponse r = b.processMarketRequest(new TickerRequest(Arrays.asList(CurrencyPair.of(Currency.BTC, Currency.RDN))));
        System.out.println(r.toString());
        System.out.println(r.getJsonResponse().toString());
    }
}
