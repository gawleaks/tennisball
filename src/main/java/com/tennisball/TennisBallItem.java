package com.tennisball;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class TennisBallItem extends Item {
    private final boolean explosive;

    public TennisBallItem(Settings settings) {
        this(settings, false);
    }

    public TennisBallItem(Settings settings, boolean explosive) {
        super(settings);
        this.explosive = explosive;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        return TennisBallBridge.itemUse(this, world, user, hand);
    }

    public boolean isExplosive() {
        return explosive;
    }
}
