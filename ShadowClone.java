package com.combatgods.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/** Clon de sombra: usa la skin de su dueno y ataca a todo menos a su dueno y a sus otros clones. */
public class ShadowClone extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(ShadowClone.class, EntityDataSerializers.OPTIONAL_UUID);

    public ShadowClone(EntityType<? extends ShadowClone> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        // Prioridad a los jugadores; despues cualquier otro ser vivo
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, this::canTarget));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, true, false, this::canTarget));
    }

    private boolean canTarget(LivingEntity e) {
        if (e == this || !e.isAlive() || e instanceof ArmorStand || e.isSpectator()) return false;
        UUID owner = getOwnerId();
        if (owner != null && owner.equals(e.getUUID())) return false;
        if (e instanceof ShadowClone other && owner != null && owner.equals(other.getOwnerId())) return false;
        return true;
    }

    public UUID getOwnerId() { return this.entityData.get(OWNER).orElse(null); }
    public void setOwner(UUID id) { this.entityData.set(OWNER, Optional.ofNullable(id)); }

    /** Vida extra (en puntos: 1 corazon = 2 puntos). */
    public void boostHealth(float extraPoints) {
        double max = this.getAttribute(Attributes.MAX_HEALTH).getBaseValue() + extraPoints;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(max);
        this.setHealth((float) max);
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID owner = getOwnerId();
        if (owner != null) tag.putUUID("CloneOwner", owner);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("CloneOwner")) setOwner(tag.getUUID("CloneOwner"));
    }
}
