package strategy;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.AccountBalanceRequest;
import api.request.AccountBalanceResponse;
import api.request.MarketResponse;
import keys.KeyManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static api.Currency.*;

/**
 * Created by Timothy on 7/28/17.
 */
public class CandleCatcher2 extends Strategy {
    // Construct Market and obtain account balances. x
    // Get Tickers for each currency pair
    // Place trades below according to what fraction we're using.
    // Sleep and move the trades as needed.
    // Log the orderid of the trades we've made.
    private static final AccountType ACCOUNT_TYPE = AccountType.EXCHANGE;

    List<CurrencyPair> PAIRS = Collections.unmodifiableList(Arrays.asList(
            CurrencyPair.of(LTC, BTC), CurrencyPair.of(XRP, BTC), CurrencyPair.of(ETH, BTC), CurrencyPair.of(DASH, BTC), CurrencyPair.of(XLM, BTC),
            CurrencyPair.of(DOGE, BTC), CurrencyPair.of(GNT, BTC), CurrencyPair.of(BTS, BTC), CurrencyPair.of(MAID, BTC), CurrencyPair.of(CLAM, BTC)
    ));

    public static void main(String[] args) {
        CandleCatcher2 c = new CandleCatcher2();
        c.run();
    }

    @Override
    public void run() {
        MarketResponse response;
        Poloniex p = new Poloniex(Credentials.fromFileString(KeyManager.getKeyForMarket("Poloniex", KeyManager.Machine.LAPTOP)));
        AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(ACCOUNT_TYPE);
        do {
            response = p.processMarketRequest(accountBalanceRequest);
        } while (!response.isSuccess());
        Map<Currency, Double> balances = ((AccountBalanceResponse) response).getBalances().getOrDefault(ACCOUNT_TYPE, Collections.emptyMap());

    }
}
