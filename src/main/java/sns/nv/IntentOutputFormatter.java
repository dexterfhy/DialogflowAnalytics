package sns.nv;

import com.mxgraph.util.mxCellRenderer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jgrapht.graph.SimpleDirectedGraph;
import sns.nv.beans.IntentBean;
import sns.nv.beans.IntentVertex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static sns.nv.beans.IntentBean.DEFAULT_INTENT_PARA_DELIMITER;
import static sns.nv.beans.IntentBean.excludeContext;

public class IntentOutputFormatter {

    private static final List<String> headers = emptyList();

    private static final String DEFAULT_GENERAL_FILE_NAME = "/general-stats(%s).txt";
    private static final String DEFAULT_INTENT_FILE_NAME = "/intent-breakdown(%s).csv";
    private static final String DEFAULT_ACTION_FILE_NAME = "/action-breakdown(%s).csv";
    private static final String DEFAULT_GRAPH_FILE_NAME = "/intent-graph(%s).png";

    private IntentOutputFormatter() {}

    public static void generateBreakdownFiles(String baseFilePath, List<IntentBean> intentBeans, SimpleDirectedGraph graph) {
        try {
            String generalFilePath = baseFilePath.concat(getGeneralFileName());
            String intentFilePath = baseFilePath.concat(getIntentFileName());
            String actionFilePath = baseFilePath.concat(getActionFileName());

            BufferedWriter generalFileWriter = Files.newBufferedWriter(Paths.get(generalFilePath));
            BufferedWriter intentFileWriter = Files.newBufferedWriter(Paths.get(intentFilePath));
            BufferedWriter actionFileWriter = Files.newBufferedWriter(Paths.get(actionFilePath));

            CSVPrinter intentCsvPrinter = new CSVPrinter(intentFileWriter, CSVFormat.DEFAULT.withHeader(IntentFileHeader.class));
            CSVPrinter actionCsvPrinter = new CSVPrinter(actionFileWriter, CSVFormat.DEFAULT.withHeader(IntentFileHeader.class));

            System.out.println();
            generateGeneralFileContents(intentBeans).stream()
                .forEach(line -> {
                    try {
                        generalFileWriter.write(line.concat("\n"));
                        System.out.println(line);
                    } catch (IOException e) {
                        System.out.printf("Error adding line for general file: %s\n%s\n", line, e.getMessage());
                    }
                });
            System.out.println();
            generalFileWriter.flush();
            System.out.printf("General stats file successfully saved to %s\n", generalFilePath);

            generateIntentCsvRecords(intentBeans, graph).stream()
                .forEach(recordList -> {
                    try {
                        intentCsvPrinter.printRecord(recordList);
                    } catch (IOException e) {
                        System.out.printf("Error adding record for intent file: %s\n%s\n", recordList, e.getMessage());
                    }
                });
            intentCsvPrinter.flush();
            System.out.printf("Intent file successfully saved to %s\n", intentFilePath);

            generateActionCsvRecords(intentBeans).stream()
                .forEach(recordList -> {
                    try {
                        actionCsvPrinter.printRecord(recordList);
                    } catch (IOException e) {
                        System.out.printf("Error adding record for action file: %s\n%s\n", recordList, e.getMessage());
                    }
                });
            actionCsvPrinter.flush();
            System.out.printf("Action file successfully saved to %s\n", actionFilePath);
        } catch (IOException e) {
            System.out.printf("Error generating breakdown file: %s\n", e.getMessage());
        }
    }

    private static List<String> generateGeneralFileContents(List<IntentBean> intentBeans) {
        List<String> generalFileContents = new ArrayList<>();

        generalFileContents.add(String.format("\n---INTENTS (Total: %d)---",intentBeans.size()));
        generalFileContents.addAll(intentBeans.stream().map(IntentBean::getName).distinct().sorted().collect(toList()));

        List<String> uniqueContexts = getUniqueContexts(intentBeans);
        generalFileContents.add(String.format("\n---CONTEXTS (Total: %d)---", uniqueContexts.size()));
        generalFileContents.addAll(uniqueContexts);

        List<String> uniqueActions = getUniqueActions(intentBeans);
        generalFileContents.add(String.format("\n---ACTIONS (Total: %d)---", uniqueActions.size()));
        generalFileContents.addAll(uniqueActions);

        generalFileContents.add("\n\n------------------------");

        List<String> intentsWithoutActions = getIntentsWithoutActions(intentBeans);
        generalFileContents.add(String.format("\nIntents without Actions: %d", intentsWithoutActions.size()));
        generalFileContents.addAll(intentsWithoutActions);

        List<String> intentsWithoutInputContexts = getIntentsWithoutInputContexts(intentBeans);
        generalFileContents.add(String.format("\nIntents without Input Contexts: %d", intentsWithoutInputContexts.size()));
        generalFileContents.addAll(intentsWithoutInputContexts);

        List<String> intentsWithoutOutputContexts = getIntentsWithoutOutputContexts(intentBeans);
        generalFileContents.add(String.format("\nIntents without Output Contexts: %d", intentsWithoutOutputContexts.size()));
        generalFileContents.addAll(intentsWithoutOutputContexts);

        return generalFileContents;
    }

