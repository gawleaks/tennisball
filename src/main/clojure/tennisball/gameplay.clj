(ns tennisball.gameplay
  (:import [com.tennisball TennisBallEntity TennisBallItem]
           [net.fabricmc.fabric.api.client.rendering.v1 EntityRendererRegistry]
           [net.fabricmc.fabric.api.object.builder.v1.entity FabricEntityTypeBuilder]
           [net.minecraft.client.render.entity FlyingItemEntityRenderer]
           [net.minecraft.entity EntityDimensions EntityType EntityType$EntityFactory SpawnGroup]
           [net.minecraft.entity.damage DamageSource]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.item Item Item$Settings ItemStack]
           [net.minecraft.registry Registries Registry RegistryKey RegistryKeys]
           [net.minecraft.server.world ServerWorld]
           [net.minecraft.sound SoundCategory SoundEvents]
           [net.minecraft.stat Stats]
           [net.minecraft.util ActionResult Hand Identifier]
           [net.minecraft.util.hit BlockHitResult EntityHitResult]
           [net.minecraft.util.math Direction Vec3d]
           [net.minecraft.world World World$ExplosionSourceType]))

(def mod-id "tennisball")

(def bounce-damping 0.78)
(def floor-friction 0.92)
(def min-speed-to-bounce 0.08)
(def max-lifetime-ticks (* 20 30))
(def bounce-sound-cooldown-ticks 3)
(def explosive-power 2.0)

(defonce registry-state (atom {:registered? false
                               :entity-type nil
                               :item nil}))

(defn- ensure-registered! []
  (when-not (:registered? @registry-state)
    (throw (IllegalStateException. "Tennis Ball not registered yet"))))

(defn register-gameplay! []
  (when-not (:registered? @registry-state)
    (let [entity-id (Identifier/of mod-id "tennis_ball")
          entity-key (RegistryKey/of RegistryKeys/ENTITY_TYPE entity-id)
          entity-type (Registry/register
                       Registries/ENTITY_TYPE
                       entity-id
                       (-> (FabricEntityTypeBuilder/create
                            SpawnGroup/MISC
                            (reify EntityType$EntityFactory
                              (create [_ entity-type world]
                                (TennisBallEntity. entity-type world))))
                           (.dimensions (EntityDimensions/fixed (float 0.25) (float 0.25)))
                           (.trackRangeBlocks 4)
                           (.trackedUpdateRate 10)
                           (.build entity-key)))
          item-id (Identifier/of mod-id "tennis_ball")
          item-key (RegistryKey/of RegistryKeys/ITEM item-id)
          settings (-> (Item$Settings.)
                       (.registryKey item-key)
                       (.maxCount 16))
          item (Registry/register Registries/ITEM item-id (TennisBallItem. settings))]
      (reset! registry-state {:registered? true
                              :entity-type entity-type
                              :item item}))))

(defn entity-type []
  (let [cached (:entity-type @registry-state)]
    (if cached
      cached
      (let [resolved (.get Registries/ENTITY_TYPE (Identifier/of mod-id "tennis_ball"))]
        (when resolved
          (swap! registry-state assoc :registered? true :entity-type resolved))
        resolved))))

(defn tennis-ball-item []
  (let [cached (:item @registry-state)]
    (if cached
      cached
      (let [resolved (.get Registries/ITEM (Identifier/of mod-id "tennis_ball"))]
        (when resolved
          (swap! registry-state assoc :registered? true :item resolved))
        resolved))))

(defn register-client-renderer! []
  (when-let [type (entity-type)]
    (EntityRendererRegistry/register
     type
     (reify net.minecraft.client.render.entity.EntityRendererFactory
       (create [_ ctx]
         (FlyingItemEntityRenderer. ctx))))))

(defn item-use [^Item item ^World world ^PlayerEntity user ^Hand hand]
  (let [stack (.getStackInHand user hand)]
    (.playSound world
                nil
                (.getX user)
                (.getY user)
                (.getZ user)
                SoundEvents/ENTITY_SNOWBALL_THROW
                SoundCategory/NEUTRAL
                (float 0.5)
                (float (+ 0.8 (* (.nextFloat (.getRandom world)) 0.2))))

    (when (instance? ServerWorld world)
      (let [^ServerWorld server-world world
            explosive? (and (instance? TennisBallItem item)
                            (.isExplosive ^TennisBallItem item))
            ball-entity (TennisBallEntity. server-world user (.copyWithCount stack 1) explosive?)]
        (.setVelocity ball-entity user (.getPitch user) (.getYaw user) (float 0.0) (float 1.25) (float 0.3))
        (.spawnEntity server-world ball-entity)

        (when-not (.. user getAbilities creativeMode)
          (.decrement stack 1))

        (.incrementStat user (.getOrCreateStat Stats/USED item))))

    ActionResult/CONSUME))

