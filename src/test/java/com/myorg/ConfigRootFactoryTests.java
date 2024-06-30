package com.myorg;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.exception.ConfigRootException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConfigRootFactoryTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void shouldSuccessfullyBuildConfigRoot() throws IOException, JSONException {
        // given: all data (test fixture) preparation
        final var rootFolder = Path.of("src/test/resources/provided_input/");
        final var dotGitFolder = Path.of(rootFolder.toString(), ".git");

        if (!Files.exists(dotGitFolder)) { // to test that .git folders in the root are ignored
            Files.createDirectory(dotGitFolder);
        }

        // when : method to be checked invocation
        final var configRoot = ConfigRootFactory.createConfigRoot(rootFolder);

        // then : checks and assertions
        assertEquals(Files.readString(Path.of("src/test/resources/com/myorg/expectedConfigRoot.json")), OBJECT_MAPPER.writeValueAsString(configRoot), false);
    }

    @ParameterizedTest
    @MethodSource("getShouldThrowConfigRootExceptionArguments")
    void shouldThrowConfigRootException(final String rootFolder, final String expectedValueOne, final String expectedValueTwo) {

        // given: all data (test fixture) preparation
        final var rootFolderPath = Path.of("src/test/resources/invalid_config_folder_structures/" + rootFolder);

        // when : method to be checked invocation
        final var exception = assertThrows(ConfigRootException.class, () -> ConfigRootFactory.createConfigRoot(rootFolderPath));

        // then : checks and assertions
        assertThat(exception.getMessage(), allOf(containsString(expectedValueOne), containsString(expectedValueTwo)));
    }

    private static Stream<Arguments> getShouldThrowConfigRootExceptionArguments() {
        return Stream.of(
            Arguments.of("with_file_in_environments_folder", "misplaced_file.tx", "only folders are allowed"),
            Arguments.of("with_folder_in_place_of_a_configuration_file", "misplaced_folder", "only files are allowed"),
            Arguments.of("with_multiple_configuration_files", "application.yam", "only one configuration file allowed")
        );
    }

    @ParameterizedTest
    @MethodSource("getShouldThrowIllegalArgumentExceptionArguments")
    void shouldThrowIllegalArgumentException(final String rootFolder, final String expectedValue) {

        // given: all data (test fixture) preparation
        final var rootFolderPath = Path.of("src/test/resources/invalid_config_folder_structures/" + rootFolder);

        // when : method to be checked invocation
        final var exception = assertThrows(IllegalArgumentException.class, () -> ConfigRootFactory.createConfigRoot(rootFolderPath));

        // then : checks and assertions
        assertThat(exception.getMessage(), containsString(expectedValue));
    }

    private static Stream<Arguments> getShouldThrowIllegalArgumentExceptionArguments() {
        return Stream.of(
            Arguments.of("with_invalid_application_name", "d application name: only alphanumeric values with u"),
            Arguments.of("with_invalid_environment_name", "d environment name: only alphanumeric values with u")
        );
    }
}