    public static void generateGraphImage(String targetPath, IntentGraphAdapter adapter) {
        try {
            String targetFile = targetPath.concat(getGraphFileName());
            BufferedImage image =
                mxCellRenderer.createBufferedImage(adapter.getJgxAdapter(), null, 2, Color.WHITE, true, null);
            File imgFile = new File(targetFile);
            ImageIO.write(image, "PNG", imgFile);
            System.out.printf("Graph image file successfully saved to %s\n", targetFile);
        } catch (IOException e) {
            System.out.printf("Unable to save graph as image: %s\n", e.getMessage());
        }
    }

    private static List<String> getUniqueContexts(List<IntentBean> intentBeans){
        return intentBeans.stream()
            .map(intentBean -> intentBean.getResponses().stream().findFirst()
                .map(IntentBean.Response::getAffectedContexts)
                .map(affectedContexts -> affectedContexts.stream().map(IntentBean.AffectedContext::getName).collect(toList()))
                .orElse(emptyList()))
            .flatMap(List::stream)
            .distinct().sorted()
            .collect(toList());
    }

    private static List<String> getUniqueActions(List<IntentBean> intentBeans){
        return intentBeans.stream()
            .map(intentBean -> intentBean.getResponses().stream().findFirst()
                .map(IntentBean.Response::getAction))
            .filter(Optional::isPresent).map(Optional::get)
            .distinct().sorted()
            .collect(toList());
    }

    private static List<String> getIntentsWithoutActions(List<IntentBean> intentBeans) {
        return intentBeans.stream()
            .filter(intentBean -> intentBean.getResponses().stream().findFirst()
                .map(response -> response.getAction() == null || response.getAction().isEmpty())
                .orElse(false))
            .map(IntentBean::getName)
            .distinct().sorted()
            .collect(toList());
    }

    private static List<String> getIntentsWithoutInputContexts(List<IntentBean> intentBeans) {
        return intentBeans.stream()
            .filter(intentBean -> intentBean.getContexts() == null || intentBean.getContexts().isEmpty())
            .map(IntentBean::getName)
            .distinct().sorted()
            .collect(toList());
    }

    private static List<String> getIntentsWithoutOutputContexts(List<IntentBean> intentBeans) {
        return intentBeans.stream()
            .filter(intentBean -> intentBean.getResponses().stream().findFirst()
                .map(response -> response.getAffectedContexts() == null || response.getAffectedContexts().isEmpty())
                .orElse(false))
            .map(IntentBean::getName)
            .distinct().sorted()
            .collect(toList());
    }

    private static List<List<String>> generateIntentCsvRecords(List<IntentBean> intentBeans, SimpleDirectedGraph graph){
        return intentBeans.stream()
            .sorted(comparing(IntentBean::getName))
            .map(intentBean -> Arrays.asList(
                    intentBean.getName(),
                    getInputContextsForIntent(intentBean),
                    getConnectedInputIntents(intentBean, graph),
                    getOutputContextsForIntent(intentBean),
                    getConnectedOutputIntents(intentBean, graph),
                    getActionForIntent(intentBean),
                    getParametersForIntent(intentBean)
                )
            ).limit(5).collect(toList());
    }

    private static String getInputContextsForIntent(IntentBean intentBean){
        return intentBean.getContexts().stream()
            .sorted()
            .collect(joining(DEFAULT_INTENT_PARA_DELIMITER));
    }

    private static String getOutputContextsForIntent(IntentBean intentBean){
        return intentBean.getResponses().stream().findFirst()
            .map(IntentBean.Response::getAffectedContexts)
            .map(affectedContexts -> affectedContexts.stream()
                .filter(affectedContext -> !excludeContext(affectedContext))
                .map(affectedContext -> String.format("%s (%d)", affectedContext.getName(), affectedContext.getLifespan()))
                .sorted()
                .collect(joining(DEFAULT_INTENT_PARA_DELIMITER)))
            .orElse("");
    }

