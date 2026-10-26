package com.combatgods.skill;

import com.combatgods.character.CharacterData;
import com.combatgods.character.CombatCharacter;
import com.combatgods.config.CombatConfig;
import com.combatgods.data.PlayerData;
import com.combatgods.entity.ModEntities;
import com.combatgods.entity.ShadowCloneEntity;
import com.combatgods.item.ModItems;
import com.combatgods.network.ModNetwork;
import com.combatgods.network.PlayerStatePacket;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Lógica del servidor de todas las habilidades de Jun. */
public final class JunSkills {
    private static final UUID RUN_SPEED_ID = UUID.fromString("5b0d3a64-7c1e-4f7a-9d0a-2c9d6a1e0a11");
    private static final Map<UUID, Long> RUNS = new HashMap<>();    // jugador -> tick en que termina la carrera de katana
    private static final Map<UUID, Long> DASHES = new HashMap<>();  // jugador -> tick en que termina la embestida (ultimate 3)

    private JunSkills() {}

    public static boolean isJun(ServerPlayer p) {
        return CharacterData.get(p) == CombatCharacter.JUN;
    }

    // =====================================================================
    //  ENTRADA: el jugador pulsó la tecla de una habilidad
    // =====================================================================
    public static void use(ServerPlayer p, Skill s) {
        if (!isJun(p)) { msg(p, "message.combatgods.not_jun"); return; }
        if (s == Skill.ULTIMATE) { activateUltimate(p); return; }

        long now = p.level().getGameTime();

        // segundo toque de la katana: dispara el ataque sin mirar el cooldown (ya empezó al activarla)
        if (s == Skill.WEAPON && RUNS.containsKey(p.getUUID())) { finishRun(p); return; }

        long ready = PlayerData.readyAt(p, s);
        if (now < ready) {
            msg(p, "message.combatgods.cooldown", String.format("%.1f", (ready - now) / 20.0));
            return;
        }

        boolean ult = PlayerData.ultActive(p);
        boolean ok = switch (s) {
            case NORMAL_1, NORMAL_2, NORMAL_3 -> normal(p, s);
            case CLONES -> clones(p, ult);
            case WEAPON -> weapon(p);
            case ULT_1 -> ult ? ult1(p) : needUltimate(p);
            case ULT_2 -> ult ? ult2(p) : needUltimate(p);
            case ULT_3 -> ult ? ult3(p) : needUltimate(p);
            case COUNTER -> ult ? counter(p) : needUltimate(p);
            default -> false;
        };

        if (ok) {
            PlayerData.setReadyAt(p, s, now + CombatConfig.cooldownTicks(s));
            if (!ult) PlayerData.addBar(p, CombatConfig.BAR_PER_SKILL.get());
        }
    }

