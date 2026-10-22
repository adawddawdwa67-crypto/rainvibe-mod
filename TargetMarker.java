package com.rainvibe;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.ActionResult;

/**
 * Cosmetic marker made of ordinary particles around the entity you hit / aim at.
 * Particles are normal world objects: not visible through walls.
 */
public final class TargetMarker {
    private static final String[] MODES = {"Выкл", "Кольцо", "Орбита", "Нимб"};
    private static final String[] COLOR_NAMES = {"Розовый", "Голубой", "Красный", "Зелёный", "Золотой"};
    private static final int[] COLORS = {0xFF4FD8, 0x40E0FF, 0xFF3030, 0x40FF70, 0xFFD040};

    private static int mode = 1;
    private static int color = 0;
    private static LivingEntity target;
    private static long lastSeen;
    private static final int SHOW_TICKS = 40;

    private TargetMarker() {}

    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() && entity instanceof LivingEntity le) {
                target = le;
                lastSeen = world.getTime();
            }
            return ActionResult.PASS;
        });
    }

    public static String nextMode() {
        mode = (mode + 1) % MODES.length;
        return MODES[mode];
    }

    public static String nextColor() {
        color = (color + 1) % COLORS.length;
        return COLOR_NAMES[color];
    }

    public static void tick(MinecraftClient mc) {
        ClientWorld w = mc.world;
        if (w == null || mc.player == null) return;

        if (mc.targetedEntity instanceof LivingEntity le && le != mc.player) {
            target = le;
            lastSeen = w.getTime();
        }
        if (mode == 0 || target == null) return;
        if (!target.isAlive() || target.isRemoved()) { target = null; return; }
        if (w.getTime() - lastSeen > SHOW_TICKS) return;

        double cx = target.getX(), cy = target.getY(), cz = target.getZ();
        double rad = target.getWidth() * 0.75 + 0.25;
        double h = target.getHeight();
        double t = w.getTime();
        DustParticleEffect dust = new DustParticleEffect(COLORS[color], 0.8f);

        switch (mode) {
            case 1 -> { // ring on the ground
                double base = t * 0.35;
                for (int i = 0; i < 14; i++) {
                    double a = base + i * (Math.PI * 2 / 14);
                    w.addParticle(dust, cx + Math.cos(a) * rad, cy + 0.05, cz + Math.sin(a) * rad, 0, 0.01, 0);
                }
            }
            case 2 -> { // three orbiting comets with trails
                for (int k = 0; k < 3; k++) {
                    double y = cy + h * (0.5 + 0.4 * Math.sin(t * 0.12 + k * 2.1));
                    for (int j = 0; j < 5; j++) {
                        double a = t * 0.28 + k * (Math.PI * 2 / 3) - j * 0.14;
                        w.addParticle(dust, cx + Math.cos(a) * (rad + 0.1), y, cz + Math.sin(a) * (rad + 0.1), 0, 0, 0);
                    }
                }
            }
            case 3 -> { // halo above the head
                double base = t * 0.3;
                for (int i = 0; i < 12; i++) {
                    double a = base + i * (Math.PI * 2 / 12);
                    w.addParticle(dust, cx + Math.cos(a) * 0.4, cy + h + 0.25, cz + Math.sin(a) * 0.4, 0, 0, 0);
                }
            }
            default -> {}
        }
    }
}
