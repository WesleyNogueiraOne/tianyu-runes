package com.github.WesleyNogueiraOne.tianyu_runes.samurai;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ModAttachments.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetworking {

    private ModNetworking() {}

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Bump this version string if you ever change the payload format or reorder Ability.
        final PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToServer(
                ActivateAbilityPayload.TYPE,
                ActivateAbilityPayload.STREAM_CODEC,
                SamuraiAbilities::handlePayload
        );
    }
}