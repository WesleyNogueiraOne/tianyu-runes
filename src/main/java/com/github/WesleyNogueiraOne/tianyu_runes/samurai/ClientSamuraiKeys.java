package com.github.WesleyNogueiraOne.tianyu_runes.samurai;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Client-only: registers the keybinds and, each client tick, drains any presses and
 * fires a request to the server. Default keys: R = dash, V = launcher, C = aerial.
 */
@EventBusSubscriber(modid = ModAttachments.MODID, value = Dist.CLIENT)
public final class ClientSamuraiKeys {

    private static final String CATEGORY = "key.categories." + ModAttachments.MODID;

    public static final KeyMapping DASH = new KeyMapping(
            "key." + ModAttachments.MODID + ".dash", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping LAUNCHER = new KeyMapping(
            "key." + ModAttachments.MODID + ".launcher", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping AERIAL = new KeyMapping(
            "key." + ModAttachments.MODID + ".aerial", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);

    private ClientSamuraiKeys() {}

    /** Polling happens on the GAME bus. consumeClick() drains the press queue. */
    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        while (DASH.consumeClick())     send(Ability.DASH);
        while (LAUNCHER.consumeClick()) send(Ability.LAUNCHER);
        while (AERIAL.consumeClick())   send(Ability.AERIAL);
    }

    private static void send(Ability ability) {
        PacketDistributor.sendToServer(new ActivateAbilityPayload(ability.ordinal()));
    }

    /** Registration is a MOD-bus event, so it lives in a nested subscriber. */
    @EventBusSubscriber(modid = ModAttachments.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent
        public static void onRegisterKeys(final RegisterKeyMappingsEvent event) {
            event.register(DASH);
            event.register(LAUNCHER);
            event.register(AERIAL);
        }
    }
}
