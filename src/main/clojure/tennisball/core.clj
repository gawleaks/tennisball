(ns tennisball.core
  (:import [com.tennisball TennisBallRegistry]))

(defn register-item []
  (TennisBallRegistry/register)
  (println "Registered Tennis Ball Item (tennis physics)"))

(defn init []
  (println "Tennis Ball Mod - Starting...")
  (register-item)
  (println "Tennis Ball Mod - Initialized!"))
