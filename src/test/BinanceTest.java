package test;

import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.binance.Binance;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import keys.KeyManager;

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
        Binance b = new Binance(KeyManager.getCredentialsForMarket("Binance", KeyManager.Machine.LAPTOP));
        MarketResponse r;
        r = b.processMarketRequest(new TradeRequest(new Trade(1, 0.01, CurrencyPair.of(Currency.ETH, Currency.BTC),
                TradeType.BUY)));
//        r = b.processMarketRequest(new TickerRequest(Arrays.asList(CurrencyPair.of(Currency.RDN, Currency.BTC))));
//        r = b.processMarketRequest(new AssetPairRequest());
//        System.out.println(r.toString());
        System.out.println(r.getJsonResponse().toString());
    }
}
