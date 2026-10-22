package com.rainvibe;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class RainVibeClient implements ClientModInitializer {
    // dark blue-violet water
    private static final int WATER_TINT = 0x2A3050;

    private static final String[] NAMES = {"Выкл", "Лёгкий дождь", "Дождь", "Гроза"};
    private static final float[] RAIN = {0f, 0.55f, 1.0f, 1.0f};
    private static final float[] THUNDER = {0f, 0f, 0f, 0.8f};
    private static int level = 2;
    private static float rainNow = 0f;
    private static float thunderNow = 0f;

    private static KeyBinding rainKey;
    private static KeyBinding markerKey;
    private static KeyBinding colorKey;

    public static float fakeRain() { return rainNow; }
    public static float fakeThunder() { return thunderNow; }

    @Override
    public void onInitializeClient() {
        FluidRenderHandlerRegistry.INSTANCE.register(
                Fluids.WATER, Fluids.FLOWING_WATER, SimpleFluidRenderHandler.coloredWater(WATER_TINT));

        // keys:  [  ]  \
        rainKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rainvibe.rain", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, "key.categories.rainvibe"));
        markerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rainvibe.marker", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, "key.categories.rainvibe"));
        colorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rainvibe.color", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "key.categories.rainvibe"));

        TargetMarker.init();
        ClientTickEvents.END_CLIENT_TICK.register(RainVibeClient::tick);
    }

    private static void say(MinecraftClient mc, String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal(s), true);
    }

    private static void tick(MinecraftClient mc) {
        while (rainKey.wasPressed()) {
            level = (level + 1) % NAMES.length;
            say(mc, "RainVibe: " + NAMES[level]);
        }
        while (markerKey.wasPressed()) say(mc, "Метка цели: " + TargetMarker.nextMode());
        while (colorKey.wasPressed()) say(mc, "Цвет метки: " + TargetMarker.nextColor());

        rainNow += (RAIN[level] - rainNow) * 0.03f;
        thunderNow += (THUNDER[level] - thunderNow) * 0.03f;
        if (Math.abs(RAIN[level] - rainNow) < 0.002f) rainNow = RAIN[level];
        if (Math.abs(THUNDER[level] - thunderNow) < 0.002f) thunderNow = THUNDER[level];

        ClientWorld world = mc.world;
        if (world != null && mc.player != null && level == 3 && world.random.nextInt(450) == 0) {
            double ang = world.random.nextDouble() * Math.PI * 2;
            double dist = 40 + world.random.nextDouble() * 60;
            world.playSound(mc.player.getX() + Math.cos(ang) * dist, mc.player.getY() + 20,
                    mc.player.getZ() + Math.sin(ang) * dist,
                    SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER,
                    4.0f, 0.6f + world.random.nextFloat() * 0.4f, false);
        }

        TargetMarker.tick(mc);
    }
}
