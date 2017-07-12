package test;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.kraken.Kraken;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Timothy on 6/17/17.
 */
public class KrakenTest {
    private static final String KRAKEN_KEYS = "/Users/Timothy/Documents/Keys/kraken_key.txt";
    // TODO(stfinancial): There are no actual keys in this file. Fix this.
//    private static final String KRAKEN_KEYS = "F:\\\\Users\\\\Zarathustra\\\\Documents\\\\main_key.txt";

    public static void main(String[] args) {
        KrakenTest t = new KrakenTest();
        t.test();
    }

    public void test() {
        Credentials c = Credentials.fromFileString(KRAKEN_KEYS);
        Kraken k = new Kraken(c);
//        OpenOrderRequest r = new OpenOrderRequest(1, 1);
//        FeeRequest r = new FeeRequest(new HashSet<>(Arrays.asList(new CurrencyPair[]{CurrencyPair.of(Currency.DASH, Currency.BTC), CurrencyPair.of(Currency.EOS, Currency.ETH)})), 1, 1);
//        FeeRequest r = new FeeRequest(new HashSet<>(Arrays.asList(new CurrencyPair[]{CurrencyPair.of(Currency.ETH, Currency.USD)})), 1, 1);

        TradeRequest r = new TradeRequest(new Trade(0.01, 0.02, CurrencyPair.of(Currency.LTC, Currency.BTC), TradeType.BUY), 1, 1);
//        AccountBalanceRequest r = new AccountBalanceRequest(AccountType.EXCHANGE, 1, 1);
//        OrderBookRequest r = new OrderBookRequest(CurrencyPair.of(Currency.DASH, Currency.USD), 5, 1, 1);
//        List<CurrencyPair> pairs = new ArrayList<>();
//        pairs.add(CurrencyPair.of(Currency.DASH, Currency.BTC));
//        pairs.add(CurrencyPair.of(Currency.BTC, Currency.USD));
//        TickerRequest r = new TickerRequest(pairs, 1, 1);
        MarketResponse resp = k.processMarketRequest(r);
        System.out.println(resp.getJsonResponse());
    }
}
