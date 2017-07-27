package test;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.gdax.Gdax;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import keys.KeyManager;

import java.util.Arrays;

/**
 * Created by Zarathustra on 4/26/2017.
 */
public class GdaxTest {
    public static void main(String[] args) {
        GdaxTest t = new GdaxTest();
        t.test();
    }

    public void test() {
        Credentials c = Credentials.fromFileString(KeyManager.getKeyForMarket("Gdax", KeyManager.Machine.LAPTOP));
        Gdax gdax = new Gdax(c);
//        TradeRequest req = new TradeRequest(new Trade(1, 1500, CurrencyPair.of(Currency.BTC, Currency.USD), TradeType.BUY));
//        req.setIsPostOnly(false);
        AssetPairRequest req = new AssetPairRequest();
//        TickerRequest req = new TickerRequest(Arrays.asList(new CurrencyPair[]{CurrencyPair.of(Currency.ETH, Currency.BTC)}));
        MarketResponse resp = gdax.processMarketRequest(req);
        System.out.println(resp.getJsonResponse());
//        if (!resp.isSuccess()) {
//            System.out.println("Failure: " + resp.getJsonResponse().toString());
//            return;
//        }
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        resp = gdax.processMarketRequest(new CancelRequest(((TradeResponse) resp).getOrderNumber(), CancelRequest.CancelType.TRADE));
//        System.out.println(resp.getJsonResponse().toString());
//
//        resp = gdax.processMarketRequest(new OrderBookRequest(CurrencyPair.of(Currency.LTC, Currency.BTC), 50));
//        System.out.println(resp.getJsonResponse().toString());
    }

}
