package com.jeremykenedy.bluemeridian;

import java.util.Random;

public final class BlueMeridianOptions {
    public final int stars;
    public final float spinSpeed;
    public final float cameraSpeed;
    public final boolean atmosphere;
    public final boolean nightLights;
    public final boolean clouds;

    private BlueMeridianOptions(int stars, float spinSpeed, float cameraSpeed,
            boolean atmosphere, boolean nightLights, boolean clouds) {
        this.stars = stars;
        this.spinSpeed = spinSpeed;
        this.cameraSpeed = cameraSpeed;
        this.atmosphere = atmosphere;
        this.nightLights = nightLights;
        this.clouds = clouds;
    }

    public static BlueMeridianOptions resolve(String starDensity, String spin, String camera,
            String atmosphere, String nightLights, String clouds, boolean randomizeAll, Random random) {
        String density = choose(starDensity, randomizeAll, random, "normal", "sparse", "dense");
        String spinChoice = choose(spin, randomizeAll, random, "natural", "slow", "fast");
        String cameraChoice = choose(camera, randomizeAll, random, "gentle", "still", "sweeping");
        String atmosphereChoice = choose(atmosphere, randomizeAll, random, "on", "off");
        String lightsChoice = choose(nightLights, randomizeAll, random, "on", "off");
        String cloudsChoice = choose(clouds, randomizeAll, random, "on", "off");
        return new BlueMeridianOptions(starsFor(density), spinFor(spinChoice), cameraFor(cameraChoice),
                "on".equals(atmosphereChoice), "on".equals(lightsChoice), "on".equals(cloudsChoice));
    }

    private static String choose(String selected, boolean randomizeAll, Random random, String... values) {
        if (randomizeAll || "random".equals(selected)) return values[random.nextInt(values.length)];
        for (String value : values) if (value.equals(selected)) return selected;
        return values[0];
    }

    static int starsFor(String density) {
        if ("sparse".equals(density)) return 70;
        if ("dense".equals(density)) return 260;
        return 150;
    }

    static float spinFor(String speed) {
        if ("slow".equals(speed)) return 0.035f;
        if ("fast".equals(speed)) return 0.13f;
        return 0.075f;
    }

    static float cameraFor(String movement) {
        if ("still".equals(movement)) return 0f;
        if ("sweeping".equals(movement)) return 0.055f;
        return 0.022f;
    }
}
