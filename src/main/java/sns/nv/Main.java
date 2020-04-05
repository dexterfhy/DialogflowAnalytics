package sns.nv;

import org.apache.commons.cli.*;
import sns.nv.beans.IntentBean;
import sns.nv.enums.CmdArg;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static sns.nv.IntentOutputFormatter.generateBreakdownFiles;
import static sns.nv.IntentOutputFormatter.generateGraphImage;
import static sns.nv.IntentParser.parseIntents;

public class Main {

    private static final String DEFAULT_TARGET_FOLDER_NAME = "/source";

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Please specify a FULL folder directory for intents e.g. /Users/dexter.fong/sns/app/dialogflowagent/intents");
            //return
        }

        //CommandLine cmd = parseCommandLineArgs(args);

        String baseFilePath = "/Users/dexter.fong/sns/app/dialogflowagent/intents"; //cmd.getOptionValue(CmdArg.DIRECTORY.getLongOpt());
        boolean includeFallback = true; //cmd.hasOption(CmdArg.INCLUDE_FALLBACK.getLongOpt());

        File dir = new File(baseFilePath.concat(DEFAULT_TARGET_FOLDER_NAME));
        if (!dir.exists()) dir.mkdirs();

        final String sourceFolderPath = baseFilePath;
        final String targetFolderPath = dir.getAbsolutePath();

        List<IntentBean> intentBeans = parseIntents(sourceFolderPath, includeFallback);
        SimpleDirectedIntentBeanGraph graph = new SimpleDirectedIntentBeanGraph(intentBeans);
        IntentGraphAdapter adapter = new IntentGraphAdapter(graph);

        adapter.display();
        generateBreakdownFiles(targetFolderPath, intentBeans, graph.getGraph());
        generateGraphImage(targetFolderPath, adapter);

    }

    private static CommandLine parseCommandLineArgs(String[] args) {
        final Options options = new Options();

        Arrays.stream(CmdArg.values())
            .forEach(cmdArg -> {
                Option option = new Option(cmdArg.getOpt(), cmdArg.getLongOpt(), cmdArg.isHasArg(), cmdArg.getDescription());
                option.setRequired(cmdArg.isRequired());
                options.addOption(option);
            });

        final CommandLineParser cliParser = new DefaultParser();
        final HelpFormatter cliFormatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = cliParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            cliFormatter.printHelp("intent", options);
            System.exit(1);
        }

        return cmd;
    }

}
