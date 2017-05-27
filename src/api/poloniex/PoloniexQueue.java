package api.poloniex;

import api.request.MarketRequest;
import api.request.MarketResponse;

import java.util.concurrent.*;

/**
 * Created by Timothy on 11/27/16.
 */
class PoloniexQueue { //implements Runnable {
    // Number of requests per second as limited by the API.
    private static final int REQUESTS_PER_SECOND = 6;

    PriorityBlockingQueue<MarketRequest> requestQueue;
    private QueueStrategy strategy;
    private long timeout;
    private ExecutorService workers;

    PoloniexQueue(QueueStrategy strategy, long timeout) {
        this.strategy = strategy;
        this.timeout = timeout;
        requestQueue = new PriorityBlockingQueue<>();
//        workers = Executors.newFixedThreadPool((int) (REQUESTS_PER_SECOND / (timeout / 1000)));
    }

//    Future<? extends MarketResponse> process(MarketRequest request) {
//        workers.submit()
//    }

    void setStrategy(QueueStrategy strategy) {
        this.strategy = strategy;
    }

    QueueStrategy getStrategy() {
        return strategy;
    }

    void setTimeout(long timeout) {
        // Change thread pool size?
        this.timeout = timeout;
    }

    long getTimeout() {
        return timeout;
    }


}
