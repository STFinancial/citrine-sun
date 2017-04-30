package api;

import api.request.MarketRequest;
import api.request.MarketResponse;

import java.util.concurrent.*;
//
//// TODO(stfinancial): Do we actually need the generics here? OR can we we just infer from the request type that is passed in.
//// TODO(stfinancial): Refactor this into a class that handles the entire cache with a single pool.
//public class CachedResource<? extends MarketResponse> implements Runnable {
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//    private final Market api;
//    private final MarketRequest request;
//    // TODO(stfinancial): How do we know what type this is?
//    private T response;
//
//    CachedResource(Market api, MarketRequest request, long period) {
//        this.api = api;
//        this.request = request;
//        scheduler.scheduleWithFixedDelay(this, 0, period, TimeUnit.SECONDS);
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public void run() {
//        MarketResponse r  = api.processMarketRequest(request);
//        if (r.isSuccess()) {
//            this.response = (r.getClass()) r;
//        }
//        this.response = (T) api.processMarketRequest(request);
//    }
//
//    public T get() {
//        return response;
//    }
//}