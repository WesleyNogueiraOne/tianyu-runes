package com.github.WesleyNogueiraOne.tianyu_runes.samurai;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;

/**
 * Authoritative server-side logic. The dash is fully wired as a working example; launcher and
 * aerial are stubs we fill next. Animation (Zigy Player Animator API) and VFX hooks are marked.
 */
@EventBusSubscriber(modid = ModAttachments.MODID)
public final class SamuraiAbilities {

    private static final double DASH_STRENGTH = 1.6;
    private static final double DASH_LIFT = 0.15;

    // --- Launcher ---
    private static final double LAUNCH_REACH = 2.0;    // alcance curto, "do teu braço"
    private static final double LAUNCH_WIDTH = 1.0;    // caixa justa
    private static final double LAUNCH_POP   = 2.0;   // subida (player E inimigo, IGUAL)
    private static final float  LAUNCH_DAMAGE = 4.0f;
    private static final int    FLOAT_TICKS = 13;      // 3 sobe + 10 congelado ≈ 0.65s
    private static final int    RISE_TICKS  = 3;       // sobe 3 ticks, depois trava

    // Inimigos suspensos pelo launcher: UUID -> ticks restantes.
    private static final Map<UUID, Integer> SUSPENDED = new ConcurrentHashMap<>();

    private SamuraiAbilities() {}

    /** Network thread -> hop to the main thread before touching the world. */
    public static void handlePayload(final ActivateAbilityPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Ability ability = Ability.byId(payload.abilityId());
            if (ability != null) tryActivate(player, ability);
        });
    }

    public static void tryActivate(ServerPlayer player, Ability ability) {
        SamuraiData data = ModAttachments.get(player);

        // 1) Gatekeeping
        if (!hasSamuraiWeapon(player)) return;           // TODO: katana check + active class
        if (data.isOnCooldown(ability)) return;
        if (!data.canSpend(ability.staminaCost)) return;

        // 2) Pay costs
        data.setCooldown(ability, ability.cooldownTicks);
        data.spend(ability.staminaCost);

        // 3) Execute
        switch (ability) {
            case DASH     -> doDash(player, data);
            case LAUNCHER -> doLauncher(player, data);
            case AERIAL   -> doAerial(player, data);
        }

        // 4) TODO: trigger the synced Player Animator animation (Zigy API) here so every
        //          tracking client sees the move, not just the caster.
        // 5) TODO: spawn VFX here via ServerLevel#sendParticles or a GeckoLib effect entity.
    }

    private static void doDash(ServerPlayer player, SamuraiData data) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z).normalize().scale(DASH_STRENGTH);

        player.setDeltaMovement(flat.x, DASH_LIFT, flat.z);
        player.hasImpulse = true;
        player.fallDistance = 0;
        data.grantIFrames(6); // ~0.3s; enforce in a damage handler

        // CRITICAL: without this the client's movement prediction snaps the player back.
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }

    private static void doLauncher(ServerPlayer player, SamuraiData data) {
        Vec3 flatLook = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
        Vec3 origin = player.position();
        Vec3 reachEnd = origin.add(flatLook.scale(LAUNCH_REACH));
        AABB box = new AABB(origin, reachEnd).inflate(LAUNCH_WIDTH);

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class, box, e -> e != player && e.isAlive());

        if (targets.isEmpty()) return; // SEM alvo mirado -> não levanta ninguém, nem tu

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), LAUNCH_DAMAGE);
            target.setDeltaMovement(target.getDeltaMovement().x * 0.2, LAUNCH_POP, target.getDeltaMovement().z * 0.2);
            target.fallDistance = 0;
            target.hasImpulse = true;
            SUSPENDED.put(target.getUUID(), FLOAT_TICKS);
            if (target instanceof ServerPlayer sp) sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            else target.hurtMarked = true;
        }

        // Sobe junto, MESMA força do inimigo.
        player.setDeltaMovement(player.getDeltaMovement().x, LAUNCH_POP, player.getDeltaMovement().z);
        player.hasImpulse = true;
        player.fallDistance = 0;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        data.startJuggleFloat(FLOAT_TICKS);
    }
    private static void doAerial(ServerPlayer player, SamuraiData data) {
        // TODO: advance the aerial combo using data.getComboStep() / data.advanceCombo(resetTicks).
    }

    private static boolean hasSamuraiWeapon(Player player) {
        // TODO: replace with your real check — katana item tag + active class from the rune system.
        return true;
    }

    /** Server-side decay for cooldowns / stamina / combo. */
    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SamuraiData data = ModAttachments.get(player);

            if (data.isFloating()) {
                int elapsed = FLOAT_TICKS - data.getJuggleRemaining();
                if (elapsed >= RISE_TICKS) {
                    // Trava no ar: sem gravidade + velocity zerada = congelado.
                    player.setNoGravity(true);
                    player.setDeltaMovement(0, 0, 0);
                    player.fallDistance = 0;
                    player.connection.send(new ClientboundSetEntityMotionPacket(player));
                    data.setGravityFrozen(true);
                }
            } else if (data.isGravityFrozen()) {
                player.setNoGravity(false); // acabou o hang time, gravidade volta
                data.setGravityFrozen(false);
            }

            data.tick();
        }
    }
    @SubscribeEvent
    public static void onServerTick(final net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        if (SUSPENDED.isEmpty()) return;

        Iterator<Map.Entry<UUID, Integer>> it = SUSPENDED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            int remaining = entry.getValue();

            LivingEntity target = null;
            for (net.minecraft.server.level.ServerLevel level : event.getServer().getAllLevels()) {
                if (level.getEntity(entry.getKey()) instanceof LivingEntity le) { target = le; break; }
            }

            // Fim da janela ou alvo sumiu: restaura gravidade e remove.
            if (remaining <= 0 || target == null || !target.isAlive()) {
                if (target != null) target.setNoGravity(false);
                it.remove();
                continue;
            }

            int elapsed = FLOAT_TICKS - remaining;
            if (elapsed >= RISE_TICKS) {
                target.setNoGravity(true);
                target.setDeltaMovement(0, 0, 0);
                target.fallDistance = 0;
                if (target instanceof ServerPlayer sp) sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
                else target.hurtMarked = true;
            }

            entry.setValue(remaining - 1);
        }
    }
}
