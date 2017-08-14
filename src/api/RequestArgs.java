package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic class containing information about constructing an {@link org.apache.http.HttpRequest}. Contains URL, http
 * request type, and request body as a {@link JsonNode} or {@link NameValuePair}.
 */
public final class RequestArgs {
    // TODO(stfinancial): Potentially move this into its own class.
    public enum HttpRequestType {
        GET, POST, DELETE;
    }

    private static final String SLASH = "/";
    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";
    private static final String AND = "&";
    private static final String LEFT_BRACE_NEWLINE = "{\n";
    private static final String RIGHT_BRACE = "}";
    private static final String COLON = ":";
    private static final String COMMA_NEWLINE = ",\n";
    private static final String QUOTE = "\"";

    // TODO(stfinancial): Find a way to handle a nonce in a multithreaded environment.

    private final String uri;
    private final HttpRequestType type;
    private final boolean isPrivate;
    private List<String> resources;
    private List<RequestParam> params;

    private final String resourcePath;
    private final String queryString;
    private JsonNode json;
    private List<NameValuePair> nameValuePairs;

    // TODO(stfinancial): Improve this... maybe allow a message or something (maybe pair a message with this class). Throw an exception... idk.
    private static final RequestArgs UNSUPPORTED = new RequestArgs((new Builder("<<< *UNSUPPORTED* >>>")));

