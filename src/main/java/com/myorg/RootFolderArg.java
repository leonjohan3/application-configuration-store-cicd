package com.myorg;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RootFolderArg {

    static final String ROOT_FOLDER_OPTION = "r";
    static final String ROOT_FOLDER_LONG_OPTION = "root-folder";
    private static final DefaultParser DEFAULT_PARSER = new DefaultParser();
    private static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption(Option.builder(ROOT_FOLDER_OPTION)
            .argName("root_folder")
            .longOpt(ROOT_FOLDER_LONG_OPTION)
            .hasArg()
            .desc("The root folder for the application configurations, e.g. ../application-configuration-store")
            .required()
            .type(String.class)
            .build());
    }

    private final String[] args;

    public RootFolderArg(final String[] args) {
        this.args = new String[args.length];
        System.arraycopy(args, 0, this.args, 0, args.length);
    }

    public String getRootFolder() {
        var rootFolder = "";

        try {
            rootFolder = parseArgs();

            if (!rootFolder.isBlank() && !Files.isDirectory(Paths.get(rootFolder))) {
                rootFolder = "";
                System.out.println("Path is not a directory, or does not exists: " + rootFolder);
                displayHelp();
            }
        } catch (ParseException e) {
            System.out.println("Error parsing arguments, " + e.getMessage());
            displayHelp();
        }
        return rootFolder;
    }

    String parseArgs() throws ParseException {
        final var cmd = DEFAULT_PARSER.parse(OPTIONS, args);
        return cmd.getOptionValue(ROOT_FOLDER_OPTION).trim();
    }

    private void displayHelp() {
        final var formatter = new HelpFormatter();
        formatter.setWidth(130);
        formatter.printHelp("ant????TODO", OPTIONS);
    }
}
