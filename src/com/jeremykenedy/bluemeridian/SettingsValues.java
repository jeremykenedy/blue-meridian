package com.jeremykenedy.bluemeridian;

public final class SettingsValues {
    private SettingsValues() {}

    public static boolean isSupported(String key, String value) {
        if (key == null || value == null) return false;
        if ("star_density".equals(key)) return oneOf(value, "sparse", "normal", "dense", "random");
        if ("spin_speed".equals(key)) return oneOf(value, "slow", "natural", "fast", "random");
        if ("camera_motion".equals(key)) return oneOf(value, "still", "gentle", "sweeping", "random");
        if ("atmosphere".equals(key) || "night_lights".equals(key) || "clouds".equals(key)) {
            return oneOf(value, "off", "on", "random");
        }
        return "randomize_all".equals(key) && oneOf(value, "true", "false");
    }

    private static boolean oneOf(String value, String... allowed) {
        for (String option : allowed) if (option.equals(value)) return true;
        return false;
    }
}
