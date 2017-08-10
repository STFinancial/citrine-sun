package test;

import api.AccountType;
import api.Currency;
import api.CurrencyPair;
import api.bittrex.Bittrex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import keys.KeyManager;

import java.util.Arrays;

/**
 * Created by Timothy on 8/9/17.
 */
public class BittrexTest {

    public static void main(String[] args) {
        BittrexTest b = new BittrexTest();
        b.test();
    }

    private void test() {
        MarketRequest r;
        MarketResponse response;
        Bittrex b = new Bittrex(KeyManager.getCredentialsForMarket("Bittrex", KeyManager.Machine.LAPTOP));
        r = new TradeRequest(new Trade(1, 0.01, CurrencyPair.of(Currency.LTC, Currency.BTC), TradeType.BUY));
        response = b.processMarketRequest(r);
        r = new OpenOrderRequest();
//        r = new OrderBookRequest(CurrencyPair.of(Currency.LTC, Currency.BTC), 50);
//        r = new AccountBalanceRequest(AccountType.EXCHANGE);
//        r = new TickerRequest(Arrays.asList(CurrencyPair.of(Currency.NEO, Currency.BTC)));
//
//        response = b.processMarketRequest(r);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
//
//        r = new CancelRequest(((TradeResponse) response).getOrderNumber(), CancelRequest.CancelType.TRADE);
        response = b.processMarketRequest(r);

        System.out.println(response.getJsonResponse());
    }
}