    // =====================================================================
    //  HABILIDADES NORMALES (mano vacía + objetivo de frente a corta distancia)
    // =====================================================================
    private static boolean normal(ServerPlayer p, Skill s) {
        if (!p.getMainHandItem().isEmpty()) { msg(p, "message.combatgods.need_empty_hand"); return false; }
        LivingEntity t = Targeting.inFront(p, CombatConfig.CLOSE_RANGE.get(), 0.3, false);
        if (t == null) { msg(p, "message.combatgods.no_target"); return false; }

        ServerLevel lv = (ServerLevel) p.level();
        Vec3 away = horizontal(t.position().subtract(p.position()));

        switch (s) {
            case NORMAL_1 -> {
                // ráfaga de patadas y remate que lanza al rival hacia arriba
                double total = CombatConfig.NORMAL_1_DAMAGE.get();
                for (int i = 0; i < 3; i++) {
                    final int k = i;
                    ServerScheduler.later(i * 4, () -> {
                        hit(p, t, total * 0.25);
                        particles(lv, ParticleTypes.SWEEP_ATTACK, t.position().add(0, 1.0, 0), 2, 0.3, 0);
                        sound(lv, t.position(), SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F + 0.1F * k);
                    });
                }
                ServerScheduler.later(12, () -> {
                    hit(p, t, total * 0.25);
                    push(t, away, 0.2, 1.3);
                    particles(lv, ParticleTypes.CRIT, t.position().add(0, 1.0, 0), 25, 0.4, 0.3);
                    sound(lv, t.position(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.2F, 0.9F);
                });
            }
            case NORMAL_2 -> {
                // golpe fuerte con destello y estallido de polvo
                hit(p, t, CombatConfig.NORMAL_2_DAMAGE.get());
                push(t, away, 1.6, 0.4);
                particles(lv, ParticleTypes.FLASH, t.position().add(0, 1.2, 0), 1, 0, 0);
                particles(lv, ParticleTypes.EXPLOSION, t.position().add(0, 1.0, 0), 3, 0.4, 0);
                particles(lv, ParticleTypes.CLOUD, t.position(), 30, 0.8, 0.1);
                sound(lv, t.position(), SoundEvents.GENERIC_EXPLODE, 0.8F, 1.2F);
            }
            case NORMAL_3 -> {
                // salto, pisotón y patada final que manda al rival lejos
                double total = CombatConfig.NORMAL_3_DAMAGE.get();
                p.setDeltaMovement(p.getDeltaMovement().add(0, 0.6, 0));
                p.hurtMarked = true;
                ServerScheduler.later(8, () -> {
                    hit(p, t, total * 0.4);
                    t.setDeltaMovement(0, -0.8, 0);
                    t.hurtMarked = true;
                    particles(lv, ParticleTypes.CLOUD, t.position(), 25, 0.6, 0.05);
                    sound(lv, t.position(), SoundEvents.ANVIL_LAND, 0.6F, 0.7F);
                });
                ServerScheduler.later(20, () -> {
                    hit(p, t, total * 0.6);
                    push(t, away, 2.4, 0.5);
                    particles(lv, ParticleTypes.CRIT, t.position().add(0, 1.0, 0), 30, 0.4, 0.4);
                    sound(lv, t.position(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.4F, 0.7F);
                });
            }
            default -> { return false; }
        }
        return true;
    }

    // =====================================================================
    //  CLONES DE SOMBRA (a cualquier distancia, sin objetivo)
    // =====================================================================
    private static boolean clones(ServerPlayer p, boolean ult) {
        ServerLevel lv = (ServerLevel) p.level();
        int count = ult ? CombatConfig.CLONES_ULTIMATE.get() : CombatConfig.CLONES_NORMAL.get();
        double extra = ult ? CombatConfig.CLONE_EXTRA_HEALTH_ULTIMATE.get() : 0.0;

        for (int i = 0; i < count; i++) {
            ShadowCloneEntity c = ModEntities.SHADOW_CLONE.get().create(lv);
            if (c == null) continue;
            double ang = (Math.PI * 2.0 / count) * i;
            double x = p.getX() + Math.cos(ang) * 2.5;
            double z = p.getZ() + Math.sin(ang) * 2.5;
            c.moveTo(x, p.getY(), z, p.getYRot(), 0.0F);
            c.setOwnerUUID(p.getUUID());
            c.getAttribute(Attributes.MAX_HEALTH).setBaseValue(CombatConfig.CLONE_HEALTH.get() + extra);
            c.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(CombatConfig.CLONE_DAMAGE.get());
            c.setHealth(c.getMaxHealth());
            lv.addFreshEntity(c);
            particles(lv, ParticleTypes.CLOUD, new Vec3(x, p.getY() + 1.0, z), 12, 0.3, 0.02);
        }
        sound(lv, p.position(), SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.2F, 1.0F);
        return true;
    }

    // =====================================================================
    //  HABILIDAD DEL ARMA EN LA MANO (V)
    // =====================================================================
    private static boolean weapon(ServerPlayer p) {
        if (p.getMainHandItem().is(ModItems.KATANA_JUN.get())) return startRun(p);
        if (p.getMainHandItem().is(ModItems.DAGA_JUN.get())) return dagger(p);
        msg(p, "message.combatgods.need_weapon");
        return false;
    }

    private static boolean dagger(ServerPlayer p) {
        LivingEntity t = Targeting.inFront(p, CombatConfig.CLOSE_RANGE.get(), 0.3, false);
        if (t == null) { msg(p, "message.combatgods.no_target"); return false; }
        ServerLevel lv = (ServerLevel) p.level();
        double each = CombatConfig.DAGGER_DAMAGE.get() / 6.0;
        for (int i = 0; i < 6; i++) {
            ServerScheduler.later(i * 3, () -> {
                if (!t.isAlive() || p.distanceToSqr(t) > 16.0) return;
                hit(p, t, each);
                particles(lv, ParticleTypes.SWEEP_ATTACK, t.position().add(0, 1.0, 0), 1, 0.2, 0);
                sound(lv, t.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.3F);
            });
        }
        ServerScheduler.later(20, () -> {
            if (!t.isAlive()) return;
            push(t, horizontal(t.position().subtract(p.position())), 1.2, 0.3);
            particles(lv, ParticleTypes.FLASH, t.position().add(0, 1.2, 0), 1, 0, 0);
            sound(lv, t.position(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.2F, 0.9F);
        });
        return true;
    }

    // ---------------------------- KATANA: carrera ----------------------------
    private static boolean startRun(ServerPlayer p) {
        long end = p.level().getGameTime() + CombatConfig.KATANA_RUN_SECONDS.get() * 20L;
        RUNS.put(p.getUUID(), end);
        setRunSpeed(p, CombatConfig.KATANA_SPEED_BONUS.get());
        sound(p.level(), p.position(), SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 1.2F);
        msg(p, "message.combatgods.katana_run");
        return true;
    }

    /** Segundo toque o fin de los 10 s: ataca si hay objetivo, si no frena suave. */
    private static void finishRun(ServerPlayer p) {
        if (RUNS.remove(p.getUUID()) == null) return;
        ServerLevel lv = (ServerLevel) p.level();
        LivingEntity t = Targeting.inFront(p, CombatConfig.KATANA_TARGET_RANGE.get(), 0.5, true);
        if (t == null) { smoothStop(p); return; }

        setRunSpeed(p, 0.0);

        // 1) atraviesa al objetivo con una estela
        Vec3 from = p.position();
        Vec3 dir = horizontal(t.position().subtract(from));
        double dist = t.position().subtract(from).multiply(1, 0, 1).length();
        Vec3 dest = new Vec3(t.getX() + dir.x * 2.5, t.getY(), t.getZ() + dir.z * 2.5);
        Vec3 delta = dest.subtract(from);
        if (!lv.noCollision(p, p.getBoundingBox().move(delta))) {
            double stop = Math.max(0.0, dist - 1.2);
            dest = new Vec3(from.x + dir.x * stop, from.y, from.z + dir.z * stop);
        }
        for (int i = 0; i <= 20; i++) {
            Vec3 pt = from.lerp(dest, i / 20.0);
            particles(lv, ParticleTypes.CLOUD, pt.add(0, 1.0, 0), 1, 0.05, 0);
            particles(lv, ParticleTypes.SWEEP_ATTACK, pt.add(0, 1.0, 0), 1, 0.0, 0);
        }
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
        p.connection.teleport(dest.x, dest.y, dest.z, yaw, p.getXRot());
        sound(lv, dest, SoundEvents.PLAYER_ATTACK_SWEEP, 1.4F, 0.8F);
        hit(p, t, CombatConfig.KATANA_CUT_DAMAGE.get());

        // 2) vuelve a envainar
        ServerScheduler.later(10, () -> sound(lv, p.position(), SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.8F));

        // 3) serie de cortes sobre el objetivo
        int cuts = CombatConfig.KATANA_CUTS.get();
        for (int i = 0; i < cuts; i++) {
            final int idx = i;
            ServerScheduler.later(18 + i * 4, () -> {
                if (!t.isAlive()) return;
                if (CutHooks.shouldDodge(t, idx)) { // Shinji con ultimate podrá esquivar saltando (se completa con su fase)
                    particles(lv, ParticleTypes.CLOUD, t.position().add(0, 1.0, 0), 8, 0.4, 0.05);
                    return;
                }
                hit(p, t, CombatConfig.KATANA_CUT_DAMAGE.get());
                particles(lv, ParticleTypes.SWEEP_ATTACK, t.position().add(0, 1.0, 0), 1, 0.3, 0);
                particles(lv, ParticleTypes.CRIT, t.position().add(0, 1.0, 0), 10, 0.3, 0.3);
                sound(lv, t.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.4F + 0.05F * idx);
            });
        }
    }

    private static void smoothStop(ServerPlayer p) {
        double bonus = CombatConfig.KATANA_SPEED_BONUS.get();
        setRunSpeed(p, bonus * 0.66);
        ServerScheduler.later(5, () -> setRunSpeed(p, bonus * 0.33));
        ServerScheduler.later(10, () -> setRunSpeed(p, 0.0));
    }

    private static void setRunSpeed(ServerPlayer p, double bonus) {
        AttributeInstance inst = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (inst == null) return;
        inst.removeModifier(RUN_SPEED_ID);
        if (bonus > 0.0) {
            inst.addTransientModifier(new AttributeModifier(RUN_SPEED_ID, "combatgods_katana_run", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    // =====================================================================
    //  HABILIDADES DE ULTIMATE
    // =====================================================================
    private static boolean needUltimate(ServerPlayer p) {
        msg(p, "message.combatgods.need_ultimate");
        return false;
    }

    /** Ultimate 1: agarre y tajos en X con remate (cerca de un jugador o mob). */
    private static boolean ult1(ServerPlayer p) {
        LivingEntity t = Targeting.nearest(p, 3.0);
        if (t == null) { msg(p, "message.combatgods.no_target"); return false; }
        ServerLevel lv = (ServerLevel) p.level();
        double total = CombatConfig.ULT_1_DAMAGE.get();
        Vec3 away = horizontal(t.position().subtract(p.position()));

        ServerScheduler.later(0, () -> {
            hit(p, t, total * 0.2);
            t.setDeltaMovement(0, 0, 0); t.hurtMarked = true;
            sound(lv, t.position(), SoundEvents.PLAYER_ATTACK_CRIT, 1.2F, 0.6F);
        });
        for (int i = 0; i < 2; i++) {
            ServerScheduler.later(10 + i * 8, () -> {
                hit(p, t, total * 0.2);
                particles(lv, ParticleTypes.SWEEP_ATTACK, t.position().add(0, 1.0, 0), 3, 0.5, 0);
                sound(lv, t.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 0.9F);
            });
        }
        ServerScheduler.later(34, () -> {
            hit(p, t, total * 0.4);
            push(t, away, 2.2, 0.5);
            particles(lv, ParticleTypes.EXPLOSION, t.position().add(0, 1.0, 0), 4, 0.5, 0);
            sound(lv, t.position(), SoundEvents.GENERIC_EXPLODE, 1.0F, 1.0F);
        });
        return true;
    }

    /** Ultimate 2: katana, dagas y puñetazo final (objetivo de frente). */
    private static boolean ult2(ServerPlayer p) {
        LivingEntity t = Targeting.inFront(p, 8.0, 0.5, true);
        if (t == null) { msg(p, "message.combatgods.no_target"); return false; }
        ServerLevel lv = (ServerLevel) p.level();
        double total = CombatConfig.ULT_2_DAMAGE.get();
        Vec3 away = horizontal(t.position().subtract(p.position()));

        for (int i = 0; i < 3; i++) {
            ServerScheduler.later(i * 6, () -> {
                if (!t.isAlive()) return;
                Vec3 a = p.getEyePosition(), b = t.position().add(0, 1.0, 0);
                for (int k = 0; k <= 10; k++) {
                    particles(lv, ParticleTypes.CRIT, a.lerp(b, k / 10.0), 1, 0.05, 0);
                }
                hit(p, t, total * 0.1);
                sound(lv, t.position(), SoundEvents.TRIDENT_THROW, 1.0F, 1.4F);
            });
        }
        ServerScheduler.later(26, () -> {
            if (!t.isAlive()) return;
            hit(p, t, total * 0.2);
            particles(lv, ParticleTypes.SWEEP_ATTACK, t.position().add(0, 1.0, 0), 3, 0.5, 0);
            sound(lv, t.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.4F, 0.8F);
        });
        // "una pulgada": puñetazo final con explosión de polvo
        ServerScheduler.later(50, () -> {
            if (!t.isAlive()) return;
            hit(p, t, total * 0.5);
            push(t, away, 3.0, 0.7);
            particles(lv, ParticleTypes.EXPLOSION_EMITTER, t.position().add(0, 1.0, 0), 1, 0, 0);
            particles(lv, ParticleTypes.CLOUD, t.position(), 60, 1.2, 0.2);
            sound(lv, t.position(), SoundEvents.GENERIC_EXPLODE, 1.5F, 0.7F);
        });
        return true;
    }

    /** Ultimate 3: carrera a gran velocidad; si choca de frente con alguien, lo mata de un golpe con explosión. */
    private static boolean ult3(ServerPlayer p) {
        DASHES.put(p.getUUID(), p.level().getGameTime() + 40L);
        sound(p.level(), p.position(), SoundEvents.ENDER_DRAGON_FLAP, 1.2F, 1.4F);
        return true;
    }

    private static void onePunch(ServerPlayer p, LivingEntity t) {
        ServerLevel lv = (ServerLevel) p.level();
        Vec3 pos = t.position();
        Vec3 away = horizontal(pos.subtract(p.position()));
        t.invulnerableTime = 0;
        t.hurt(p.damageSources().playerAttack(p), t.getMaxHealth() * 100.0F + 1000.0F);
        p.setDeltaMovement(0, 0, 0);
        p.hurtMarked = true;

        particles(lv, ParticleTypes.FLASH, pos.add(0, 1.2, 0), 1, 0, 0);
        particles(lv, ParticleTypes.EXPLOSION_EMITTER, pos.add(0, 1.0, 0), 1, 0, 0);
        sound(lv, pos, SoundEvents.GENERIC_EXPLODE, 2.0F, 0.6F);

        // la explosión daña a los de alrededor, pero NO a quien dio el golpe
        double r = CombatConfig.ULT_3_EXPLOSION_RADIUS.get();
        AABB box = new AABB(pos, pos).inflate(r);
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, box,
                x -> x != p && x != t && x.isAlive() && !Targeting.isOwnClone(p, x))) {
            e.invulnerableTime = 0;
            e.hurt(lv.damageSources().explosion(p, p), CombatConfig.ULT_3_EXPLOSION_DAMAGE.get().floatValue());
            push(e, horizontal(e.position().subtract(pos)), 1.4, 0.5);
        }
    }

    /** Contraataque: quita toda la vida al que te golpea (solo con la ultimate). */
    private static boolean counter(ServerPlayer p) {
        long window = Math.round(CombatConfig.COUNTER_WINDOW_SECONDS.get() * 20.0);
        PlayerData.setCounterUntil(p, p.level().getGameTime() + window);
        sound(p.level(), p.position(), SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.6F);
        msg(p, "message.combatgods.counter_ready");
        return true;
    }

    /** Lo llama el evento de ataque cuando el contraataque estaba listo. */
    public static void counterKill(ServerPlayer p, LivingEntity attacker) {
        ServerLevel lv = (ServerLevel) p.level();
        Vec3 pos = attacker.position();
        particles(lv, ParticleTypes.FLASH, pos.add(0, 1.2, 0), 1, 0, 0);
        particles(lv, ParticleTypes.SWEEP_ATTACK, pos.add(0, 1.0, 0), 4, 0.5, 0);
        sound(lv, pos, SoundEvents.PLAYER_ATTACK_CRIT, 1.5F, 0.5F);
        attacker.invulnerableTime = 0;
        attacker.hurt(lv.damageSources().genericKill(), Float.MAX_VALUE);
    }

    // =====================================================================
    //  ULTIMATE: activar / terminar
    // =====================================================================
    private static void activateUltimate(ServerPlayer p) {
        if (PlayerData.ultActive(p)) { msg(p, "message.combatgods.ult_already"); return; }
        double bar = PlayerData.bar(p);
        if (bar < PlayerData.BAR_MAX - 0.001) {
            msg(p, "message.combatgods.bar", String.format("%.0f", bar));
            return;
        }
        long now = p.level().getGameTime();
        PlayerData.setUltEnd(p, now + CombatConfig.ULTIMATE_SECONDS.get() * 20L);
        PlayerData.setBar(p, 0);
        swapItems(p, ModItems.DAGA_JUN.get(), ModItems.KATANA_JUN.get()); // la daga se va, aparece la katana

        ServerLevel lv = (ServerLevel) p.level();
        particles(lv, ParticleTypes.FLAME, p.position().add(0, 1.0, 0), 60, 0.6, 0.1);
        particles(lv, ParticleTypes.EXPLOSION, p.position().add(0, 1.0, 0), 3, 0.5, 0);
        sound(lv, p.position(), SoundEvents.ENDER_DRAGON_GROWL, 1.5F, 0.9F);
        p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 30, 10));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("message.combatgods.ult_title")));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("message.combatgods.ult_subtitle")));
    }

    public static void endUltimate(ServerPlayer p) {
        PlayerData.setUltEnd(p, 0);
        cleanup(p);
        swapItems(p, ModItems.KATANA_JUN.get(), ModItems.DAGA_JUN.get()); // vuelve la daga
        msg(p, "message.combatgods.ult_end");
    }

    // =====================================================================
    //  TICK, limpieza y utilidades
    // =====================================================================
    public static void tick(ServerPlayer p) {
        long now = p.level().getGameTime();
        UUID id = p.getUUID();

        Long runEnd = RUNS.get(id);
        if (runEnd != null && now >= runEnd) finishRun(p); // se acabaron los 10 s = igual que el segundo toque

        Long dashEnd = DASHES.get(id);
        if (dashEnd != null) {
            if (now >= dashEnd) {
                DASHES.remove(id);
            } else {
                Vec3 look = p.getLookAngle();
                p.setDeltaMovement(look.x * 1.5, p.getDeltaMovement().y, look.z * 1.5);
                p.hurtMarked = true;
                LivingEntity t = Targeting.inFront(p, 1.2, 0.6, false);
                if (t != null) { DASHES.remove(id); onePunch(p, t); }
            }
        }

        long ultEnd = PlayerData.ultEnd(p);
        if (ultEnd > 0 && now >= ultEnd) endUltimate(p);
    }

    /** Quita estados temporales (carrera, embestida, velocidad). */
    public static void cleanup(ServerPlayer p) {
        RUNS.remove(p.getUUID());
        DASHES.remove(p.getUUID());
        setRunSpeed(p, 0.0);
    }

    public static void onDeath(ServerPlayer p) {
        cleanup(p);
        PlayerData.setUltEnd(p, 0);
        PlayerData.setBar(p, 0);
        PlayerData.setCounterUntil(p, 0);
    }

    /** Jun siempre tiene su daga mientras la ultimate no esté activa. */
    public static void ensureWeapon(ServerPlayer p) {
        if (!isJun(p)) return;
        Item wanted = PlayerData.ultActive(p) ? ModItems.KATANA_JUN.get() : ModItems.DAGA_JUN.get();
        Item other = PlayerData.ultActive(p) ? ModItems.DAGA_JUN.get() : ModItems.KATANA_JUN.get();
        Inventory inv = p.getInventory();
        boolean has = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(wanted)) has = true;
        }
        if (!has) swapItems(p, other, wanted);
    }

    private static void swapItems(ServerPlayer p, Item from, Item to) {
        Inventory inv = p.getInventory();
        int slot = -1;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(from)) {
                if (slot < 0) slot = i; else inv.setItem(i, ItemStack.EMPTY);
            }
        }
        ItemStack stack = new ItemStack(to);
        if (slot >= 0) inv.setItem(slot, stack);
        else if (!inv.add(stack)) p.drop(stack, false);
    }

    public static void sync(ServerPlayer p) {
        CombatCharacter c = CharacterData.get(p);
        long now = p.level().getGameTime();
        Skill[] all = Skill.values();
        int[] cd = new int[all.length];
        for (Skill s : all) cd[s.ordinal()] = (int) Math.max(0L, PlayerData.readyAt(p, s) - now);
        long ultEnd = PlayerData.ultEnd(p);
        int ultLeft = ultEnd > 0 ? (int) Math.max(0L, ultEnd - now) : 0;
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p),
                new PlayerStatePacket(c == null ? "" : c.id(), (float) PlayerData.bar(p), ultLeft, cd));
    }

    // ---------------------------- ayudas ----------------------------
    private static void hit(ServerPlayer p, LivingEntity t, double dmg) {
        if (!t.isAlive()) return;
        t.invulnerableTime = 0;
        t.hurt(p.damageSources().playerAttack(p), (float) dmg);
    }

    private static void push(LivingEntity t, Vec3 dir, double strength, double up) {
        Vec3 d = dir.lengthSqr() < 1.0E-6 ? Vec3.ZERO : dir.normalize();
        t.setDeltaMovement(d.x * strength, up, d.z * strength);
        t.hurtMarked = true;
    }

    private static Vec3 horizontal(Vec3 v) {
        return new Vec3(v.x, 0.0, v.z).normalize();
    }

    private static void particles(ServerLevel lv, ParticleOptions type, Vec3 pos, int count, double spread, double speed) {
        lv.sendParticles(type, pos.x, pos.y, pos.z, count, spread, spread, spread, speed);
    }

    private static void sound(net.minecraft.world.level.Level lv, Vec3 pos, SoundEvent ev, float vol, float pitch) {
        lv.playSound(null, pos.x, pos.y, pos.z, ev, SoundSource.PLAYERS, vol, pitch);
    }

    private static void msg(ServerPlayer p, String key, Object... args) {
        p.displayClientMessage(Component.translatable(key, args), true);
    }
}
