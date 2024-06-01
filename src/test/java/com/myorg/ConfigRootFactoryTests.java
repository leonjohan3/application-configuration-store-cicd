package com.myorg;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

class ConfigRootFactoryTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void shouldSuccessfullyBuildConfigRoot() throws IOException, JSONException {
        // given: all data (test fixture) preparation
        final var rootFolder = Path.of("src/test/resources/provided_input/");

        // when : method to be checked invocation
        final var configRoot = ConfigRootFactory.createConfigRoot(rootFolder);

        // then : checks and assertions
        assertEquals(Files.readString(Path.of("src/test/resources/com/myorg/file.json")), OBJECT_MAPPER.writeValueAsString(configRoot), true);
    }

    @Test
    void shouldThrowExceptionForFileInEnvironmentsFolder() throws IOException {
        // given: all data (test fixture) preparation
        final var rootFolder = Path.of("src/test/resources/invalid_config_folder_structures/with_file_in_environments_folder");

        // when : method to be checked invocation
        final var exception = assertThrows(ConfigRootException.class, () -> ConfigRootFactory.createConfigRoot(rootFolder));

        // then : checks and assertions
        assertThat(exception.getMessage(), allOf(containsString("misplaced_file.tx"), containsString("only folders are allowed")));
    }

    @Test
    void shouldThrowExceptionForFolderInPlaceOfaConfigurationFile() throws IOException {
        // given: all data (test fixture) preparation
        final var rootFolder = Path.of("src/test/resources/invalid_config_folder_structures/with_folder_in_place_of_a_configuration_file");

        // when : method to be checked invocation
        final var exception = assertThrows(ConfigRootException.class, () -> ConfigRootFactory.createConfigRoot(rootFolder));

        // then : checks and assertions
        assertThat(exception.getMessage(), allOf(containsString("misplaced_folder"), containsString("only files are allowed")));
    }

    @Test
    void shouldThrowExceptionForMultipleConfigurationFiles() throws IOException {
        // given: all data (test fixture) preparation
        final var rootFolder = Path.of("src/test/resources/invalid_config_folder_structures/with_multiple_configuration_files");

        // when : method to be checked invocation
        final var exception = assertThrows(ConfigRootException.class, () -> ConfigRootFactory.createConfigRoot(rootFolder));

        // then : checks and assertions
        assertThat(exception.getMessage(), allOf(containsString("application.yam"), containsString("only one configuration file allowed")));
    }
}
