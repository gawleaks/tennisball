package com.tennisball;

import net.fabricmc.api.ModInitializer;
import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class TennisBallMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // Load and initialize the Clojure namespace
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("tennisball.core"));
        
        // Call the init function
        IFn init = Clojure.var("tennisball.core", "init");
        init.invoke();
    }
}
