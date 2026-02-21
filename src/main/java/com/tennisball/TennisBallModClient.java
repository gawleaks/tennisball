package com.tennisball;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class TennisBallModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Call Clojure client initialization
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("tennisball.client"));
        
        IFn initClient = Clojure.var("tennisball.client", "init-client");
        initClient.invoke();

        EntityRendererRegistry.register(
                TennisBallRegistry.TENNIS_BALL_ENTITY_TYPE,
                FlyingItemEntityRenderer::new
        );
    }
}
