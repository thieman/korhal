(ns korhal.macro.build-order
  (:require [korhal.interop.interop :refer [print-text]]))

(def double-rax-mnm
  [9 :supply-depot
   11 :send-scout
   11 :barracks
   13 :barracks
   15 :supply-depot
   15 :refinery])

(def triple-rax-mnm
  [9 :supply-depot
   11 :send-scout
   11 :barracks
   13 :barracks
   15 :supply-depot
   15 :refinery
   15 :engineering-bay
   15 :academy
   :upgrade :infantry-weapons])

(def triple-factory-vulture
  [9 :supply-depot
   11 :barracks
   12 :refinery
   13 :send-scout
   15 :supply-depot
   :train :marine
   18 :factory
   :train :marine
   20 :factory
   :train :marine
   :addon :machine-shop
   22 :supply-depot
   :train :vulture
   25 :wait
   :upgrade :ion-thrusters
   :addon :machine-shop
   30 :supply-depot
   32 :factory
   :research :spider-mines])

(def one-rax-fast-expand-marine-defense
  [9 :supply-depot
   11 :barracks
   12 :refinery
   13 :send-scout
   15 :supply-depot
   16 :wait
   :train :marine
   18 :factory
   19 :wait
   :train :marine
   21 :wait
   :train :marine
   :addon :machine-shop
   :train :marine
   24 :supply-depot
   26 :wait
   :train :siege-tank-tank-mode
   28 :wait
   :research :tank-siege-mode
   28 :command-center])

(def one-rax-fast-expand-no-defense
  [9 :supply-depot
   11 :barracks
   12 :refinery
   13 :send-scout
   15 :supply-depot
   16 :wait
   :train :marine
   18 :factory
   :addon :machine-shop
   23 :command-center
   24 :wait
   :train :siege-tank-tank-mode
   26 :supply-depot
   :research :tank-siege-mode
   :train :siege-tank-tank-mode
   29 :engineering-bay])

#_(def build-orders
  {:double-rax-mnm double-rax-mnm
   :triple-rax-mnm triple-rax-mnm
   :triple-factory-vulture triple-factory-vulture
   :one-rax-fast-expand-marine-defense one-rax-fast-expand-marine-defense
   :one-rax-fast-expand-no-defense one-rax-fast-expand-no-defense})

(def build-orders
  {:triple-factory-vulture triple-factory-vulture})

(defn get-random-build-order []
  (let [k (nth (keys build-orders) (rand-int (count build-orders)))]
    (print-text (str "Build order: " (name k)))
    (build-orders k)))
