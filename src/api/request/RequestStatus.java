package api.request;

/**
 * Contains a {@link StatusType} as well as a message used in a {@link MarketResponse}.
 */
public class RequestStatus {
    private static final RequestStatus EMPTY_SUCCESS = new RequestStatus(StatusType.SUCCESS);

    // TODO(stfinancial): Are there actually cases where we actually return a non-success outside of MarketResponse base classes.

    private StatusType type;
    private Exception exception;
    private String message;

    public RequestStatus(StatusType type) {
        this.type = type;
    }

    public RequestStatus(StatusType type, String message) { this.type = type; this.message = message; }

    // TODO(stfinancial): Maybe just remove this?
    // TODO(stfinancial): Need to be consistent about this. Should we set the message and exception as null or just call the above constructor in the case where there is no exception.
    public RequestStatus(StatusType type, Exception e, String message) {
        this.type = type;
        this.exception = e;
        this.message = message;
    }

    public StatusType getType() {
        return type;
    }
    public Exception getException() {
        return exception;
    }
    public String getMessage() {
        return message;
    }

    // TODO(stfinancial): Static factory methods for generic errors?
    public static RequestStatus success() { return EMPTY_SUCCESS; }
    // TODO(stfinancial): Unspported request (though maybe not because it really should say what isn't supported).

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Status - ");
        sb.append(type.toString());
        sb.append(":");
        sb.append(message);
        return sb.toString();
    }
}
