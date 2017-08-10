package test;

import api.Currency;
import api.CurrencyPair;
import api.bittrex.Bittrex;
import api.request.MarketResponse;
import api.request.TickerRequest;
import api.request.TradeRequest;
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
        Bittrex b = new Bittrex(KeyManager.getCredentialsForMarket("Bittrex", KeyManager.Machine.LAPTOP));
//        TickerRequest r = new TickerRequest(Arrays.asList(CurrencyPair.of(Currency.NEO, Currency.BTC)));
        TradeRequest r = new TradeRequest(new Trade(50, 0.00001, CurrencyPair.of(Currency.GNT, Currency.BTC), TradeType.BUY));

        MarketResponse response = b.processMarketRequest(r);
        System.out.println(response.getJsonResponse());
    }
}
