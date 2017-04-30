package api.request;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Returned after processing a {@link MarketRequest}. Contains a JSON response and the {@link MarketRequest}.
 */
// TODO(stfinancial): Does it make sense that MarketRequest is abstract but this is not?
public class MarketResponse {
    // TODO(stfinancial): Consider refactoring to return JSON instead of actual objects. Similar for MarketRequest.


    // TODO(stfinancial): Consider optional payload.


    // TODO(stfinancial): Use a promise instead of success/error?

    protected final JsonNode jsonResponse;
    protected final MarketRequest request;
    protected final long timestamp; // TODO(stfinancial): Potentially rename this to responseTimestamp to be more clear.
    // TODO(stfinancial): Rename this to status.
    protected final RequestStatus error; // TODO(stfinancial): Does this need to be final or not?

    // TODO(stfinancial): Consider consolidating these into a single method.
    // TODO(stfinancial): Need a way to determine if something is an error.
    public MarketResponse(JsonNode jsonResponse, MarketRequest request, long timestamp, RequestStatus error) {
        this.jsonResponse = jsonResponse;
//        this.payload = payload;
        this.request = request;
        this.timestamp = timestamp;
        this.error = error;
    }

    // TODO(stfinancial): Static factory method to create error with a certain timestamp perhaps. Not sure what the benefit is though.


    public final JsonNode getJsonResponse() { return jsonResponse; }
//    @Nullable
//    public Object getPayload() { return payload; } // TODO(stfinancial): Thinking that we could have this method with Optional<T> or something... not sure this is easier than casting.
    public final MarketRequest getRequest() { return request; }
    public final long getTimestamp() { return timestamp; }
    public final boolean isSuccess() { return error.getType() == StatusType.SUCCESS; } // TODO(stfinancial): Should this be isError instead? Logic might be easier since we generally only want to change behavior on error.
    public final RequestStatus getError() { return error; }

//    public abstract Class<? extends MarketRequest> getRequestType(); // Maybe requesttype
}
