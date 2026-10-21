package net.taunty.asuka.highlight;

public class HandheldAnimation {
    private static boolean enabled = false;
    private static float swingDuration = 1.0f;

    public static void toggle() {
        enabled = !enabled;
    }
    public static boolean isEnabled() {
        return enabled;
    }
    public static float getSwingDuration() {
        return swingDuration;
    }
}
