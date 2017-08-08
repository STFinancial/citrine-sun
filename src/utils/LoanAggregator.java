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
        Map<Currency, LoanStats> usedTotals = new HashMap<>();
        r.getUsed().forEach((c, l) -> {
            l.forEach((loan) -> {
                LoanStats s = usedTotals.getOrDefault(c, new LoanStats());
                s.num++;
                s.total += loan.getLoan().getAmount();
                s.weightedRate += loan.getLoan().getRate() * loan.getLoan().getAmount();
                usedTotals.put(c, s);
            });
        });
        usedTotals.forEach((c, t) -> {
            System.out.println(c + ": total - " + t.total + " num - " + t.num + " avg - " + t.weightedRate / t.total);
        });
    }

    private class LoanStats {
        private double total;
        private double weightedRate;
        private int num;
    }

}
