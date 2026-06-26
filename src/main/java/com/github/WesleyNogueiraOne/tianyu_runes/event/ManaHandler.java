package com.github.WesleyNogueiraOne.tianyu_runes.event;

import com.github.WesleyNogueiraOne.tianyu_runes.Tianyu_runes;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Tianyu_runes.MODID)
public class ManaHandler {

    // id único do nosso boost de mana máxima (evita empilhar a cada login)
    private static final ResourceLocation MANA_BOOST_ID =
            ResourceLocation.fromNamespaceAndPath(Tianyu_runes.MODID, "infinite_mana");

    // o atributo MAX_MANA do Iron's tem teto de 1.000.000 — usamos isso como "mana infinita"
    private static final double MANA_BOOST_AMOUNT = 1_000_000.0D;

    // 1) Nenhuma magia consome mana: qualquer QUEDA de mana é cancelada.
    @SubscribeEvent
    public static void onChangeMana(ChangeManaEvent event) {
        if (event.getNewMana() < event.getOldMana()) {
            event.setCanceled(true);
        }
    }

    // 2) Reforço: zera o custo de mana de qualquer cast.
    @SubscribeEvent
    public static void onSpellOnCast(SpellOnCastEvent event) {
        event.setManaCost(0);
    }

    // 3) Garante mana sempre suficiente pro check inicial (ao entrar e ao renascer).
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        applyInfiniteMana(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        applyInfiniteMana(event.getEntity());
    }

    private static void applyInfiniteMana(Player player) {
        if (player.level().isClientSide()) return;

        AttributeInstance maxMana = player.getAttribute(AttributeRegistry.MAX_MANA);
        if (maxMana != null) {
            AttributeModifier modifier = new AttributeModifier(
                    MANA_BOOST_ID, MANA_BOOST_AMOUNT, AttributeModifier.Operation.ADD_VALUE);
            maxMana.addOrUpdateTransientModifier(modifier);
        }

        // enche a mana até o novo máximo
        MagicData magicData = MagicData.getPlayerMagicData(player);
        magicData.setMana((float) player.getAttributeValue(AttributeRegistry.MAX_MANA));
    }
}