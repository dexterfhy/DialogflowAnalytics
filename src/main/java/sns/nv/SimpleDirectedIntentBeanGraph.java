package sns.nv;

import lombok.Getter;
import org.jgrapht.graph.SimpleDirectedGraph;
import sns.nv.beans.IntentBean;
import sns.nv.beans.IntentEdge;
import sns.nv.beans.IntentVertex;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static sns.nv.beans.IntentBean.excludeContext;

public final class SimpleDirectedIntentBeanGraph implements Grapher {

    @Getter
    private SimpleDirectedGraph<IntentVertex, IntentEdge> graph;

    private SimpleDirectedIntentBeanGraph() {}

    public SimpleDirectedIntentBeanGraph(List<IntentBean> intentBeans){
        graph = new SimpleDirectedGraph(IntentEdge.class);

        addVertices(intentBeans);
        addEdges(intentBeans);
    }

    private void addEdges(List<IntentBean> intentBeans) {
        intentBeans.forEach(intentBean -> getConnectedTargetIntents(intentBean, intentBeans)
            .forEach(connectedIntent -> this.graph.addEdge(new IntentVertex(intentBean), new IntentVertex(connectedIntent))));
    }

    private static List<IntentBean> getConnectedTargetIntents(IntentBean sourceIntent, List<IntentBean> intents) {
        return intents.stream()
            .filter(targetIntent -> !sourceIntent.equals(targetIntent))
            .filter(targetIntent -> intentMatch(sourceIntent, targetIntent))
            .collect(toList());
    }

    private static boolean intentMatch(IntentBean source, IntentBean target){
        if (target.getContexts().isEmpty()) return false;

        return target.getContexts().stream()
            .allMatch(context -> source
                .getResponses()
                .stream().findFirst()
                .map(IntentBean.Response::getAffectedContexts)
                .map(affectedContexts -> affectedContexts.stream()
                    .filter(affectedContext -> !excludeContext(affectedContext))
                    .map(IntentBean.AffectedContext::getName).collect(toList())
                    .contains(context))
                .orElse(false));
    }

    private void addVertices(List<IntentBean> intentBeans) {
        intentBeans.stream().forEach(intentBean -> this.graph.addVertex(new IntentVertex(intentBean)));
    }

}
