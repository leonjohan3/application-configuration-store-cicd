package com.myorg.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ServiceConstants {

    public static final String CONFIG_GROUP_PREFIX_PATTERN = "^[a-zA-Z0-9]{3}$";
    public static final String CONFIG_GROUP_PREFIX_MESSAGE = "configGroupPrefix must be letters or digits and must have a length of 3";
    public static final int MAX_WALK_DEPTH = 1;
    public static final String APP_CONFIG_GROUP_PREFIX = "app.config.group.prefix";
    public static final String APP_CONFIG_ROOT_CONFIG_FOLDER = "app.config.root.config.folder";
    public static final String APP_CONFIG_GROUP_PREFIX_ENV = "APP_CONFIG_GROUP_PREFIX";
    public static final String APP_CONFIG_ROOT_CONFIG_FOLDER_ENV = "APP_CONFIG_ROOT_CONFIG_FOLDER";
}
