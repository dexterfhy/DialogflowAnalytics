package sns.nv.beans;

import lombok.Getter;
import org.jgrapht.graph.DefaultEdge;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static sns.nv.beans.IntentBean.DEFAULT_INTENT_PARA_DELIMITER;
import static sns.nv.beans.IntentBean.excludeContext;

public class IntentEdge extends DefaultEdge {

    @Getter
    private String name = "";

    private IntentEdge() {}

    @Override
    public String toString() {
        return "";
//        return formatEdgeName((IntentVertex) this.getSource());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof IntentVertex) && (name.equals(o.toString()));
    }

    private static String formatEdgeName(IntentVertex intentVertex){
        return intentVertex.getIntent().getResponses().stream()
            .findFirst()
            .map(response -> response.getAffectedContexts().stream()
                .filter(context -> !excludeContext(context))
                .filter(context -> context.getLifespan() == 5)
                .sorted(comparing(IntentBean.AffectedContext::getName))
                .map(affectedContext -> affectedContext.getContextIdentifier())
                .collect(joining(DEFAULT_INTENT_PARA_DELIMITER))
            ).orElse("-");
    }

}
