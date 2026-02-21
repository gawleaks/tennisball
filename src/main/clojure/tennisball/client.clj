(ns tennisball.client
  (:import [com.tennisball TennisBallBridge]))

;; Client-side initialization
(defn init-client []
  (println "Tennis Ball Mod - Client initializing...")
  (TennisBallBridge/registerClientRenderer)
  (println "Tennis Ball Mod - Client initialized!"))
