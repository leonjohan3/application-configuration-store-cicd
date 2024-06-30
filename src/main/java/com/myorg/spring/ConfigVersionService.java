package com.myorg.spring;

import com.myorg.model.appconfig.Application;
import com.myorg.model.appconfig.ConfigurationProfile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
@RequiredArgsConstructor
@Validated
public class ConfigVersionService {

    private final AppConfigFacade appConfigFacade;

    public SortedSet<Integer> getHostedConfigurationVersions(final Application application, final ConfigurationProfile configurationProfile) {
        final var versions = new TreeSet<Integer>();
        appConfigFacade.listHostedConfigurationVersions(application, configurationProfile)
            .forEach(hostedConfigurationVersion -> versions.add(hostedConfigurationVersion.versionNumber()));
        return versions;
    }

    public Optional<Integer> getLatestHostedConfigVersion(final Application application, final ConfigurationProfile configurationProfile) {
        final var versions = getHostedConfigurationVersions(application, configurationProfile);
        return Optional.ofNullable(versions.isEmpty() ? null : versions.last());
    }
}
