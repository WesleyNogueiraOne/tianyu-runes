package com.github.WesleyNogueiraOne.tianyu_runes.event;

import com.github.WesleyNogueiraOne.tianyu_runes.Tianyu_runes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = Tianyu_runes.MODID, value = Dist.CLIENT)
public class ManaHudHandler {

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        var name = event.getName();
        // esconde qualquer camada de HUD do Iron's relacionada à mana
        if (name.getNamespace().equals("irons_spellbooks") && name.getPath().contains("mana")) {
            event.setCanceled(true);
        }
    }
}