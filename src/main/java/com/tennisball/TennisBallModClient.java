package com.tennisball;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import net.fabricmc.api.ClientModInitializer;

public class TennisBallModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Call Clojure client initialization (handles renderer registration)
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("tennisball.client"));
        
        IFn initClient = Clojure.var("tennisball.client", "init-client");
        initClient.invoke();
    }
}
