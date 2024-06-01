package com.myorg;

import static com.myorg.RootFolderArg.ROOT_FOLDER_LONG_OPTION;
import static com.myorg.RootFolderArg.ROOT_FOLDER_OPTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RootFolderArgTests {

    @Test
    void shouldThrowMissingOptionException() {

        // given: all data (test fixture) preparation
        final var rootFolderArg = new RootFolderArg(new String[]{});

        // when : method to be checked invocation
        final var exception = assertThrows(MissingOptionException.class, rootFolderArg::parseArgs);

        // then : checks and assertions
        assertThat(exception.getMessage(), containsString("issing required option: r"));
    }

    @Test
    void shouldThrowUnrecognizedOptionException() {

        // given: all data (test fixture) preparation
        final var rootFolderArg = new RootFolderArg(new String[]{"-d"});

        // when : method to be checked invocation
        final var exception = assertThrows(UnrecognizedOptionException.class, rootFolderArg::parseArgs);

        // then : checks and assertions
        assertThat(exception.getMessage(), containsString("nrecognized option: -d"));
    }

    @Test
    void shouldThrowUnrecognizedOptionExceptionForLongOption() {

        // given: all data (test fixture) preparation
        final var rootFolderArg = new RootFolderArg(new String[]{"--debug"});

        // when : method to be checked invocation
        final var exception = assertThrows(UnrecognizedOptionException.class, rootFolderArg::parseArgs);

        // then : checks and assertions
        assertThat(exception.getMessage(), containsString("nrecognized option: --debug"));
    }

    @Test
    void shouldThrowMissingArgumentException() {

        // given: all data (test fixture) preparation
        final var rootFolderArg = new RootFolderArg(new String[]{"-r"});

        // when : method to be checked invocation
        final var exception = assertThrows(MissingArgumentException.class, rootFolderArg::parseArgs);

        // then : checks and assertions
        assertThat(exception.getMessage(), containsString("issing argument for option: r"));
    }

    @Test
    void shouldThrowMissingArgumentExceptionForLongOption() {

        // given: all data (test fixture) preparation
        final var rootFolderArg = new RootFolderArg(new String[]{"--root-folder"});

        // when : method to be checked invocation
        final var exception = assertThrows(MissingArgumentException.class, rootFolderArg::parseArgs);

        // then : checks and assertions
        assertThat(exception.getMessage(), containsString("issing argument for option: r"));
    }

    @ParameterizedTest
    @MethodSource("getRootFolderArgTestsArguments")
    void shouldGetRootFolder(final String[] args, final String expectedResult) {

        // given: all data (test fixture) preparation
        final var rootFolderArg = new RootFolderArg(args);

        // when : method to be checked invocation
        final var rootFolder = rootFolderArg.getRootFolder();

        // then : checks and assertions
        assertThat(rootFolder, is(expectedResult));
    }

    private static Stream<Arguments> getRootFolderArgTestsArguments() {
        return Stream.of(
            Arguments.of((Object) new String[]{"-" + ROOT_FOLDER_OPTION, "build"}, "build"),
            Arguments.of((Object) new String[]{"--" + ROOT_FOLDER_LONG_OPTION, "build"}, "build"),
            Arguments.of((Object) new String[]{"-" + ROOT_FOLDER_OPTION, "folder-that-does-not-exist"}, ""),
            Arguments.of((Object) new String[]{"--" + ROOT_FOLDER_LONG_OPTION, "folder-that-does-not-exist"}, ""),
            Arguments.of((Object) new String[]{"-" + ROOT_FOLDER_OPTION, ""}, ""),
            Arguments.of((Object) new String[]{"--" + ROOT_FOLDER_LONG_OPTION, ""}, ""),
            Arguments.of((Object) new String[]{"-" + ROOT_FOLDER_OPTION, " "}, ""),
            Arguments.of((Object) new String[]{"--" + ROOT_FOLDER_LONG_OPTION, " "}, ""),
            Arguments.of((Object) new String[]{"-" + ROOT_FOLDER_OPTION}, ""),
            Arguments.of((Object) new String[]{"--" + ROOT_FOLDER_LONG_OPTION}, ""),
            Arguments.of((Object) new String[]{"-?"}, ""),
            Arguments.of((Object) new String[]{"--debug", "true"}, ""),
            Arguments.of((Object) new String[]{"--debug"}, ""),
            Arguments.of((Object) new String[]{}, "")
        );
    }
}
