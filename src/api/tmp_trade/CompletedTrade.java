package api.tmp_trade;

import java.math.BigDecimal;

/**
 * Created by Timothy on 1/6/17.
 */
public final class CompletedTrade {
    // TODO(stfinancial): If we're paying attention, we may be able to match this with an order/timestamp.

    public enum Category {
        EXCHANGE, SETTLEMENT, MARGIN;
    }

    /* Required Params */
    private final Trade trade;
    private final String tradeId;
    private final long completionTimestamp;

    /* Optional Params */
    private final String globalTradeId;
    private final double total;
    private final double fee;
    private final boolean isMake; // Whether this trade was a market make or take. Potentially make this an enum as well. Perhaps LiquidityType or something?
    // TODO(stfinancial): Should this be margin for margin sales and exchange for normal sales?
    private final Category category;
    // I believe this is used to get all trades for a given order number.
    private final int orderNumber; // Not sure if this is the same as the orderNumber in TradeOrder or not.

    private CompletedTrade(Builder builder) {
        this.trade = builder.trade;
        this.tradeId = builder.tradeId;
        this.completionTimestamp = builder.completionTimestamp;
        this.globalTradeId = builder.globalTradeId;
        this.total = builder.total;
        this.fee = builder.fee;
        this.isMake = builder.isMake;
        this.category = builder.category;
        this.orderNumber = builder.orderNumber;
    }

    /* Getters */
    public Trade getTrade() { return trade; }
    public String getId() { return tradeId; }
    public long getCompletionTimestamp() { return completionTimestamp; }
    public String getGlobalTradeId() { return globalTradeId; }
    public double getTotal() { return total; }
    public double getFee() { return fee; }
    public boolean isMake() { return isMake; }
    public Category getCategory() { return category; }
    public int getOrderNumber() { return orderNumber; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("\ttradeId: " + tradeId).append("\n");
        sb.append("\tcompletionTimestamp: " + completionTimestamp).append("\n");
        sb.append("\ttrade " + trade.toString().replace("\n","\n\t")).append("\n");
        sb.append("\tglobalTradeId: " + globalTradeId).append("\n");
        sb.append("\ttotal: " + total).append("\n");
        sb.append("\tfee: " + fee).append("\n");
        sb.append("\tisMake: " + (isMake ? "true" : "false")).append("\n");
        if (category != null) {
            sb.append("\tcategory: " + category.toString()).append("\n");
        }
        sb.append("\torderNumber: " + orderNumber).append("\n");
        sb.append("}");
        return sb.toString();
    }

    public static class Builder {
        private final Trade trade;
        private final String tradeId;
        private final long completionTimestamp;

        /* Optional Params */
        private String globalTradeId;
        private Category category;
        private double total;
        private double fee;
        private boolean isMake;
        private int orderNumber;

        public Builder(Trade trade, String tradeId, long completionTimestamp) {
            this.trade = trade;
            this.tradeId = tradeId;
            this.completionTimestamp = completionTimestamp;
        }

        public Builder category(Category category) { this.category = category; return this; }

        public Builder globalTradeId(String globalTradeId) { this.globalTradeId = globalTradeId; return this; }

        public Builder total(double total) { this.total = total; return this; }

        public Builder fee(double fee) { this.fee = fee; return this; }

        public Builder isMake(boolean isMake) { this.isMake = isMake; return this; }

//        public Builder orderNumber(int orderNumber) { this.orderNumber = orderNumber; return this; } // TODO(stfinancial): Where is this?

        public CompletedTrade build() { return new CompletedTrade(this); }
    }
}
