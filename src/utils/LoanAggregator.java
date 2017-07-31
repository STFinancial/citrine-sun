package utils;

import api.Currency;
import api.poloniex.Poloniex;
import api.request.tmp_loan.GetActiveLoansRequest;
import api.request.tmp_loan.GetActiveLoansResponse;
import keys.KeyManager;

import java.util.HashMap;
import java.util.Map;

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
        Poloniex p = new Poloniex(KeyManager.getCredentialsForMarket("Poloniex", KeyManager.Machine.DESKTOP));
        GetActiveLoansResponse r = (GetActiveLoansResponse) p.processMarketRequest(new GetActiveLoansRequest());
        Map<Currency, Double> usedTotals = new HashMap<>();
        r.getUsed().forEach((c, l) -> {
            l.forEach((loan) -> {
                usedTotals.put(c, usedTotals.getOrDefault(c, 0.0) + loan.getLoan().getAmount());
            });
        });
        usedTotals.forEach((c, t) -> {
            System.out.println(c + ": " + t);
        });
    }

}
