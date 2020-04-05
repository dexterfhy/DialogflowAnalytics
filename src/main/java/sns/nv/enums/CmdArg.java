package sns.nv.enums;

import lombok.Getter;

public enum CmdArg {

    DIRECTORY("d", "directory", true, "intents base directory", true),
    INCLUDE_FALLBACK("fb", "includeFallback", false, "display fallback intents in graph", false);

    @Getter
    private String opt;
    @Getter
    private String longOpt;
    @Getter
    private boolean hasArg;
    @Getter
    private String description;
    @Getter
    private boolean required;

    CmdArg(String opt, String longOpt, boolean hasArg, String description, boolean required) {
        this.opt = opt;
        this.longOpt = longOpt;
        this.hasArg = hasArg;
        this.description = description;
        this.required = required;
    }

}
