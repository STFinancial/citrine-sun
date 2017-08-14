package api.book;

import java.util.Map;

/**
 * Created by Timothy on 8/11/17.
 */
public class OrderBook {

//    Map<String, Order> orders;

    public void update(OrderBookUpdate update) {
        switch (update.getType()) {
            case INSERT:
                break;
            case REMOVE:
                break;
            case UPDATE:
                break;
            default:
                System.out.println("Invalid UpdateType: " + update.getType());
                return;
        }
    }
}
