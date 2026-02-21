package com.tennisball;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class TennisBallRegistry {
    public static final String MOD_ID = "tennisball";

    public static EntityType<TennisBallEntity> TENNIS_BALL_ENTITY_TYPE;
    public static Item TENNIS_BALL_ITEM;

    private static boolean registered = false;

    private TennisBallRegistry() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        Identifier entityId = Identifier.of(MOD_ID, "tennis_ball");
        RegistryKey<EntityType<?>> entityKey = RegistryKey.of(RegistryKeys.ENTITY_TYPE, entityId);
        TENNIS_BALL_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                entityId,
                FabricEntityTypeBuilder.<TennisBallEntity>create(SpawnGroup.MISC, TennisBallEntity::new)
                        .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                        .trackRangeBlocks(4)
                        .trackedUpdateRate(10)
                        .build(entityKey)
        );

        Identifier itemId = Identifier.of(MOD_ID, "tennis_ball");
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, itemId);
        Item.Settings settings = new Item.Settings()
                .registryKey(itemKey)
                .maxCount(16);

        TENNIS_BALL_ITEM = Registry.register(
                Registries.ITEM,
                itemId,
                new TennisBallItem(settings)
        );

        registered = true;
    }
}
