package test;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.kraken.Kraken;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import keys.KeyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Timothy on 6/17/17.
 */
public class KrakenTest {
    public static void main(String[] args) {
        KrakenTest t = new KrakenTest();
        t.test();
    }

    public void test() {
        Credentials c = Credentials.fromFileString(KeyManager.getKeyForMarket("Kraken", KeyManager.Machine.LAPTOP));
        Kraken k = new Kraken(c);
//        OpenOrderRequest r = new OpenOrderRequest(1, 1);
//        FeeRequest r = new FeeRequest(new HashSet<>(Arrays.asList(new CurrencyPair[]{CurrencyPair.of(Currency.DASH, Currency.BTC), CurrencyPair.of(Currency.EOS, Currency.ETH)})));
//        FeeRequest r = new FeeRequest(new HashSet<>(Arrays.asList(new CurrencyPair[]{CurrencyPair.of(Currency.ETH, Currency.USD)})));

//        TradeRequest r = new TradeRequest(new Trade(0.01, 0.01, CurrencyPair.of(Currency.LTC, Currency.BTC), TradeType.BUY));
        TradeHistoryRequest r = new TradeHistoryRequest(0, System.currentTimeMillis());
//        AccountBalanceRequest r = new AccountBalanceRequest(AccountType.EXCHANGE);
//        OrderBookRequest r = new OrderBookRequest(CurrencyPair.of(Currency.DASH, Currency.USD), 5);
//        List<CurrencyPair> pairs = new ArrayList<>();
//        pairs.add(CurrencyPair.of(Currency.DASH, Currency.BTC));
//        pairs.add(CurrencyPair.of(Currency.BTC, Currency.USD));
//        TickerRequest r = new TickerRequest(pairs);
        MarketResponse resp = k.processMarketRequest(r);
        System.out.println(resp.getJsonResponse());
//        CancelRequest can = new CancelRequest(((TradeResponse) resp).getOrderNumber(), CancelRequest.CancelType.TRADE);
//        resp = k.processMarketRequest(can);
//        System.out.println(resp.getJsonResponse());
    }
}
