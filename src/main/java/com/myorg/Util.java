package com.myorg;

import static org.apache.commons.lang3.StringUtils.removeStart;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Util {

    public static String extractPlainApplicationName(final String applicationNameWithPrefix, final String configGroupPrefix) {
        return removeStart(applicationNameWithPrefix, configGroupPrefix + "/");
    }
}
