(ns advent.day9
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

;part a

(def tv (->> (first (with-open [reader (io/reader "resources/day9.csv")]
                      (doall
                        (csv/read-csv reader))))
             (map #(Integer/parseInt %))
             (into [])
             (zipmap (range))
             (into (sorted-map-by <))))

(def sample [109, 1, 204, -1, 1001, 100, 1, 100, 1008, 100, 16, 101, 1006, 101, 0, 99])
(def good (into (sorted-map-by <) (zipmap (range) sample)))

(def sample-2 [1102, 34915192, 34915192, 7, 4, 7, 99, 0])
(def good-2 (into (sorted-map-by <) (zipmap (range) sample-2)))

(def sample-3 [104, 1125899906842624, 99])
(def good-3 (into (sorted-map-by <) (zipmap (range) sample-3)))

(defn pad-5 [n]
  (zipmap [:a :b :c :d :e]
          (for [n (format "%05d" n)]
            (- (byte n) 48))))

; y1
(defn a-p-w [pointer memory]
  (memory (+ 3 pointer)))

; y2, y9
(defn b-p-r-b-r-r [pointer memory relative-base]
  (get memory (+ (memory (+ 2 pointer)) relative-base) 0))

; y3, y10
(defn c-p-r-c-r-r [pointer memory relative-base]
  (get memory (+ (memory (+ 1 pointer)) relative-base) 0))

; y4, y6
(defn c-p-w-c-i-r [pointer memory]
  (memory (+ 1 pointer)))

; y5
(defn b-i-r [pointer memory]
  (memory (+ 2 pointer)))

; y7
(defn a-r-w [pointer memory relative-base]
  (+ (memory (+ 3 pointer)) relative-base))

; y8
(defn c-r-w [pointer memory relative-base]
  (+ (memory (+ 1 pointer)) relative-base))

(defn param-maker-c [instruction pointer memory relative-base]
  (case (instruction :e)
    1 (case (instruction :c)
        0 (c-p-r-c-r-r pointer memory 0)
        1 (c-p-w-c-i-r pointer memory)
        2 (c-p-r-c-r-r pointer memory relative-base))
    2 (case (instruction :c)
        0 (c-p-r-c-r-r pointer memory 0)
        1 (c-p-w-c-i-r pointer memory)
        2 (c-p-r-c-r-r pointer memory relative-base))
    3 (case (instruction :c)
        0 (c-p-w-c-i-r pointer memory)
        2 (c-r-w pointer memory relative-base))
    4 (case (instruction :c)
        0 (c-p-r-c-r-r pointer memory 0)
        1 (c-p-w-c-i-r pointer memory)
        2 (c-p-r-c-r-r pointer memory relative-base))
    5 (case (instruction :c)
        0 (c-p-r-c-r-r pointer memory 0)
        1 (c-p-w-c-i-r pointer memory)
        2 (c-p-r-c-r-r pointer memory relative-base))
    6 (case (instruction :c)
        0 (c-p-r-c-r-r pointer memory 0)
        1 (c-p-w-c-i-r pointer memory)
        2 (c-p-r-c-r-r pointer memory relative-base))
    7 (case (instruction :c)
        0 (c-p-r-c-r-r pointer memory 0)
        1 (c-p-w-c-i-r pointer memory)
        2 (c-p-r-c-r-r pointer memory relative-base))
    8 (case (instruction :c)
        0 (c-p-r-c-r-r pointer memory 0)
        1 (c-p-w-c-i-r pointer memory)
        2 (c-p-r-c-r-r pointer memory relative-base))))

(defn param-maker-b [instruction pointer memory]
  (case (instruction :e)
    1 (case (instruction :b)
        0 (b-p-r-b-r-r pointer memory 0)
        1 (b-i-r pointer memory)
        2 (b-p-r-b-r-r pointer memory 0))
    2 (case (instruction :b)
        0 (b-p-r-b-r-r pointer memory 0)
        1 (b-i-r pointer memory)
        2 (b-p-r-b-r-r pointer memory 0))
    5 (case (instruction :b)
        0 (b-p-r-b-r-r pointer memory 0)
        1 (b-i-r pointer memory)
        2 (b-p-r-b-r-r pointer memory 0))
    6 (case (instruction :b)
        0 (b-p-r-b-r-r pointer memory 0)
        1 (b-i-r pointer memory)
        2 (b-p-r-b-r-r pointer memory 0))
    7 (case (instruction :b)
        0 (b-p-r-b-r-r pointer memory 0)
        1 (b-i-r pointer memory)
        2 (b-p-r-b-r-r pointer memory 0))
    8 (case (instruction :b)
        0 (b-p-r-b-r-r pointer memory 0)
        1 (b-i-r pointer memory)
        2 (b-p-r-b-r-r pointer memory 0))))

(defn param-maker-a [instruction pointer memory relative-base]
  (case (instruction :e)
    1 (case (instruction :a)
        0 (a-p-w pointer memory)
        2 (a-r-w pointer memory relative-base))
    2 (case (instruction :a)
        0 (a-p-w pointer memory)
        2 (a-r-w pointer memory relative-base))
    7 (case (instruction :a)
        0 (a-p-w pointer memory)
        2 (a-r-w pointer memory relative-base))
    8 (case (instruction :a)
        0 (a-p-w pointer memory)
        2 (a-r-w pointer memory relative-base))))

(defn op-code [[input phase pointer relative-base memory stopped? recur?]]
  (if stopped?
    [input phase pointer relative-base memory stopped? recur?]
    (let [instruction (pad-5 (memory pointer))]
      (case (instruction :e)
        9 (if (= (instruction :d) 9)
            [input phase pointer relative-base memory true recur?]
            (recur
              input
              (+ 2 pointer)
              (+ (param-mode-c instruction pointer memory relative-base) relative-base)
              memory
              stopped?))
        1 (recur
            input
            (+ 4 pointer)
            relative-base
            (assoc memory (param-mode-a instruction pointer memory relative-base)
                          (+ (param-mode-c instruction pointer memory relative-base)
                             (param-mode-b instruction pointer memory relative-base)))
            stopped?)
        2 (recur
            input
            (+ 4 pointer)
            relative-base
            (assoc memory (param-mode-a instruction pointer memory relative-base)
                          (* (param-mode-c instruction pointer memory relative-base)
                             (param-mode-b instruction pointer memory relative-base)))
            stopped?)
        3 (recur
            input
            (+ 2 pointer)
            relative-base
            (if (= (instruction :c) 2)
              (if (= 0 pointer)
                (assoc memory (+ (memory (+ 1 pointer)) relative-base) phase)
                (assoc memory (+ (memory (+ 1 pointer)) relative-base) input))
              (if (= 0 pointer)
                (assoc memory (memory (+ 1 pointer)) phase)
                (assoc memory (memory (+ 1 pointer)) input)))
            stopped?)
        4 (if recur?
            (recur
              (param-mode-c instruction pointer memory relative-base)
              (+ 2 pointer)
              relative-base
              memory
              stopped?)
            [(param-mode-c instruction pointer memory relative-base) phase (+ 2 pointer) relative-base memory stopped? recur?])
        5 (recur
            input
            (if (= 0 (param-mode-c instruction pointer memory relative-base))
              (+ 3 pointer)
              (param-mode-b instruction pointer memory relative-base))
            relative-base
            memory
            stopped?)
        6 (recur
            input
            (if (not= 0 (param-mode-c instruction pointer memory relative-base))
              (+ 3 pointer)
              (param-mode-b instruction pointer memory relative-base))
            relative-base
            memory
            stopped?)
        7 (recur
            input
            (+ 4 pointer)
            relative-base
            (if (< (param-mode-c instruction pointer memory relative-base) (param-mode-b instruction pointer memory relative-base))
              (assoc memory (memory (+ 3 pointer)) 1)
              (assoc memory (memory (+ 3 pointer)) 0))
            stopped?)
        8 (recur
            input
            (+ 4 pointer)
            relative-base
            (if (= (param-mode-c instruction pointer memory relative-base) (param-mode-b instruction pointer memory relative-base))
              (assoc memory (memory (+ 3 pointer)) 1)
              (assoc memory (memory (+ 3 pointer)) 0))
            stopped?)))))
