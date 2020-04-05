package sns.nv.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class IntentBean {

    public static final String DEFAULT_INTENT_PARA_DELIMITER = "/";
    public static final Set<String> IGNORED_CONTEXTS = emptySet();

    public static boolean excludeContext(AffectedContext affectedContext) {
        return IGNORED_CONTEXTS.contains(affectedContext.getName()) || affectedContext.getLifespan() == 0;
    }

    @JsonProperty()
    private String id;
    @JsonProperty()
    private String parentId;
    @JsonProperty()
    private String rootParentId;
    @JsonProperty()
    private String name;
    @JsonProperty()
    private boolean auto;
    @JsonProperty()
    private List<String> contexts;
    @JsonProperty()
    private List<Response> responses;
    @JsonProperty()
    private long priority;
    @JsonProperty()
    private boolean webhookUsed;
    @JsonProperty()
    private boolean webhookForSlotFilling;
    @JsonProperty()
    private boolean fallbackIntent;
    @JsonProperty()
    private List<String> events;
    @JsonProperty()
    private List<String> conditionalResponses;
    @JsonProperty()
    private String condition;
    @JsonProperty()
    private List<String> conditionalFollowupEvents;

    @Data
    @Builder
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        @JsonProperty()
        private boolean resetContexts;
        @JsonProperty()
        private String action;
        @JsonProperty()
        private List<AffectedContext> affectedContexts;
        @JsonProperty()
        private List<Parameter> parameters;
        @JsonProperty()
        private List<Message> messages;
        @JsonProperty()
        private DefaultResponsePlatforms defaultResponsePlatforms;
        @JsonProperty()
        private List<String> speech;
    }

    @Data
    @Builder
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AffectedContext {
        @JsonProperty()
        private String name;
        @JsonProperty()
        private List<Parameter> parameters;
        @JsonProperty()
        private int lifespan;

        public String getContextIdentifier() {
            return String.format("%s (%d)", this.name, this.lifespan);
        }
    }

    @Data
    @Builder
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        @JsonProperty()
        private String id;
        @JsonProperty()
        private boolean required;
        @JsonProperty()
        private String dataType;
        @JsonProperty()
        private String name;
        @JsonProperty()
        private String value;
        @JsonProperty()
        private List<Prompt> prompts;
        @JsonProperty()
        private List<String> promptMessages;
        @JsonProperty()
        private List<String> noMatchPromptMessages;
        @JsonProperty()
        private List<String> noInputPromptMessages;
        @JsonProperty()
        private List<AffectedContext> outputDialogContexts;
        @JsonProperty()
        private boolean isList;
    }

    @Data
    @Builder
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prompt {
        @JsonProperty()
        private String lang;
        @JsonProperty()
        private String value;
    }

    @Data
    @Builder
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        @JsonProperty()
        private int type;
        @JsonProperty()
        private String lang;
        @JsonProperty()
        private String condition;
        @JsonProperty()
        private JsonNode speech;
    }

    @Data
    @Builder
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class DefaultResponsePlatforms {

    }

}
