(ns korhal.macro.build-order
  (:require [korhal.interop.interop :refer [print-text]]))

(def build-orders
  [:double-rax-mnm
   :triple-raw-mnm
   :triple-factory-vulture
   :one-rax-fast-expand-marine-defense
   :one-rax-fast-expand-no-defense])

(def double-rax-mnm
  [9 :supply-depot
   :with-builder :scout
   11 :barracks
   13 :barracks
   15 :supply-depot
   :asap :refinery])

(def triple-rax-mnm
  [9 :supply-depot
   :with-builder :scout
   11 :barracks
   13 :barracks
   15 :supply-depot
   :asap :refinery
   :asap :engineering-bay
   :asap :academy
   :upgrade :infantry-weapons])

(def triple-factory-vulture
  [9 :supply-depot
   11 :barracks
   12 :refinery
   :with-builder :scout
   15 :supply-depot
   :train :marine
   18 :factory
   :train :marine
   20 :factory
   :train :marine
   22 :machine-shop
   22 :supply-depot
   :train :vulture
   25 :wait
   :research :ion-thrusters
   :continuous :vulture
   27 :machine-shop
   30 :supply-depot
   32 :factory
   :research :spider-mines])

(def one-rax-fast-expand-marine-defense
  [9 :supply-depot
   11 :barracks
   12 :refinery
   :with-builder :scout
   15 :depot
   16 :wait
   :train :marine
   18 :factory
   19 :wait
   :train :marine
   21 :wait
   :train :marine
   23 :machine-shop
   :train :marine
   24 :supply-depot
   26 :siege-tank-tank-mode
   28 :wait
   :research :tank-siege-mode
   28 :command-center])

(def one-rax-fast-expand-no-defense
  [9 :supply-depot
   11 :barracks
   12 :refinery
   :with-builder :scout
   15 :depot
   16 :wait
   :train :marine
   18 :factory
   21 :machine-shop
   23 :command-center
   24 :siege-tank-tank-mode
   26 :supply-depot
   :research :tank-siege-mode
   27 :siege-tank-tank-mode
   29 :engineering-bay])

(defn get-build-order [build-order-kw]
  (let [bo (eval `(symbol (name ~build-order-kw)))]
    (print-text (str "Build order: " (name bo)))
    bo))

(defn get-random-build-order []
  (get-build-order (nth build-orders (rand-int (count build-orders)))))
