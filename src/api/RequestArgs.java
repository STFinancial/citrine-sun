package api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Timothy on 5/4/17.
 */
public final class RequestArgs {
    public enum HttpRequestType {
        GET, POST, DELETE;
    }

    private final String uri;
    private final HttpRequestType type;

    private RequestArgs(Builder builder) {
        this.uri = builder.uri;
        this.type = builder.type;
    }


    public static class Builder {
        private final String uri;
        // TODO(stfinancial): Replace this with a string builder?
        private List<String> resources;
        private HttpRequestType type = HttpRequestType.GET;

        public Builder(String uri) {
            this.uri = uri;
            resources = new ArrayList<>();
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

        public Builder httpRequestType(HttpRequestType type) {
            this.type = type;
            return this;
        }

        public RequestArgs build() {
            return new RequestArgs(this);
        }
    }
}
