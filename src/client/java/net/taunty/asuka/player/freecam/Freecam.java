package net.taunty.asuka.player.freecam;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

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

    public static void rotateCamera(double yaw, double pitch) {
        if (camera == null) {
            return;
        }
        camera.turn(yaw, pitch);
    }

    public static void moveCamera(Input input) {
        Minecraft minecraft = Minecraft.getInstance();

        if (camera == null) {
            return;
        }

        if (minecraft.player == null || minecraft.level != camera.level()) {
            disable();
            return;
        }

        camera.xo = camera.getX();
        camera.yo = camera.getY();
        camera.zo = camera.getZ();

        if (minecraft.gui.screen() != null) {
            return;
        }

        Vec3 movement = getMovement(input);

        double speed = input.sprint() ? 1.0 : 0.5;
        camera.setPos(camera.position().add(movement.scale(speed)));
    }

    private static @NonNull Vec3 getMovement(Input input) {
        double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        double sideways = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
        double vertical = (input.jump() ? 1 : 0) - (input.shift() ? 1 : 0);

        double yaw = Math.toRadians(camera.getYRot());

        Vec3 movement = new Vec3(
                sideways * Math.cos(yaw) - forward * Math.sin(yaw),
                vertical,
                forward * Math.cos(yaw) + sideways * Math.sin(yaw)
        );

        // Prevent diagonal movement from being faster.
        if (movement.lengthSqr() > 1) {
            movement = movement.normalize();
        }
        return movement;
    }

    public static void disable() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setCameraEntity(minecraft.player);
        camera = null;
        enabled = false;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static FreecamEntity getCamera() {
        return camera;
    }
}
