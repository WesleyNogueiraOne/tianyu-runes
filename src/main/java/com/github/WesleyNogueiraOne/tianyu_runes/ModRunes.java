package com.github.WesleyNogueiraOne.tianyu_runes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

public class ModRunes {

    // guarda todas as runas registradas, pra alimentar a aba do criativo
    public static final List<DeferredItem<RuneItem>> ALL_RUNES = new ArrayList<>();

    public static final DeferredItem<RuneItem> RUNE_FIRE_1  = registerRune("rune_fire_1",  RuneElement.FIRE,  1);
    public static final DeferredItem<RuneItem> RUNE_FIRE_2  = registerRune("rune_fire_2",  RuneElement.FIRE,  2);
    public static final DeferredItem<RuneItem> RUNE_WATER_1 = registerRune("rune_water_1", RuneElement.WATER, 1);
    public static final DeferredItem<RuneItem> RUNE_WATER_2 = registerRune("rune_water_2", RuneElement.WATER, 2);

    // aba do criativo só com as runas
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RUNES_TAB =
            Tianyu_runes.CREATIVE_MODE_TABS.register("runes", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tianyu_runes.runes"))
                    .icon(() -> new ItemStack(RUNE_FIRE_1.get()))
                    .displayItems((params, output) -> {
                        for (DeferredItem<RuneItem> rune : ALL_RUNES) {
                            output.accept(rune.get());
                        }
                    })
                    .build());

    private static DeferredItem<RuneItem> registerRune(String name, RuneElement element, int tier) {
        DeferredItem<RuneItem> rune = Tianyu_runes.ITEMS.registerItem(
                name,
                props -> new RuneItem(props, element, tier),
                new Item.Properties()
        );
        ALL_RUNES.add(rune);
        return rune;
    }

    // força o carregamento da classe (senão os campos static nunca rodam e nada registra)
    public static void init() {}
}