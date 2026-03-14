package com.tennisball;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class TennisBallBridge {
    private static final double BOUNCE_DAMPING = 0.78;
    private static final double FLOOR_FRICTION = 0.92;
    private static final double MIN_SPEED_TO_BOUNCE = 0.08;
    private static final int MAX_LIFETIME_TICKS = 20 * 30;
    private static final int BOUNCE_SOUND_COOLDOWN_TICKS = 3;
    private static final float EXPLOSIVE_POWER = 2.0f;

    private TennisBallBridge() {
    }

    public static void registerGameplay() {
        TennisBallRegistry.register();
    }

    public static EntityType<TennisBallEntity> entityType() {
        return TennisBallRegistry.TENNIS_BALL_ENTITY_TYPE;
    }

    public static Item tennisBallItem() {
        return TennisBallRegistry.TENNIS_BALL_ITEM;
    }

    public static ActionResult itemUse(TennisBallItem item, World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        world.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW,
                SoundCategory.NEUTRAL,
                0.5f,
                0.8f + world.getRandom().nextFloat() * 0.2f
        );

        if (world instanceof ServerWorld serverWorld) {
            TennisBallEntity ballEntity = new TennisBallEntity(
                    serverWorld,
                    user,
                    stack.copyWithCount(1),
                    item.isExplosive()
            );
            ballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.25f, 0.3f);
            serverWorld.spawnEntity(ballEntity);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }

            user.incrementStat(Stats.USED.getOrCreateStat(item));
        }

        return ActionResult.CONSUME;
    }

    public static double entityGravity(TennisBallEntity entity) {
        return 0.035;
    }

    public static void entityTick(TennisBallEntity entity) {
        if (entity.tennisballGetAge() > MAX_LIFETIME_TICKS) {
            entity.discard();
            return;
        }

        if (entity.isOnGround()) {
            Vec3d velocity = entity.getVelocity();
            Vec3d slowed = new Vec3d(velocity.x * FLOOR_FRICTION, velocity.y, velocity.z * FLOOR_FRICTION);

            if (Math.abs(slowed.y) < 0.03) {
                slowed = new Vec3d(slowed.x, 0.0, slowed.z);
            }

            entity.setVelocity(slowed);

            if (slowed.horizontalLengthSquared() < 0.0009 && Math.abs(slowed.y) < 0.01) {
                entity.discard();
            }
        }
    }

    public static void entityOnBlockHit(TennisBallEntity entity, BlockHitResult hitResult) {
        if (entity.tennisballIsExplosive()) {
            explodeAndDiscard(entity);
            return;
        }

        Vec3d velocity = entity.getVelocity();
        Direction side = hitResult.getSide();

        if (side == Direction.UP && Math.abs(velocity.y) < 0.08) {
            entity.setVelocity(velocity.x * 0.88, 0.0, velocity.z * 0.88);
            return;
        }

        Vec3d normal = Vec3d.of(side.getVector());
        double dot = velocity.dotProduct(normal);
        Vec3d reflected = velocity.subtract(normal.multiply(2.0 * dot)).multiply(BOUNCE_DAMPING);

        if (side == Direction.UP && reflected.y > 0.0) {
            reflected = new Vec3d(reflected.x * 0.95, reflected.y * 0.85, reflected.z * 0.95);
        }

        if (reflected.lengthSquared() < MIN_SPEED_TO_BOUNCE * MIN_SPEED_TO_BOUNCE) {
            if (side == Direction.UP) {
                entity.setVelocity(reflected.x * 0.5, 0.0, reflected.z * 0.5);
            } else {
                entity.discard();
                return;
            }
        } else {
            entity.setVelocity(reflected);
        }

        entity.setPosition(entity.getPos().add(normal.multiply(0.01)));

        if (!entity.isSilent()
                && entity.tennisballGetAge() - entity.tennisballGetLastBounceSoundTick() >= BOUNCE_SOUND_COOLDOWN_TICKS
                && reflected.lengthSquared() > 0.02 * 0.02) {
            entity.getWorld().playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvents.BLOCK_SLIME_BLOCK_HIT,
                    SoundCategory.NEUTRAL,
                    0.25f,
                    1.5f + (entity.getRandom().nextFloat() - 0.5f) * 0.2f
            );
            entity.tennisballSetLastBounceSoundTick(entity.tennisballGetAge());
        }
    }

    public static void entityOnEntityHit(TennisBallEntity entity, EntityHitResult hitResult) {
        if (entity.tennisballIsExplosive()) {
            explodeAndDiscard(entity);
            return;
        }

        Entity target = hitResult.getEntity();
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            DamageSource damageSource = entity.getDamageSources().thrown(entity, entity.getOwner());
            target.damage(serverWorld, damageSource, 1.0f);
        }

        Vec3d bounced = entity.getVelocity().multiply(-0.55, 0.45, -0.55);
        entity.setVelocity(bounced.x, Math.max(0.08, Math.abs(bounced.y)), bounced.z);
    }

    private static void explodeAndDiscard(TennisBallEntity entity) {
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.createExplosion(
                    entity,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    EXPLOSIVE_POWER,
                    World.ExplosionSourceType.TNT
            );
        }
        entity.discard();
    }
}
