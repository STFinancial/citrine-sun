package test;

import api.Currency;
import api.CurrencyPair;
import api.alert.PriceAlert;

/**
 * Created by Timothy on 1/17/17.
 */
public class alerttest {
    public static void main(String[] args) {
        alerttest t = new alerttest();
        t.test();
    }

    private void test() {
        System.out.println();
        PriceAlert a = new PriceAlert("./data/sounds/alerts/bike_horn.wav", 0, CurrencyPair.of(Currency.BTC, Currency.XMR), null);
        a.playAlert();
    }
}
