package sns.nv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import sns.nv.beans.IntentBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class IntentParser {

    private static final String JSON_EXTENSION = "json";
    private static final Pattern IGNORED_JSON_SAYS_REGEX = Pattern.compile("^.+usersays_[a-z]+.json");
    private static final Pattern IGNORED_JSON_FALLBACK_REGEX = Pattern.compile("^.+Fallback.+.json");

    public static List<IntentBean> parseIntents(String baseFilePath, boolean includeFallback) {
        return getIntentFilePaths(baseFilePath, includeFallback).stream()
            .map(IntentParser::parseIntentFile)
            .filter(Optional::isPresent).map(Optional::get)
            .collect(toList());
    }

    private static Set<String> getIntentFilePaths(String baseFilePath, boolean includeFallback) {
        try {
            return Files.walk(Paths.get(baseFilePath))
                .filter(Files::isRegularFile)
                .filter(path -> FilenameUtils.getExtension(path.toString()).equals(JSON_EXTENSION))
                .filter(path -> !IGNORED_JSON_SAYS_REGEX.matcher(path.toString()).matches() && (includeFallback || !IGNORED_JSON_FALLBACK_REGEX.matcher(path.toString()).matches()))
                .map(Path::toString)
                .collect(toSet());
        } catch (IOException e) {
            System.out.printf("Error reading files from directory '%s'. Expected format: e.g. /Users/dexter.fong/sns/app/dialogflowagent/intents\n", baseFilePath);
            return emptySet();
        }
    }

    private static Optional<IntentBean> parseIntentFile (String intentFilePath) {
        try {
            String intentFileContents = new String(Files.readAllBytes(Paths.get(intentFilePath)), StandardCharsets.UTF_8).replace("{}", "null");
            return Optional.ofNullable(new ObjectMapper().readValue(intentFileContents, IntentBean.class));
        } catch (IOException e){
            System.out.printf("Error parsing file '%s' as intent\n", intentFilePath);
            return Optional.empty();
        }
    }

}
