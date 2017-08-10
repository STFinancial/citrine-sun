package test;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.bitfinex.Bitfinex;
import api.request.*;
import keys.KeyManager;

import java.util.Arrays;

/**
 * Created by Timothy on 7/14/17.
 */
public class BitfinexTest {
    public static void main(String[] args) {
        BitfinexTest b = new BitfinexTest();
        b.test();
    }

    private void test() {
        Bitfinex b = new Bitfinex(Credentials.fromFileString(KeyManager.getKeyForMarket("Bitfinex", KeyManager.Machine.LAPTOP)));
//        TickerRequest r = new TickerRequest(Arrays.asList(CurrencyPair.of(Currency.BTC, Currency.USD), CurrencyPair.of(Currency.ETH, Currency.BTC)));
//        OrderBookRequest r = new OrderBookRequest(CurrencyPair.of(Currency.ETH, Currency.BTC), 30);
        AccountBalanceRequest r = new AccountBalanceRequest();

        MarketResponse resp = b.processMarketRequest(r);
        System.out.println(resp.getJsonResponse());
//        ((TickerResponse) resp).getTickers().forEach((pair, ticker)-> {
//            System.out.println(ticker.toString());
//        });
    }
}
