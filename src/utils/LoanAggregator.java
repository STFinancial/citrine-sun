package utils;

import api.Credentials;
import api.poloniex.Poloniex;
import api.request.tmp_loan.GetActiveLoansRequest;
import api.request.tmp_loan.GetActiveLoansResponse;
import keys.KeyManager;

/**
 * Created by Zarathustra on 7/28/2017.
 */
public class LoanAggregator implements Runnable {

    public static void main(String[] args) {
        LoanAggregator l = new LoanAggregator();
        l.run();
    }

    @Override
    public void run() {
        Poloniex p = new Poloniex(Credentials.fromFileString(KeyManager.getKeyForMarket("Poloniex", KeyManager.Machine.DESKTOP)));
        GetActiveLoansResponse r = (GetActiveLoansResponse) p.processMarketRequest(new GetActiveLoansRequest());
    }

}
