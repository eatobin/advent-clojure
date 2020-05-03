(ns advent.day11
  (:require [advent.intcode :as ic]
            [clojure.math.numeric-tower :as math]))

;part a
(def tv (ic/make-tv "resources/day11.csv"))
(def tv-test (ic/make-tv "resources/day11-test.csv"))

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

(defn runner [visits oc]
  (loop [c ((last @visits) :c)]
    (atom (reset! oc (ic/op-code (assoc @oc :input c))))
    (if (@oc :stopped?)
      (count (distinct (map :pt (vec (remove #(nil? (% :rp)) @visits)))))
      (do
        (atom (reset! visits (paint-atom @visits (@oc :output))))
        (atom (swap! oc ic/op-code))
        (atom (reset! visits (turn-atom @visits (@oc :output))))
        (recur
          ((last @visits) :c))))))

(def answer (runner visits oc))

(println answer)

;1771

;part b
(defn runner-2 [visits oc]
  (loop [c ((last @visits) :c)]
    (atom (reset! oc (ic/op-code (assoc @oc :input c))))
    (if (@oc :stopped?)
      (map #(select-keys % [:pt :c]) @visits)
      (do
        (atom (reset! visits (paint-atom @visits (@oc :output))))
        (atom (swap! oc ic/op-code))
        (atom (reset! visits (turn-atom @visits (@oc :output))))
        (recur
          ((last @visits) :c))))))

(def raw-visits (vec (runner-2 visits oc)))

(defn corrector [{{x :x, y :y} :pt c :c}]
  (let [closest-x (apply min (map #(get-in % [:pt :x]) raw-visits))
        closest-y (apply max (map #(get-in % [:pt :y]) raw-visits))]
    {:row (if (<= y 0) (+ (math/abs y) closest-y) (- closest-y y))
     :col (+ (math/abs closest-x) x) :c c}))

(def corrected (into (sorted-map) (zipmap (range) (map corrector raw-visits))))
(println \u25A0)
;■
;=> nil
(println \u25A1)
;□
;=> nil
(assoc-in [] [0] \h)
;=> [\h]
(assoc-in [] [0 0] \h)
;=> [{0 \h}]
(assoc-in [[] []] [1 0] \h)
;=> [[] [\h]]

(clojure.pprint/print-table [{0 \u25A0 1 \u25A1 2 \u25A0} {0 \u25A1 1 \u25A0 2 \u25A1} {0 \u25A0 1 \u25A1 2 \u25A0}])
(clojure.pprint/print-table [{0 \u25A0 1 "" 2 \u25A0} {0 "" 1 \u25A0 2 ""} {0 \u25A0 1 "" 2 \u25A0}])
(def my-vals (map val corrected))
(def num-rows (apply max (map #(get % :row) my-vals)))
(def num-cols (apply max (map #(get % :col) my-vals)))
(def my-grid (map #(hash-map % 0) (range num-rows)))
