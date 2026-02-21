package com.tennisball;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public final class TennisBallBridge {
    private static final IFn REQUIRE = Clojure.var("clojure.core", "require");

    private static final IFn ITEM_USE;
    private static final IFn ENTITY_GRAVITY;
    private static final IFn ENTITY_TICK;
    private static final IFn ENTITY_ON_BLOCK_HIT;
    private static final IFn ENTITY_ON_ENTITY_HIT;

    static {
        REQUIRE.invoke(Clojure.read("tennisball.gameplay"));

        ITEM_USE = Clojure.var("tennisball.gameplay", "item-use");
        ENTITY_GRAVITY = Clojure.var("tennisball.gameplay", "entity-gravity");
        ENTITY_TICK = Clojure.var("tennisball.gameplay", "entity-tick");
        ENTITY_ON_BLOCK_HIT = Clojure.var("tennisball.gameplay", "entity-on-block-hit");
        ENTITY_ON_ENTITY_HIT = Clojure.var("tennisball.gameplay", "entity-on-entity-hit");
    }

    private TennisBallBridge() {
    }

    public static void registerGameplay() {
        TennisBallRegistry.register();
    }

    public static void registerClientRenderer() {
        EntityRendererRegistry.register(TennisBallRegistry.TENNIS_BALL_ENTITY_TYPE, FlyingItemEntityRenderer::new);
    }

    public static EntityType<TennisBallEntity> entityType() {
        return TennisBallRegistry.TENNIS_BALL_ENTITY_TYPE;
    }

    public static Item tennisBallItem() {
        return TennisBallRegistry.TENNIS_BALL_ITEM;
    }

    public static ActionResult itemUse(TennisBallItem item, World world, PlayerEntity user, Hand hand) {
        return (ActionResult) ITEM_USE.invoke(item, world, user, hand);
    }

    public static double entityGravity(TennisBallEntity entity) {
        return ((Number) ENTITY_GRAVITY.invoke(entity)).doubleValue();
    }

    public static void entityTick(TennisBallEntity entity) {
        ENTITY_TICK.invoke(entity);
    }

    public static void entityOnBlockHit(TennisBallEntity entity, BlockHitResult hitResult) {
        ENTITY_ON_BLOCK_HIT.invoke(entity, hitResult);
    }

    public static void entityOnEntityHit(TennisBallEntity entity, EntityHitResult hitResult) {
        ENTITY_ON_ENTITY_HIT.invoke(entity, hitResult);
    }
}
