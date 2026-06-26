package com.github.WesleyNogueiraOne.tianyu_runes.samurai;


import java.util.EnumMap;
import java.util.Map;

/**
 * Per-player runtime combat state for the Samurai class, stored as a Data Attachment.
 *
 * This is intentionally TRANSIENT: cooldowns, combo step, i-frames and stamina reset on relog,
 * which is usually what you want for a combat class. If you ever need persistence (e.g. you fold
 * "which class is active" in here), add a Codec and call .serialize(...) in ModAttachments.
 */
public class SamuraiData {

    private final Map<Ability, Integer> cooldowns = new EnumMap<>(Ability.class);
    private int comboStep = 0;
    private int comboResetTimer = 0;
    private int iFrames = 0;
    private float stamina = 100f;
    private int juggleFloatTicks = 0;
    private boolean gravityFrozen = false;

    // --- Cooldowns (ticks) ---
    public boolean isOnCooldown(Ability ability) {
        return cooldowns.getOrDefault(ability, 0) > 0;
    }

    public int getCooldown(Ability ability) {
        return cooldowns.getOrDefault(ability, 0);
    }

    public void setCooldown(Ability ability, int ticks) {
        cooldowns.put(ability, ticks);
    }

    // --- Combo / juggle ---
    public int getComboStep() {
        return comboStep;
    }

    public void advanceCombo(int resetTicks) {
        comboStep++;
        comboResetTimer = resetTicks;
    }

    public void resetCombo() {
        comboStep = 0;
        comboResetTimer = 0;
    }

    // --- i-frames (enforce these in a LivingIncomingDamageEvent / hurt handler) ---
    public boolean hasIFrames() {
        return iFrames > 0;
    }

    public void grantIFrames(int ticks) {
        iFrames = Math.max(iFrames, ticks);
    }

    // --- Stamina ---
    public float getStamina() {
        return stamina;
    }

    public boolean canSpend(float amount) {
        return stamina >= amount;
    }

    public void spend(float amount) {
        stamina = Math.max(0f, stamina - amount);
    }

    // --- Juggle float (hang time no ar pós-launcher) ---
    public boolean isFloating() { return juggleFloatTicks > 0; }
    public void startJuggleFloat(int ticks) { juggleFloatTicks = Math.max(juggleFloatTicks, ticks); }
    public int getJuggleRemaining() { return juggleFloatTicks; }
    public boolean isGravityFrozen() { return gravityFrozen; }
    public void setGravityFrozen(boolean v) { gravityFrozen = v; }
    /**
     * Call once per SERVER tick (see SamuraiAbilities#onPlayerTick).
     * Decrements cooldowns / i-frames, decays the combo, and regenerates stamina.
     */
    public void tick() {
        for (Ability a : Ability.values()) {
            int cd = cooldowns.getOrDefault(a, 0);
            if (cd > 0) cooldowns.put(a, cd - 1);
        }
        if (iFrames > 0) iFrames--;
        if (juggleFloatTicks > 0) juggleFloatTicks--;   // <- adiciona esta linha
        if (comboResetTimer > 0 && --comboResetTimer == 0) comboStep = 0;
        if (stamina < 100f) stamina = Math.min(100f, stamina + 0.5f); // ~10 stamina/s
    }
}