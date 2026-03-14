(ns tennisball.client
  (:import [com.tennisball TennisBallBridge]
           [net.fabricmc.fabric.api.client.rendering.v1 EntityRendererRegistry]
           [net.minecraft.client.render.entity FlyingItemEntityRenderer]))

(defn- register-client-renderer! []
  (when-let [type (TennisBallBridge/entityType)]
    (EntityRendererRegistry/register
     type
     (reify net.minecraft.client.render.entity.EntityRendererFactory
       (create [_ ctx]
         (FlyingItemEntityRenderer. ctx))))))

;; Client-side initialization
(defn init-client []
  (println "Tennis Ball Mod - Client initializing...")
  (register-client-renderer!)
  (println "Tennis Ball Mod - Client initialized!"))
