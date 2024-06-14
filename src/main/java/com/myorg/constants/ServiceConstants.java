package com.myorg.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ServiceConstants {

    public static final String APP_PREFIX_PATTERN = "^[a-zA-Z0-9]{3}$";
    public static final String APP_PREFIX_MESSAGE = "applicationPrefix must be letters or digits and must have a length of 3";
    public static final int MAX_WALK_DEPTH = 1;
    public static final String APP_CONFIG_APPLICATION_PREFIX = "app.config.application.prefix";
    public static final String APP_CONFIG_ROOT_CONFIG_FOLDER = "app.config.root.config.folder";
}