    private static String getConnectedInputIntents(IntentBean intentBean, SimpleDirectedGraph graph) {
        return getInputVertices(intentBean, graph)
            .map(IntentVertex::getName)
            .distinct()
            .sorted()
            .collect(joining(DEFAULT_INTENT_PARA_DELIMITER));
    }

    private static Stream<IntentVertex> getInputVertices(IntentBean intentBean, SimpleDirectedGraph graph) {
        return graph.incomingEdgesOf(graph.vertexSet().stream().filter(vertex -> ((IntentVertex) vertex).getName().equals(intentBean.getName())).findFirst().orElse(new IntentVertex(intentBean)))
            .stream()
            .map(incomingEdge -> ((IntentVertex) graph.getEdgeSource(incomingEdge)))
            .sorted();
    }

    private static String getConnectedOutputIntents(IntentBean intentBean, SimpleDirectedGraph graph) {
        return getOutputVertices(intentBean, graph)
            .map(IntentVertex::getName)
            .distinct()
            .sorted()
            .collect(joining(DEFAULT_INTENT_PARA_DELIMITER));
    }

    private static Stream<IntentVertex> getOutputVertices(IntentBean intentBean, SimpleDirectedGraph graph) {
        return graph.outgoingEdgesOf(graph.vertexSet().stream().filter(vertex -> ((IntentVertex) vertex).getName().equals(intentBean.getName())).findFirst().orElse(new IntentVertex(intentBean)))
            .stream()
            .map(outgoingEdge -> ((IntentVertex) graph.getEdgeSource(outgoingEdge)))
            .sorted();
    }

    private static String getActionForIntent(IntentBean intentBean){
        return intentBean.getResponses().stream().findFirst()
            .map(IntentBean.Response::getAction)
            .orElse("");
    }

    private static String getParametersForIntent(IntentBean intentBean){
        return intentBean.getResponses().stream().findFirst()
            .map(IntentBean.Response::getParameters)
            .map(parameters -> parameters.stream()
                .map(IntentBean.Parameter::getName)
                .collect(joining(DEFAULT_INTENT_PARA_DELIMITER)))
            .orElse("");
    }

    private enum IntentFileHeader {
        NAME,
        INPUT_CONTEXTS,
        CONNECTED_INPUT_INTENTS,
        OUTPUT_CONTEXTS,
        CONNECTED_OUTPUT_INTENTS,
        ACTION,
        PARAMETERS
    }

    private static List<List<String>> generateActionCsvRecords(List<IntentBean> intentBeans){
        return intentBeans.stream()
            .map(intentBean -> intentBean.getResponses().stream().findFirst()
                .map(IntentBean.Response::getAction)
            ).filter(Optional::isPresent).map(Optional::get)
            .distinct()
            .map(actionName -> Arrays.asList(
                actionName,
                getIntentsCalledByAction(actionName, intentBeans),
                countIntentsCalledByAction(actionName, intentBeans)
            )).collect(toList());
    }

    private static String getIntentsCalledByAction(String actionName, List<IntentBean> intentBeans){
        return intentBeans.stream().filter(intentBean -> intentBean.getResponses().stream()
                .findFirst()
                .map(response -> actionName.equals(response.getAction())).orElse(false)
            ).map(IntentBean::getName)
            .sorted()
            .collect(joining(DEFAULT_INTENT_PARA_DELIMITER));
    }

    private static String countIntentsCalledByAction(String actionName, List<IntentBean> intentBeans) {
        return String.valueOf(intentBeans.stream().filter(intentBean -> intentBean.getResponses().stream()
                .findFirst()
                .map(response -> actionName.equals(response.getAction())).orElse(false))
            .count());
    }

    private enum ActionFileHeader {
        NAME,
        CALLING_INTENTS,
        NUM_OF_CALLING_INTENTS
    }

    private static String getGeneralFileName(){
        return String.format(DEFAULT_GENERAL_FILE_NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    private static String getIntentFileName(){
        return String.format(DEFAULT_INTENT_FILE_NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    private static String getActionFileName(){
        return String.format(DEFAULT_ACTION_FILE_NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    private static String getGraphFileName(){
        return String.format(DEFAULT_GRAPH_FILE_NAME, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

}