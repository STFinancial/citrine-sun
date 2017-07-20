package api.krakentest;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.kraken.Kraken;
import api.request.MarketResponse;
import api.request.TickerRequest;
import test.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by Timothy on 7/1/17.
 */
public class KrakenTestTest {
    private static final String KRAKEN_KEYS = "/Users/Timothy/Documents/Keys/kraken_key.txt";
    // TODO(stfinancial): There are no actual keys in this file. Fix this.
//    private static final String KRAKEN_KEYS = "F:\\\\Users\\\\Zarathustra\\\\Documents\\\\main_key.txt";

    public static void main(String[] args) {
        KrakenTestTest t = new KrakenTestTest();
        t.test();
    }

    public void test() {
        System.out.println("Getting here.");
        System.out.println(KRAKEN_KEYS);
        Credentials c = Credentials.fromFileString(KRAKEN_KEYS);
        KrakenTest k = new KrakenTest(c);
//        AccountBalanceRequest r = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);
//        OrderBookRequest r = new OrderBookRequest(CurrencyPair.of(Currency.DASH, Currency.BTC), 5, 1, 1);
        List<CurrencyPair> pairs = new ArrayList<>();
        pairs.add(CurrencyPair.of(Currency.DASH, Currency.BTC));
        pairs.add(CurrencyPair.of(Currency.BTC, Currency.USD));
        TickerRequest r = new TickerRequest(pairs);
        Future<MarketResponse> resp;

        System.out.println("Sending request 1");
        Future<MarketResponse> r1 = k.processMarketRequest(new TickerRequest(pairs));
        System.out.println("Sending request 2");
        Future<MarketResponse> r2 = k.processMarketRequest(new TickerRequest(pairs));
        System.out.println("Sending request 3");
        Future<MarketResponse> r3 = k.processMarketRequest(new TickerRequest(pairs));
        System.out.println("Sending request 4");
        Future<MarketResponse> r4 = k.processMarketRequest(new TickerRequest(pairs));
        System.out.println("Sending request 5");
        Future<MarketResponse> r5 = k.processMarketRequest(new TickerRequest(pairs));
    }
}
