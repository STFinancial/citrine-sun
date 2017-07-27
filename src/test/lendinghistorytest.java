package test;

import api.Credentials;
import api.poloniex.Poloniex;
import api.request.MarketResponse;
import api.request.tmp_loan.GetLendingHistoryRequest;
import api.request.tmp_loan.GetLendingHistoryResponse;

/**
 * Created by Timothy on 4/13/17.
 */
public class lendinghistorytest {

//    private static final String API_KEYS = "/Users/Timothy/Documents/Keys/main_key.txt";
private static final String API_KEYS = "F:\\Users\\Zarathustra\\Documents\\main_key.txt";

    public static void main(String[] args) {
        Credentials c = Credentials.fromFileString(API_KEYS);
        Poloniex p = new Poloniex(c);
        long end = System.currentTimeMillis();
        long start = end - (3000 * 1000);
//        System.out.println()
        GetLendingHistoryRequest r = new GetLendingHistoryRequest(start, end);
        MarketResponse resp = p.processMarketRequest(r);
        System.out.println(resp.getJsonResponse().toString());
        System.out.println(((GetLendingHistoryResponse)resp).getLoans().size());
        resp.getJsonResponse().forEach((loan) -> {
            System.out.println("Mawp");
        });
    }
}
