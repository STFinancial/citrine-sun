//package api.poloniex;
//
//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;
//
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//// TODO(stfinancial): Consider if we want to move nonce out of the class, make generic, and deal with nonce in sendRequest.
//final class RequestArgs {
//    // TODO(stfinancial): Improve this... maybe allow a message or something, throw an exception. idk...
//    private static final RequestArgs UNSUPPORTED = new RequestArgs("<<< *UNSUPPORTED* >>>");
//    // TODO(stfinancial): The question is if we want to allow setting the nonce in the constructor, if we should do it on construction of the class, or allow setting it after the fact.
//
//    private final String command;
//    private HashMap<String, String> args;
//    private long nonce;
//
//    RequestArgs(String command) {
//        this.args = new HashMap<>();
//        this.command = command;
//        // TODO(stfinancial): How will this behave concurrently? Seems currently like they will be rejected as the timestamps may be sent out of order.
//        this.nonce = System.currentTimeMillis(); // Setting it this way ensures monotonicity.
//    }
//
//    static RequestArgs unsupported() {
//        return UNSUPPORTED;
//    }
//
//    boolean isUnsupported() {
//        // TODO(stfinancial): This is a bit confusing. So a RequestArgs is only unsupported if it was created from the unsupported() method?
//        // What about if the command is just straight up unsupported. Will that be confusing to the client? Or is returning an invlaid command json result fine?
//        return this == UNSUPPORTED;
//    }
//
//    boolean addParam(String param, String value) {
//        // TODO(stfinancial): How should we handle values that are already in the argument list? Currently we return false.
//        if (!args.containsKey(param)) {
//            args.put(param, value);
//            return true;
//        }
//        return false;
//    }
//
//    // TODO(stfinancial): YAGNI: The queue may want this method for some reason.
//    long refreshNonce() {
//        nonce = System.currentTimeMillis();
//        return nonce;
//    }
//
//    String getCommand() { return command; }
//    long getNonce() {
//        return nonce;
//    }
//
//    // TODO(stfinancial): Consider caching both the NVP list and the string.
//    List<NameValuePair> asNameValuePairs() {
//        List<NameValuePair> pairs = new LinkedList<>(); // TODO(stfinancial): Or array list?
//        pairs.add(new BasicNameValuePair("command", command));
//        pairs.add(new BasicNameValuePair("nonce", String.valueOf(nonce)));
//        for (Map.Entry<String, String> arg : args.entrySet()) {
//            pairs.add(new BasicNameValuePair(arg.getKey(), arg.getValue()));
//        }
//        return pairs;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder("command=").append(command);
//        if (!Poloniex.isPublicMethod(command)) {
//            sb.append("&nonce=").append(nonce);
//        }
//        for (Map.Entry<String, String> arg : args.entrySet()) {
//            sb.append("&").append(arg.getKey()).append("=").append(arg.getValue());
//        }
//        return sb.toString();
//    }
//}
