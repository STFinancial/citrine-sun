package api.request;

import api.tmp_trade.CompletedTrade;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;

/**
 * Created by Timothy on 12/28/16.
 */
public class TradeResponse extends MarketResponse {
    // TODO(stfinancial): This probably needs to turn into a builder.

    // TODO(stfinancial): We shouldn't need orderNumber as well as TradeOrder

    // TODO(stfinancial): Should this be id?
    private String orderNumber;
//    private Trade order;

    // TODO(stfinancial): Need to figure out how to handle this for markets that don't return this information.
    // TODO(stfinancial): Perhaps try a bit less information.
    private List<CompletedTrade> resultingTrades;
    private double quoteAmountFilled = 0;

    // TODO(stfinancial): Figure out how to add support for all of the GDAX stuff.

    // TODO(stfinancial): This probably requires a builder... there are too many fields...
    public TradeResponse(String orderNumber, List<CompletedTrade> resultingTrades, JsonNode jsonResponse, TradeRequest request, long timestamp, RequestStatus status) {
        super(jsonResponse, request, timestamp, status);
        this.orderNumber = orderNumber;
//        this.order = order;
        this.resultingTrades = resultingTrades;
        double amount = 0;
        for (CompletedTrade trade : resultingTrades) {
            amount += trade.getTrade().getAmount();
        }
        this.quoteAmountFilled = amount;
    }

    public TradeResponse(String orderNumber, double quoteAmountFilled, JsonNode jsonResponse, TradeRequest request, long timestamp, RequestStatus status) {
        super(jsonResponse, request, timestamp, status);
        this.orderNumber = orderNumber;
        this.quoteAmountFilled = quoteAmountFilled;
    }

    public TradeResponse(String orderNumber, JsonNode jsonResponse, TradeRequest request, long timestamp, RequestStatus status) {
        super(jsonResponse, request, timestamp, status);
        this.orderNumber = orderNumber;
    }

    public double getQuoteAmountFilled() { return quoteAmountFilled; }
    public String getOrderNumber() { return orderNumber; }
    public List<CompletedTrade> getResultingTrades() { return Collections.unmodifiableList(resultingTrades); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("\torderNumber: " + orderNumber).append("\n");
        sb.append("\ttrade " + ((TradeRequest) request).getTrade().toString().replace("\n", "\n\t")).append("\n");
        for (CompletedTrade trade : resultingTrades) {
            sb.append("\tresultingTrade " + trade.toString().replace("\n","\n\t")).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
