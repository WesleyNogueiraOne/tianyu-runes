package com.github.WesleyNogueiraOne.tianyu_runes.samurai;

import javax.annotation.Nullable;

/**
 * The Samurai class abilities. Cooldown in ticks (20 ticks = 1s), plus a stamina cost.
 * The ordinal is what gets sent over the wire, so DON'T reorder these without bumping
 * the network version in ModNetworking.
 */
public enum Ability {
    DASH(20, 15f),       // 1.0s cooldown, 15 stamina
    LAUNCHER(60, 30f),   // 3.0s cooldown, 30 stamina
    AERIAL(15, 10f);     // 0.75s cooldown, 10 stamina

    public final int cooldownTicks;
    public final float staminaCost;

    Ability(int cooldownTicks, float staminaCost) {
        this.cooldownTicks = cooldownTicks;
        this.staminaCost = staminaCost;
    }

    private static final Ability[] BY_ID = values();

    @Nullable
    public static Ability byId(int id) {
        return (id >= 0 && id < BY_ID.length) ? BY_ID[id] : null;
    }
}