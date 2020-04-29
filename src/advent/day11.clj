(ns advent.day11
  (:require [advent.intcode :as ic]))

;First, it will output a value indicating the color to paint the panel the robot is over:
;   0 means to paint the panel black, and 1 means to paint the panel white.
;Second, it will output a value indicating the direction the robot should turn:
;   0 means it should turn left 90 degrees, and 1 means it should turn right 90 degrees.

;part a
;(def tv (ic/make-tv "resources/day11.csv"))
(def tv (ic/make-tv "resources/day11-test.csv"))

(ic/op-code {:input 0 :output nil :phase nil :pointer 0 :relative-base 0 :memory tv :stopped? false :recur? true})

(defn new-heading [heading turn]
  (cond
    (and (= heading :n) (= turn 0)) :w
    (and (= heading :n) (= turn 1)) :e
    (and (= heading :e) (= turn 0)) :n
    (and (= heading :e) (= turn 1)) :s
    (and (= heading :s) (= turn 0)) :e
    (and (= heading :s) (= turn 1)) :w
    (and (= heading :w) (= turn 0)) :s
    (and (= heading :w) (= turn 1)) :n))

(defn new-point [{x :x y :y} h]
  (case h
    :n {:x x :y (inc y)}
    :e {:x (inc x) :y y}
    :s {:x x :y (dec y)}
    :w {:x (dec x) :y y}))

(defn repainted? [c p]
  (cond
    (and (= c 0) (= p 1)) 1
    (and (= c 1) (= p 0)) 0
    (and (= c 1) (= p 1)) 1
    (and (= c 0) (= p 0)) 0))

(def state {:pt {:x 0 :y 0} :h :n :c 0 :rp nil})

(def states (atom [{:pt {:x 0 :y 0} :h :n :c 0 :rp nil}]))

(def visits (atom [{:pt {:x 0 :y 0} :h :n :c 0 :rp nil}]))

(def oc (atom {:input nil :output nil :phase nil :pointer 0 :relative-base 0 :memory tv :stopped? false :recur? false}))

(defn map-eq-pts
  "Takes a {:x 3 :y 3} as target point (tpt)"
  [tpt pts]
  (vec (map (fn [{pt :pt c :c}] (if (= tpt pt) c nil)) pts)))

(defn dups-check
  "Takes a {:x 3 :y 3} as target point (tpt)"
  [tpt pts]
  (let [c (->>
            pts
            (map-eq-pts tpt)
            (butlast)
            (remove nil?)
            (last))]
    (if (nil? c)
      0
      c)))

(defn paint-atom [coll p]
  (let [{:keys [pt h c]} (last coll)
        painted [{:pt pt :h h :c p :rp (repainted? c p)}]]
    (into (vec (butlast coll)) painted)))

(defn turn-atom [coll t]
  (let [{:keys [pt h]} (last coll)
        new-point (new-point pt (new-heading h t))
        turned [{:pt new-point :h (new-heading h t) :c (dups-check new-point coll) :rp nil}]]
    (into (vec coll) turned)))

(defn count-repaints [coll]
  (count (filter #(some? (% :rp)) coll)))

;[1 0]
(swap! states paint-atom 1)
(swap! states turn-atom 0)

;[0 0]
(swap! states paint-atom 0)
(swap! states turn-atom 0)

;[1 0]
(swap! states paint-atom 1)
(swap! states turn-atom 0)

;[1 0]
(swap! states paint-atom 1)
(swap! states turn-atom 0)

;Back to start

;[0 1]
(swap! states paint-atom 0)
(swap! states turn-atom 1)

;[1 0]
(swap! states paint-atom 1)
(swap! states turn-atom 0)

;[1 0]
(swap! states paint-atom 1)
(swap! states turn-atom 0)

;6 repaints

;.....
;..<#.
;...#.
;.##..
;.....

(defn runner [visits oc]
  (loop [c ((last @visits) :c)]
    (atom (reset! oc (ic/op-code (assoc @oc :input c))))
    (if (@oc :stopped?)
      [@oc @visits]
      (do
        (atom (reset! visits (paint-atom @visits (@oc :output))))
        (atom (swap! oc ic/op-code))
        (atom (reset! visits (turn-atom @visits (@oc :output))))
        (recur
          ((last @visits) :c))))))

(def run-done (runner visits oc))

(def answer (count (distinct (map :pt (vec (remove #(nil? (% :rp)) @visits))))))

(println answer)
