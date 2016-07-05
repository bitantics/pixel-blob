(ns pixel-blobs.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [clojure.set :as s]))


; Constants
(def width 500)
(def height 500)

(def cell-size 8)
(def max-blob-size 500)
(def min-value 0.20)

(def lavender-color [256 0.47 0.65])
(def background-color [256 0 0.65])


; Derived Constants
(def max-x (quot width cell-size))
(def mid-x (quot max-x 2))

(def max-y (quot height cell-size))
(def mid-y (quot max-y 2))


(defn scaled [min max value]
  (+ min (* value (- max min))))


(defn dist [x y]
  (js/Math.abs (- x y)))


(defn cell-value [x y t]
  (let [n-base (q/noise (* 0.1 x) (* 0.1 y) (* 0.005 t))
        n-noise (q/noise (* 0.8 x) (* 0.8 y) (-> t (* 0.001) (+ 2000)))

        v-base (scaled -1.5 2.2 n-base)
        v-noise (scaled -0.3 0.3 n-noise)]
    (+ v-base v-noise)))


(defn cell-neighbors [cx cy]
  (->>
    [[0 -1] [-1 0] [1 0] [0 1]]
    (map (fn [[xo yo]] [(+ cx xo) (+ cy yo)]))
    (filter (fn [[x y]] (and (<= 0 x)
                             (< x max-x)
                             (<= 0 y)
                             (< y max-y))))))

(defn find-visible-cells
  ([t] (find-visible-cells #{[mid-x mid-y]} (list [mid-x mid-y]) #{} 0 t))
  ([seen frontier visible visible-count t]
    (if (or (= 0 (count frontier))
            (<= max-blob-size visible-count))
      visible
      (let [[x y :as cell] (first frontier)
            visible? (< min-value (cell-value x y t))
            neighbors (apply cell-neighbors cell)
            unseen (filter (comp not (partial contains? seen)) neighbors)
            
            new-seen (s/union seen (set unseen))
            new-frontier (if visible?
                           (concat (rest frontier) unseen)
                           (rest frontier))
            new-visible (if visible?
                          (conj visible cell)
                          visible)
            new-visible-count (+ visible-count (if visible? 1 0))]
        (recur new-seen new-frontier new-visible new-visible-count t)))))


(defn set-viable-seed! []
  (while (< (cell-value mid-x mid-y 0) min-value)
    (q/noise-seed (q/random 10000))))

(defn next-valid-step [step]
  (if (< (cell-value mid-x mid-y (+ step 1)) min-value)
    (recur (+ step 1))
    (+ step 1)))

(defn setup []
  (q/color-mode :hsb 360 1.0 1.0 1.0)
  (q/frame-rate 5)

  (set-viable-seed!)
  {:visible-cells (find-visible-cells 0)
   :hovering? false
   :step 0})

(defn on-mouse-enter [state]
  (assoc state :hovering? true))

(defn on-mouse-exit [state]
  (assoc state :hovering? false))

(defn update-state [{step :step hovering? :hovering? :as state}]
  (if (not hovering?)
    state
    (assoc state :visible-cells (find-visible-cells step)
                 :step (next-valid-step step))))


(defn draw-state [{step :step visible-cells :visible-cells}]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 0 0)
  (q/no-stroke)

  (doseq [x (range (quot width cell-size))
          y (range (quot height cell-size))]
    (let [color (if (contains? visible-cells [x y])
                  (conj lavender-color (cell-value x y step))
                  [0 0 0 0])]
      (apply q/fill color)
      (apply q/rect (mapv #(* % cell-size)
                          [x y 1 1])))))


(q/defsketch pixel-blobs
  :host "pixel-blobs"
  :size [width height]
  :setup setup
  :update update-state
  :draw draw-state
  :mouse-entered on-mouse-enter
  :mouse-exited on-mouse-exit
  :middleware [m/fun-mode])
