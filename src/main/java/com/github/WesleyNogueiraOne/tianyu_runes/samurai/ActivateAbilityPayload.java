package com.github.WesleyNogueiraOne.tianyu_runes.samurai;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> Server: "I pressed the keybind for ability #abilityId".
 * The client NEVER applies the effect itself; it only requests, and the server is authoritative.
 */
public record ActivateAbilityPayload(int abilityId) implements CustomPacketPayload {

    public static final Type<ActivateAbilityPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModAttachments.MODID, "activate_ability"));

    public static final StreamCodec<FriendlyByteBuf, ActivateAbilityPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ActivateAbilityPayload::abilityId,
                    ActivateAbilityPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}