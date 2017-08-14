package api;

import api.request.MarketRequest;

/**
 * Created by Timothy on 7/19/17.
 */
public interface RequestRewriter {
    // TODO(stfinancial): Potentially make a default implementation of this, it's a little awkward to have all the rest of the methods have a default implementation though.
    RequestArgs rewriteRequest(MarketRequest request);
}
