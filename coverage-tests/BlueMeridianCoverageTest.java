package com.jeremykenedy.bluemeridian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.junit.Test;

public final class BlueMeridianCoverageTest {
    @Test
    public void resolvesExplicitAndRandomSettingsWithSafeFallbacks() {
        BlueMeridianOptions explicit = BlueMeridianOptions.resolve(
                "dense", "fast", "sweeping", "off", "on", "off", false, new Random(2));
        assertEquals(260, explicit.stars);
        assertEquals(0.13f, explicit.spinSpeed, 0f);
        assertEquals(0.055f, explicit.cameraSpeed, 0f);
        assertFalse(explicit.atmosphere);
        assertTrue(explicit.nightLights);
        assertFalse(explicit.clouds);

        BlueMeridianOptions fallback = BlueMeridianOptions.resolve(
                "invalid", "invalid", "invalid", "invalid", "invalid", "invalid", false, new Random(3));
        assertEquals(150, fallback.stars);
        assertEquals(0.075f, fallback.spinSpeed, 0f);
        assertEquals(0.022f, fallback.cameraSpeed, 0f);
        assertTrue(fallback.atmosphere && fallback.nightLights && fallback.clouds);

        BlueMeridianOptions randomized = BlueMeridianOptions.resolve(
                "random", "random", "random", "random", "random", "random", false, new Random(9));
        assertTrue(randomized.stars == 70 || randomized.stars == 150 || randomized.stars == 260);
        assertTrue(randomized.spinSpeed == 0.035f || randomized.spinSpeed == 0.075f || randomized.spinSpeed == 0.13f);
        assertTrue(randomized.cameraSpeed == 0f || randomized.cameraSpeed == 0.022f || randomized.cameraSpeed == 0.055f);
        BlueMeridianOptions all = BlueMeridianOptions.resolve(
                "normal", "natural", "gentle", "on", "on", "on", true, new Random(7));
        assertTrue(all.stars == 70 || all.stars == 150 || all.stars == 260);
    }

    @Test
    public void mapsAndValidatesEverySupportedSetting() {
        assertEquals(70, BlueMeridianOptions.starsFor("sparse"));
        assertEquals(150, BlueMeridianOptions.starsFor("normal"));
        assertEquals(260, BlueMeridianOptions.starsFor("dense"));
        assertEquals(0.035f, BlueMeridianOptions.spinFor("slow"), 0f);
        assertEquals(0.075f, BlueMeridianOptions.spinFor("natural"), 0f);
        assertEquals(0.13f, BlueMeridianOptions.spinFor("fast"), 0f);
        assertEquals(0f, BlueMeridianOptions.cameraFor("still"), 0f);
        assertEquals(0.022f, BlueMeridianOptions.cameraFor("gentle"), 0f);
        assertEquals(0.055f, BlueMeridianOptions.cameraFor("sweeping"), 0f);
        assertFalse(SettingsValues.isSupported(null, "on"));
        assertFalse(SettingsValues.isSupported("clouds", null));
        String[][] settings = {
            {"star_density", "sparse", "normal", "dense", "random"},
            {"spin_speed", "slow", "natural", "fast", "random"},
            {"camera_motion", "still", "gentle", "sweeping", "random"},
            {"atmosphere", "off", "on", "random"},
            {"night_lights", "off", "on", "random"},
            {"clouds", "off", "on", "random"},
            {"randomize_all", "true", "false"}
        };
        for (String[] setting : settings) {
            for (int i = 1; i < setting.length; i++) assertTrue(SettingsValues.isSupported(setting[0], setting[i]));
            assertFalse(SettingsValues.isSupported(setting[0], "invalid"));
        }
        assertFalse(SettingsValues.isSupported("unknown", "value"));
    }
}
