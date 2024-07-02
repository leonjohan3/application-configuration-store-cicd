package org.example;

import static org.apache.commons.lang3.StringUtils.removeStart;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utilities {

    public static String extractPlainApplicationName(final String appNameWithPrefix, final String configGroupPrefix) {
        return removeStart(appNameWithPrefix, configGroupPrefix + "/");
    }
}
