package com.github.WesleyNogueiraOne.tianyu_runes.samurai;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Registers the SamuraiData attachment. Data Attachments are the NeoForge 1.21 replacement
 * for the old Capability system.
 */
public final class ModAttachments {

    public static final String MODID = "tianyu_runes";

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<SamuraiData>> SAMURAI_DATA =
            ATTACHMENT_TYPES.register("samurai_data",
                    () -> AttachmentType.builder(SamuraiData::new).build());
    // For persistence later: .serialize(YourCodec).copyOnDeath()

    /** Convenience accessor. getData() lazily creates the default instance if absent. */
    public static SamuraiData get(Player player) {
        return player.getData(SAMURAI_DATA.get());
    }

    private ModAttachments() {}

    /** Call from your @Mod main class constructor: ModAttachments.register(modEventBus); */
    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
