package utils;

import api.AccountType;
import api.Credentials;
import api.Currency;
import api.CurrencyPair;
import api.poloniex.Poloniex;
import api.request.*;
import api.tmp_trade.Trade;
import api.tmp_trade.TradeType;
import keys.KeyManager;

import java.util.Map;

import static api.Currency.BCH;
import static api.AccountType.*;
import static api.Currency.BTC;

/**
 * Created by Zarathustra on 7/28/2017.
 */
public class BitcoinCashSeller implements Runnable {
    private static final double SATOSHI = 0.00000001;

    public static void main(String[] args) {
        BitcoinCashSeller b = new BitcoinCashSeller();
        b.run();
    }

    @Override
    public void run() {
        Poloniex p = new Poloniex(Credentials.fromFileString(KeyManager.getKeyForMarket("Poloniex", KeyManager.Machine.DESKTOP)));
        AccountBalanceRequest r = new AccountBalanceRequest(AccountType.ALL);
        MarketResponse resp;
        AccountBalanceResponse abr;
        Map<AccountType, Map<Currency, Double>> balances;
        while (true) {
            resp = p.processMarketRequest(r);
            if (!resp.isSuccess()) continue;

            balances = ((AccountBalanceResponse) resp).getBalances();
            if (balances.get(MARGIN).getOrDefault(BCH, 0.0) != 0.0 || balances.get(LOAN).getOrDefault(BCH, 0.0) != 0.0) {
                if (balances.get(MARGIN).getOrDefault(BCH, 0.0) != 0.0) {
                    TransferBalanceRequest t = new TransferBalanceRequest(BCH, balances.get(MARGIN).get(BCH), MARGIN, EXCHANGE);
                    do { resp = p.processMarketRequest(t); } while (!resp.isSuccess());
                }
                if (balances.get(LOAN).getOrDefault(BCH, 0.0) != 0.0) {
                    TransferBalanceRequest t = new TransferBalanceRequest(BCH, balances.get(LOAN).get(BCH), LOAN, EXCHANGE);
                    do { resp = p.processMarketRequest(t); } while (!resp.isSuccess());
                }
                continue;
            }
            if (balances.get(EXCHANGE).getOrDefault(BCH, 0.0) != 0) {
                double amount = balances.get(EXCHANGE).get(BCH);
                // Set the trades.
                TradeRequest t = new TradeRequest(new Trade((amount * 0.25) - SATOSHI, 0.5, CurrencyPair.of(BCH, BTC), TradeType.SELL));
                do { resp = p.processMarketRequest(t); } while (!resp.isSuccess());
                t = new TradeRequest(new Trade((amount * 0.25) - SATOSHI, 0.35, CurrencyPair.of(BCH, BTC), TradeType.SELL));
                do { resp = p.processMarketRequest(t); } while (!resp.isSuccess());
                t = new TradeRequest(new Trade((amount * 0.25) - SATOSHI, 0.25, CurrencyPair.of(BCH, BTC), TradeType.SELL));
                do { resp = p.processMarketRequest(t); } while (!resp.isSuccess());
                t = new TradeRequest(new Trade((amount * 0.25) - SATOSHI, 0.2, CurrencyPair.of(BCH, BTC), TradeType.SELL));
                do { resp = p.processMarketRequest(t); } while (!resp.isSuccess());
                return;
            }

            try { Thread.sleep(300); } catch (InterruptedException e) {}
        }
    }
}
