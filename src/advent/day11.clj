(ns advent.day11
  (:require [advent.intcode :as ic]))

;part a
(def tv (ic/make-tv "resources/day11.csv"))

(ic/op-code {:input 1 :phase nil :pointer 0 :relative-base 0 :memory tv :stopped? false :recur? false})

(defn new-heading [heading turn]
  (cond
    (and (= heading 0) (= turn 0)) 3
    (and (= heading 3) (= turn 0)) 2
    (and (= heading 2) (= turn 0)) 1
    (and (= heading 1) (= turn 0)) 0
    (and (= heading 3) (= turn 1)) 0
    :else (+ heading turn)))

(defn new-point [{:keys [x y]} h]
  (case h
    0 {:x x :y (+ y 1)}
    1 {:x (+ x 1) :y y}
    2 {:x x :y (+ y -1)}
    3 {:x (+ x -1) :y y}))

(def state {:pt {:x 0 :y 0} :c 0})

(def states (atom [{:pt {:x 0, :y 0}, :h 0, :c 0}]))

(defn update-atom [coll p t]
  (let [{:keys [pt h]} (last coll)
        new-2 [{:pt pt :c p} {:pt (new-point pt (new-heading h t)) :h (new-heading h t) :c 0}]]
    (into (vec (butlast coll)) new-2)))

(swap! states update-atom 1 0)
(swap! states update-atom 0 0)
(swap! states update-atom 1 0)
(swap! states update-atom 1 0)

(defn eq-pts [{tpt :pt} {pt :pt c :c}]
  (if (= tpt pt)
    c
    nil))

(defn map-eq-pts [{tpt :pt} pts]
  (vec (map (fn [{pt :pt c :c}] (if (= tpt pt) c nil)) pts)))

(map-eq-pts {:pt {:x 0, :y 0}} [{:pt {:x 0, :y 0}, :c 1} {:pt {:x 0, :y 90}, :c 1} {:pt {:x 0, :y 0}, :c 19}])
;=> [1 nil 19]