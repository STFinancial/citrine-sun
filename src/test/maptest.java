package test;

import java.util.*;

/**
 * Created by Timothy on 11/23/16.
 */
public class maptest {
    public static void main(String[] args) {
        ArrayList<String> commands = new ArrayList<>();
        HashSet<String> commandSet = new HashSet<>();
        commandSet.add("returnTicker");
        commandSet.add("return24hVolume");
        commandSet.add("returnOrderBook");
        commandSet.add("returnMarketTradeHistory");
        commandSet.add("returnLoanOrders");
        commandSet.add("returnChartData");
        commands.add("returnTicker");
        commands.add("return24hVolume");
        commands.add("returnOrderBook");
        commands.add("returnMarketTradeHistory");
        commands.add("returnLoanOrders");
        commands.add("returnChartData");
        for (int i = 0; i < 14; i++) {
            commands.add("returnBalances");
            commands.add("cancelOrder");
        }
        Random r = new Random();
        String c;
        int total = 0;
        long start = System.nanoTime();
        for (int i = 0; i < 1000000000; i++) {
            c = commands.get(r.nextInt(34));
            if (stringTest(c)) {
                total++;
            }
//            if (commandSet.contains(c)) {
//                total++;
//            }
        }
        long end = System.nanoTime();
        System.out.println((end - start) / 1000000.0);
        System.out.println(total);
    }

    private static boolean stringTest(String s) {
        return (s.equals("returnTicker") ||
                s.equals("return24hVolume") ||
                s.equals("returnOrderBook") ||
                s.equals("returnMarketTradeHistory") ||
                s.equals("returnLoanOrders") ||
                s.equals("returnChartData"));
    }
}
