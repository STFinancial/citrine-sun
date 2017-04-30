package api.request.tmp_loan;

import api.request.MarketRequest;

/**
 * Created by Timothy on 1/28/17.
 */
public final class GetLendingHistoryRequest extends MarketRequest {
    // TODO(stfinancial): Optional limit parameter to limit number of rows returned.

    // TODO(stfinancial): This should be in milliseconds
    private final long start;
    private final long end;

    // TODO(stfinancial): Potentially take a Currency here.
    public GetLendingHistoryRequest(long start, long end, int priority, long timestamp) {
        super(priority, timestamp);
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