    private RequestArgs(Builder builder) {
        this.uri = builder.uri;
        this.type = builder.type;
        this.isPrivate = builder.isPrivate;
        this.resources = builder.resources;
        this.params = builder.params;

        StringBuilder sb = new StringBuilder();
        for (String resource : resources) { sb.append(SLASH).append(resource); }
        resourcePath = sb.toString();

        sb = new StringBuilder();
        boolean first = true;
        for (RequestParam param : params) {
            if (!param.isQueryParam) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                sb.append(AND);
            }
            sb.append(param.name).append(EQUALS).append(param.value);
        }
        queryString = sb.toString();
    }

    public static RequestArgs unsupported() {
        return UNSUPPORTED;
    }

    public boolean isUnsupported() {
        // TODO(stfinanical): See if we can give an unsupported reason.
        // TODO(stfinancial): This is a bit confusing. So a RequestArgs is only unsupported if it was created from the unsupported() method?
        // What about if the command is just straight up unsupported. Will that be confusing to the client? Or is returning an invlaid command json result fine?
        return this == UNSUPPORTED;
    }

    public HttpRequestType getHttpRequestType() {
        return type;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public List<NameValuePair> asNameValuePairs() {
        if (nameValuePairs != null) {
            return Collections.unmodifiableList(nameValuePairs);
        }
        List<NameValuePair> pairs = new ArrayList<>();
        params.forEach((param)->pairs.add(new BasicNameValuePair(param.name, param.value)));
//        // TODO(stfinancial): Revert this comma splitting.
//        params.forEach((param)-> {
//            if (param.value.contains(",")) {
//                StringBuilder sb = new StringBuilder("[");
//                for (String v : param.value.split(",")) {
//                    sb.append("\"").append(v).append("\"");
//                }
////                pairs.add(new BasicNameValuePair(param.name, "[" + param.value + "]"));
//                sb.append("]");
//                pairs.add(new BasicNameValuePair(param.name, sb.toString()));
////                for (String v : param.value.split(",")) {
////                    pairs.add(new BasicNameValuePair(param.name, v));
////                }
//            } else {
//                pairs.add(new BasicNameValuePair(param.name, param.value));
//            }
//        });
        nameValuePairs = pairs;
        return Collections.unmodifiableList(nameValuePairs);
    }

    public String getResourcePath() { return resourcePath; }

    public String getQueryString() { return queryString; }

    public String getUri() { return uri; }

    public String asUrl(boolean withQueryParams) {
        if (withQueryParams) {
            if (queryString != null && !queryString.isEmpty()) {
                return uri + resourcePath + QUESTION_MARK + queryString;
            } else {
                return uri + resourcePath;
            }
        } else {
            return uri + resourcePath;
        }
    }

    public JsonNode asJson(ObjectMapper mapper) {
        if (json != null && !json.isNull()) {
            return json;
        }
        // TODO(stfinancial): Add support for list arguments.
        if (params.isEmpty()) {
            // TODO(stfinancial): Figure out the best way to handle this. Read empty json?
            return NullNode.getInstance();
        }
        StringBuilder output = new StringBuilder(LEFT_BRACE_NEWLINE);
        params.forEach((param) -> {
            if (param.valueWithQuotes) {
                output.append(QUOTE).append(param.name).append(QUOTE).append(COLON).append(QUOTE).append(param.value).append(QUOTE).append(COMMA_NEWLINE);
            } else {
                output.append(QUOTE).append(param.name).append(QUOTE).append(COLON).append(param.value).append(COMMA_NEWLINE);
            }
        });
        // TODO(stfinancial): This is kind of gross. Fix this later.
        output.deleteCharAt(output.length() - 2);
        output.append(RIGHT_BRACE);
        try {
            json = mapper.readTree(output.toString());
            return json;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Unable to parse RestArgs as JSON: " + output.toString());
            return NullNode.getInstance();
        }
    }

    public static class Builder {
        private final String uri;
        // TODO(stfinancial): Replace this with a string builder?
        private List<String> resources;
        private List<RequestParam> params;
        // TODO(stfinancial): Make type and isPrivate part of the constructor?
        private HttpRequestType type = HttpRequestType.GET;
        private boolean isPrivate = false;

        public Builder(String uri) {
            this.uri = uri;
            resources = new ArrayList<>();
            params = new ArrayList<>();
        }

        /**
         * Appends {@code /resource} to the end of the URL.
         * @param resource The resource to append.
         * @return An updated builder.
         */
        public Builder withResource(String resource) {
            resources.add(resource);
            return this;
        }

        /**
         * Adds a piece of http request body data.
         * @param name Data field name.
         * @param value Data field value.
         * @return An updated Builder instance.
         */
        public Builder withParam(String name, String value) {
            RequestParam param = new RequestParam();
            param.name = name;
            param.value = value;
            param.isQueryParam = true;
            param.valueWithQuotes = true;
            params.add(param);
            return this;
        }

        // TODO(stfinancial): Is this one needed?
        /**
         * Adds a piece of http request body data.
         * @param name Data field name.
         * @param value Data field value.
         * @param valueWithQuotes Whether the value parameter should be in quotes in the json of the request body. (e.g. {"isMargin":"1"} vs. {"isMargin":true}
         * @return An updated Builder instance.
         */
        public Builder withParam(String name, String value, boolean valueWithQuotes) {
            RequestParam param = new RequestParam();
            param.name = name;
            param.value = value;
            param.isQueryParam = true;
            param.valueWithQuotes = valueWithQuotes;
            params.add(param);
            return this;
        }

        // TODO(stfinancial): IsQueryParam is super confusing imo.
        /**
         * Adds a piece of http request body data.
         * @param name Data field name.
         * @param value Data field value.
         * @param valueWithQuotes Whether the value parameter should be in quotes in the json of the request body. (e.g. {"isMargin":"1"} vs. {"isMargin":true}
         * @param isQueryParam Whether this parameter should be added as part of the query parameters (e.g. command=returnTicker in http://api.poloniex.com/public?command=returnTicker)
         * @return An updated Builder instance.
         */
        public Builder withParam(String name, String value, boolean valueWithQuotes, boolean isQueryParam) {
            RequestParam param = new RequestParam();
            param.name = name;
            param.value = value;
            param.isQueryParam = isQueryParam;
            param.valueWithQuotes = valueWithQuotes;
            params.add(param);
            return this;
        }

        public Builder httpRequestType(HttpRequestType type) {
            this.type = type;
            return this;
        }

        public Builder isPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
            return this;
        }

        public RequestArgs build() {
            return new RequestArgs(this);
        }
    }

    private static class RequestParam {
        String name;
        String value;
        boolean isQueryParam;
        boolean valueWithQuotes;
    }
}
