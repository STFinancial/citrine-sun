package api.gdax;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.*;

/**
 * Created by Timothy on 4/23/17.
 */
final class RestArgs {
    // TODO(stfinancial): Review the thread safety of this object.
    private static final ObjectMapper mapper = new ObjectMapper();

    enum HttpRequestType {
        // TODO(stfinancial): Potentially move this into its own class?
        POST, GET, DELETE;
    }

    // TODO(stfinancial): Potentially take in an isPublic, isPrivate to determine which url to use.
    // TODO(stfinancial): Potentially merge this class with RequestArgs from poloniex...

    // TODO(stfinancial): Improve this... maybe allow a message or something. Throw an exception... idk.
    private static final RestArgs UNSUPPORTED = new RestArgs((new Builder()).withResource("<<< *UNSUPPORTED* >>>"));

    private static final String PUBLIC_URI = "https://api.gdax.com";
    private static final String SLASH = "/";
    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";
    private static final String AND = "&";

    private String resourcePath;
    private String url;
    private List<String> resources;
    private Map<String, String> params;
    private final boolean isPublic;
    private final HttpRequestType type;

    private RestArgs(Builder builder) {
        this.resources = builder.resources;
        this.params = builder.params;
        this.isPublic = builder.isPublic;
        this.type = builder.type;
        StringBuilder sb = new StringBuilder();
        resources.forEach(resource -> sb.append(SLASH).append(resource));
        resourcePath = sb.toString();

        // TODO(stfinancial): public and private URI stuff.
    }

    static RestArgs unsupported() {
        return UNSUPPORTED;
    }

    boolean isUnsupported() {
        // TODO(stfinanical): See if we can give an unsupported reason.
        // TODO(stfinancial): This is a bit confusing. So a RequestArgs is only unsupported if it was created from the unsupported() method?
        // What about if the command is just straight up unsupported. Will that be confusing to the client? Or is returning an invlaid command json result fine?
        return this == UNSUPPORTED;
    }

    String getUrl() {
        if (url != null && !url.isEmpty()) {
            return url;
        }
        StringBuilder sb;
        if (isPublic) {
            sb = new StringBuilder(PUBLIC_URI);
        } else {
            // TODO(stfinancial): These are currently the same... clean this up.
            sb = new StringBuilder(PUBLIC_URI);
        }
        sb.append(resourcePath);
        // TODO(stfinancial): This code is pretty ugly. Look this over.
        if (isPublic) {
            sb.append(QUESTION_MARK);
            boolean first = true;
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (!first) {
                    sb.append(AND);
                } else {
                    first = false;
                }
                sb.append(param.getKey()).append(EQUALS).append(param.getValue());
            }
        }
        url = sb.toString();
        return url;
    }

    JsonNode asJson() {
        // TODO(stfinancial): Do we want to just throw the IO Exception from this function?
        // TODO(stfinancial): Implement this. Object map arguments.
        // TODO(stfinancial): Add support for list arguments.
        if (params.isEmpty()) {
            // TODO(stfinancial): Figure out the best way to handle this.
            return NullNode.getInstance();
//            try {
//                return mapper.readTree("");
//            } catch (IOException e) {
//
//            }
        }
        StringBuilder output = new StringBuilder("{\n");
        params.forEach((key, value) -> {
            // TODO(stfinancial): Awful hack for now, refactor to a pair that allows modifiers such as "no quotes in json"  for example
            if (value.equals("false") || value.equals("true")) {
                output.append("\"").append(key).append("\"").append(":").append(value).append(",\n");
            } else {
                // TODO(stfinancial): Test that we actually need these extra quotes here.
                output.append("\"").append(key).append("\"").append(":").append("\"").append(value).append("\",\n");
            }


        });
        // TODO(stfinancial): This is kind of gross. Fix this later.
        output.deleteCharAt(output.length() - 2);
        output.append("}");
//        System.out.println("JSON: " + output);
        try {
            return mapper.readTree(output.toString());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Unable to parse RestArgs as JSON: " + output.toString());
            return NullNode.getInstance();
        }
    }

    List<NameValuePair> asNameValuePairs() {
        List<NameValuePair> pairs = new ArrayList<>();
        params.forEach((name, value) -> pairs.add(new BasicNameValuePair(name, value)));
        // TODO(stfinancial): See if we need nonce.
        return pairs;
    }

    String getResourcePath() {
        return resourcePath;
    }
    Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }
    boolean isPublic() { return isPublic; }
    HttpRequestType getHttpRequestType() { return type; }

    static class Builder {
        private List<String> resources;
        private Map<String, String> params;
        private boolean isPublic;
        private HttpRequestType type;

        Builder() {
            resources = new ArrayList<>();
            params = new HashMap<>();
            // These two settings are the most innocuous.
            isPublic = false;
            type = HttpRequestType.GET;
        }

        /**
         * Appends {@code /resource} to the end of the URL.
         * @param resource The resource to append.
         * @return An updated builder.
         */
        Builder withResource(String resource) {
            resources.add(resource);
            return this;
        }

        Builder withParam(String name, String value) {
            params.put(name, value);
            return this;
        }

        Builder isPublic(boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }

        Builder httpRequestType(HttpRequestType type) {
            this.type = type;
            return this;
        }

        RestArgs build() {
            return new RestArgs(this);
        }
    }
}
