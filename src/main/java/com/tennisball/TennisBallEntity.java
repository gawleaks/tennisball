package com.tennisball;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class TennisBallEntity extends ThrownItemEntity {
    private int lastBounceSoundTick = -3;

    public TennisBallEntity(EntityType<? extends TennisBallEntity> entityType, World world) {
        super(entityType, world);
    }

    public TennisBallEntity(World world, LivingEntity owner, ItemStack stack) {
        super(TennisBallBridge.entityType(), owner, world, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TennisBallBridge.tennisBallItem();
    }

    @Override
    protected double getGravity() {
        return TennisBallBridge.entityGravity(this);
    }

    @Override
    public void tick() {
        super.tick();
        TennisBallBridge.entityTick(this);
    }

    @Override
    protected void onBlockHit(BlockHitResult hitResult) {
        super.onBlockHit(hitResult);
        TennisBallBridge.entityOnBlockHit(this, hitResult);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        TennisBallBridge.entityOnEntityHit(this, entityHitResult);
    }

    public int tennisballGetAge() {
        return this.age;
    }

    public int tennisballGetLastBounceSoundTick() {
        return this.lastBounceSoundTick;
    }

    public void tennisballSetLastBounceSoundTick(int tick) {
        this.lastBounceSoundTick = tick;
    }
}
