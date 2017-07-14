package test;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.bitfinex.Bitfinex;
import api.request.MarketResponse;
import api.request.TickerRequest;
import api.request.TickerResponse;

import java.util.Arrays;

/**
 * Created by Timothy on 7/14/17.
 */
public class BitfinexTest {
    private static final String API_KEYS = "";

    public static void main(String[] args) {
        BitfinexTest b = new BitfinexTest();
        b.test();
    }

    private void test() {
        Bitfinex b = new Bitfinex(Credentials.publicOnly());
        TickerRequest r = new TickerRequest(Arrays.asList(CurrencyPair.of(Currency.BTC, Currency.USD), CurrencyPair.of(Currency.ETH, Currency.BTC)), 1, 1);

        MarketResponse resp = b.processMarketRequest(r);
        System.out.println(resp.getJsonResponse());
        ((TickerResponse) resp).getTickers().forEach((pair, ticker)-> {
            System.out.println(ticker.toString());
        });
    }
}