(defn entity-gravity [_entity]
  0.035)

(defn- explode-and-discard! [^TennisBallEntity entity]
  (let [world (.getWorld entity)]
    (when (instance? ServerWorld world)
      (.createExplosion ^ServerWorld world
                        entity
                        (.getX entity)
                        (.getY entity)
                        (.getZ entity)
                        (float explosive-power)
                        World$ExplosionSourceType/TNT))
    (.discard entity)))

(defn entity-tick [^TennisBallEntity entity]
  (when (> (.tennisballGetAge entity) max-lifetime-ticks)
    (.discard entity))

  (when (.isOnGround entity)
    (let [velocity (.getVelocity entity)
          slowed (Vec3d. (* (.x velocity) floor-friction)
                         (.y velocity)
                         (* (.z velocity) floor-friction))
          grounded (if (< (Math/abs (.y slowed)) 0.03)
                     (Vec3d. (.x slowed) 0.0 (.z slowed))
                     slowed)]
      (.setVelocity entity grounded)
      (when (and (< (.horizontalLengthSquared grounded) 0.0009)
                 (< (Math/abs (.y grounded)) 0.01))
        (.discard entity)))))

(defn entity-on-block-hit [^TennisBallEntity entity ^BlockHitResult hit-result]
  (if (.tennisballIsExplosive entity)
    (explode-and-discard! entity)
    (let [velocity (.getVelocity entity)
        side (.getSide hit-result)]
      (if (and (= side Direction/UP) (< (Math/abs (.y velocity)) 0.08))
        (.setVelocity entity (* (.x velocity) 0.88) 0.0 (* (.z velocity) 0.88))
        (let [normal (Vec3d/of (.getVector side))
              dot (.dotProduct velocity normal)
              reflected (.multiply (.subtract velocity (.multiply normal (* 2.0 dot))) bounce-damping)
              reflected (if (and (= side Direction/UP) (> (.y reflected) 0.0))
                          (Vec3d. (* (.x reflected) 0.95)
                                  (* (.y reflected) 0.85)
                                  (* (.z reflected) 0.95))
                          reflected)
              min-speed-sq (* min-speed-to-bounce min-speed-to-bounce)
              final-velocity (if (< (.lengthSquared reflected) min-speed-sq)
                               (if (= side Direction/UP)
                                 (Vec3d. (* (.x reflected) 0.5) 0.0 (* (.z reflected) 0.5))
                                 nil)
                               reflected)]
          (if (nil? final-velocity)
            (.discard entity)
            (do
              (.setVelocity entity final-velocity)
              (.setPosition entity (.add (.getPos entity) (.multiply normal 0.01)))
              (when (and (not (.isSilent entity))
                         (>= (- (.tennisballGetAge entity) (.tennisballGetLastBounceSoundTick entity))
                             bounce-sound-cooldown-ticks)
                         (> (.lengthSquared reflected) (* 0.02 0.02)))
                (.playSound (.getWorld entity)
                            nil
                            (.getX entity)
                            (.getY entity)
                            (.getZ entity)
                            SoundEvents/BLOCK_SLIME_BLOCK_HIT
                            SoundCategory/NEUTRAL
                            (float 0.25)
                            (float (+ 1.5 (* (- (.nextFloat (.getRandom entity)) 0.5) 0.2))))
                (.tennisballSetLastBounceSoundTick entity (.tennisballGetAge entity))))))))))

(defn entity-on-entity-hit [^TennisBallEntity entity ^EntityHitResult hit-result]
  (if (.tennisballIsExplosive entity)
    (explode-and-discard! entity)
    (let [target (.getEntity hit-result)
          world (.getWorld entity)]
      (when (instance? ServerWorld world)
        (let [^ServerWorld server-world world
              ^DamageSource damage-source (.thrown (.getDamageSources entity) entity (.getOwner entity))]
          (.damage target server-world damage-source (float 1.0))))

      (let [bounced (.multiply (.getVelocity entity) -0.55 0.45 -0.55)]
        (.setVelocity entity
                      (.x bounced)
                      (max 0.08 (Math/abs (.y bounced)))
                      (.z bounced))))))
