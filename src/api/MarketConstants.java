package api;

/**
 * Created by Timothy on 1/28/17.
 */
public abstract class MarketConstants {

    // TODO(stfinancial): Should this be here or on the market itself?
//    public abstract String getName();

    // TODO(stfinancial): Are there cases where these results are not the same?
    public abstract boolean canLend();
    public abstract boolean canMarginTrade();

    public abstract boolean canTrade();


}
