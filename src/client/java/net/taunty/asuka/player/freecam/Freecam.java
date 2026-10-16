package net.taunty.asuka.player.freecam;

import net.minecraft.client.Minecraft;

public class Freecam {

    private static FreecamEntity camera;
    private static boolean enabled = false;

    public static void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    public static void enable() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        camera = new FreecamEntity(minecraft.level);

        camera.setPos(
                minecraft.player.getX(),
                minecraft.player.getY(),
                minecraft.player.getZ()
        );

        camera.setYRot(minecraft.player.getYRot());
        camera.setXRot(minecraft.player.getXRot());

        camera.xo = camera.getX();
        camera.yo = camera.getY();
        camera.zo = camera.getZ();

        camera.yRotO = camera.getYRot();
        camera.xRotO = camera.getXRot();

        minecraft.setCameraEntity(camera);
        enabled = true;
    }

    public static void disable() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setCameraEntity(minecraft.player);
        camera = null;
        enabled = false;
    }
}
