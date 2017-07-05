package api.krakentest;

import api.QueueStrategy;
import api.request.MarketResponse;

import java.util.concurrent.*;

/**
 * Created by Timothy on 7/1/17.
 */
class KrakenQueueTest implements Runnable {
    private ExecutorService workers;
    private QueueStrategy strategy;
    PriorityBlockingQueue<KrakenTest.KrakenTestWorkItem> requestQueue;

    // TODO(stfinancial): Implement other queue strategies.
    KrakenQueueTest(QueueStrategy strategy) {
        this.strategy = strategy;
        // TODO(stfinancial): May want to call the constructor directly to be able to handle errors and return a bad market response.
        workers = Executors.newFixedThreadPool(5);
        requestQueue = new PriorityBlockingQueue<>();
    }

    Future<MarketResponse> submitWorkItem(KrakenTest.KrakenTestWorkItem item) {
        // TODO(stfinancial): What do we do if the queue is full and want to immediately return a future?
        // We can use a ScheduledExecutorService and if the priority is high enough, submit with delay.
        // This logic can be embedded in the queuestrategy.
        // Will need an additional data structure to maintain delayed requests, so we can cancel and reschedule in case a high priority one comes along
        // But then how do we get the Future to the client? Either we disallow this, or make use of the wrapper class for MarketResponse and simple replace the future inside that class.
        // If it is well encapsulated enough, this should not be bug prone.
        // For now, we will ignore these cases though, and just perform work on a best effort basis.
        return workers.submit(item);
    }

    @Override
    public void run() {
        // TODO(stfinancial): We are currently only supporting STRICT mode for the queue.
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Interrupted... who knows?");
            }
        }
    }
}
