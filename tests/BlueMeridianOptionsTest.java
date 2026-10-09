package com.jeremykenedy.bluemeridian;

import java.util.Random;

public final class BlueMeridianOptionsTest {
    public static void main(String[] args) {
        verifyResolvedValues();
        verifyRandomBounds();
        verifyProviderValidation();
        System.out.println("Blue Meridian settings tests passed.");
    }

    private static void verifyResolvedValues() {
        BlueMeridianOptions options = BlueMeridianOptions.resolve("dense", "fast", "sweeping", "off", "on", "off", false, new Random(1));
        check(options.stars == 260 && options.spinSpeed == 0.13f && options.cameraSpeed == 0.055f, "explicit motion values");
        check(!options.atmosphere && options.nightLights && !options.clouds, "explicit effect values");
        check(BlueMeridianOptions.starsFor("sparse") == 70, "sparse star count");
        check(BlueMeridianOptions.starsFor("normal") == 150, "normal star count");
        check(BlueMeridianOptions.starsFor("unknown") == 150, "safe density fallback");
        check(BlueMeridianOptions.spinFor("slow") == 0.035f, "slow spin");
        check(BlueMeridianOptions.spinFor("natural") == 0.075f, "natural spin");
        check(BlueMeridianOptions.spinFor("other") == 0.075f, "safe spin fallback");
        check(BlueMeridianOptions.cameraFor("still") == 0f, "still camera");
        check(BlueMeridianOptions.cameraFor("gentle") == 0.022f, "gentle camera");
        check(BlueMeridianOptions.cameraFor("sweeping") == 0.055f, "sweeping camera");
        check(BlueMeridianOptions.cameraFor("other") == 0.022f, "safe camera fallback");
    }

    private static void verifyRandomBounds() {
        for (int seed = 0; seed < 12; seed++) {
            BlueMeridianOptions individual = BlueMeridianOptions.resolve("random", "random", "random", "random", "random", "random", false, new Random(seed));
            assertRanges(individual);
            BlueMeridianOptions all = BlueMeridianOptions.resolve("sparse", "slow", "still", "off", "off", "off", true, new Random(seed));
            assertRanges(all);
        }
        BlueMeridianOptions invalid = BlueMeridianOptions.resolve("other", "other", "other", "other", "other", "other", false, new Random(1));
        check(invalid.stars == 150 && invalid.spinSpeed == 0.075f && invalid.cameraSpeed == 0.022f,
                "invalid stored settings use defaults");
    }

    private static void assertRanges(BlueMeridianOptions option) {
        check(option.stars == 70 || option.stars == 150 || option.stars == 260, "random star density range");
        check(option.spinSpeed == 0.035f || option.spinSpeed == 0.075f || option.spinSpeed == 0.13f, "random spin range");
        check(option.cameraSpeed == 0f || option.cameraSpeed == 0.022f || option.cameraSpeed == 0.055f, "random camera range");
    }

    private static void verifyProviderValidation() {
        check(!SettingsValues.isSupported(null, "on"), "null keys rejected");
        check(!SettingsValues.isSupported("clouds", null), "null values rejected");
        String[] keys = {"star_density", "spin_speed", "camera_motion", "atmosphere", "night_lights", "clouds"};
        String[][] values = {{"sparse", "normal", "dense", "random"}, {"slow", "natural", "fast", "random"},
            {"still", "gentle", "sweeping", "random"}, {"off", "on", "random"}, {"off", "on", "random"}, {"off", "on", "random"}};
        for (int key = 0; key < keys.length; key++) {
            for (String value : values[key]) check(SettingsValues.isSupported(keys[key], value), keys[key] + " accepts " + value);
            check(!SettingsValues.isSupported(keys[key], "invalid"), keys[key] + " rejects invalid value");
        }
        check(SettingsValues.isSupported("randomize_all", "true"), "randomize enabled");
        check(SettingsValues.isSupported("randomize_all", "false"), "randomize disabled");
        check(!SettingsValues.isSupported("unknown", "value"), "unknown settings rejected");
    }

    private static void check(boolean result, String message) {
        if (!result) throw new AssertionError(message);
    }
}
