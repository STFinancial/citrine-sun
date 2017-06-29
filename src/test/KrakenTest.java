package test;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.kraken.Kraken;
import api.request.AccountBalanceRequest;
import api.request.MarketResponse;
import api.request.OrderBookRequest;
import api.request.TickerRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Timothy on 6/17/17.
 */
public class KrakenTest {
    private static final String KRAKEN_KEYS = "/Users/Timothy/Documents/Keys/kraken_key.txt";

    public static void main(String[] args) {
        KrakenTest t = new KrakenTest();
        t.test();
    }

    public void test() {
        Credentials c = Credentials.fromFileString(KRAKEN_KEYS);
        Kraken k = new Kraken(c);
        AccountBalanceRequest r = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);
//        OrderBookRequest r = new OrderBookRequest(CurrencyPair.of(Currency.DASH, Currency.BTC), 5, 1, 1);
//        List<CurrencyPair> pairs = new ArrayList<>();
//        pairs.add(CurrencyPair.of(Currency.DASH, Currency.BTC));
//        pairs.add(CurrencyPair.of(Currency.BTC, Currency.USD));
//        TickerRequest r = new TickerRequest(pairs, 1, 1);
        MarketResponse resp = k.processMarketRequest(r);
        System.out.println(resp.getJsonResponse());
    }
}
