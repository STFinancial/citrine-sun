package utils;

import api.AccountType;
import api.Credentials;
import api.poloniex.Poloniex;
import api.request.AccountBalanceRequest;
import keys.KeyManager;

/**
 * Created by Zarathustra on 7/28/2017.
 */
public class BitcoinCashSeller implements Runnable {

    public static void main(String[] args) {
        BitcoinCashSeller b = new BitcoinCashSeller();
        b.run();
    }

    @Override
    public void run() {
        Poloniex p = new Poloniex(Credentials.fromFileString(KeyManager.getKeyForMarket("Poloniex", KeyManager.Machine.DESKTOP)));
        AccountBalanceRequest r = new AccountBalanceRequest(AccountType.ALL);

    }
}
