package org.example.spring;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.example.model.appconfig.Application;
import org.example.model.appconfig.ConfigurationProfile;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
@RequiredArgsConstructor
@Validated
public class ConfigVersionService {

    private final AppConfigFacade appConfigFacade;

    public SortedSet<Integer> getHostedConfigurationVersions(final Application application, final ConfigurationProfile configurationProfile) {
        final var versions = new TreeSet<Integer>();
        appConfigFacade.listHostedConfigurationVersions(application, configurationProfile)
            .forEach(configurationVersion -> versions.add(configurationVersion.versionNumber()));
        return versions;
    }

    public Optional<Integer> getLatestHostedConfigVersion(final Application application, final ConfigurationProfile configurationProfile) {
        final var versions = getHostedConfigurationVersions(application, configurationProfile);
        return Optional.ofNullable(versions.isEmpty() ? null : versions.last());
    }
}
