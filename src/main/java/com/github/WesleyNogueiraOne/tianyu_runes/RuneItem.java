package com.github.WesleyNogueiraOne.tianyu_runes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class RuneItem extends Item {
    private final RuneElement element;
    private final int tier;

    public RuneItem(Properties properties, RuneElement element, int tier) {
        super(properties);
        this.element = element;
        this.tier = tier;
    }

    public RuneElement getElement() { return element; }
    public int getTier() { return tier; }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tianyu_runes.rune").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}