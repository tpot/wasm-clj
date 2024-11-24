(ns wasm-clj.core
    (:require [clojure.test :refer :all]))

;; Memory instructions

(def initial-state
  {:stack (vector)})

;; Const integer operations

(defn s32-const
  "Push a signed 32-bit integer onto the stack"
  [state s32]
  (let [stack (get state :stack)]
    (merge state {:stack (conj stack (int s32))})))

(defn u32-const
  "Push an unsigned 32-bit integer onto the stack"
  [state u32]
  (when (< u32 0) (throw (ArithmeticException. "u32.const input too small")))
  (when (> u32 0xffffffff) (throw (ArithmeticException. "u32.const input too large")))
  (let [stack (get state :stack)
        ;; Java integers are signed so we must use Long.
        v (long (bit-and u32 0xffffffff))]
    (assert (<= v 0xffffffff))
    (merge state {:stack (conj stack v)})))

(defn u32-add
  "Push the sum of the top two integers on the stack"
  [state]
  (let [stack (get state :stack)
        a (last stack)
        b (last (butlast stack))
        newstate (merge state {:stack (pop (pop stack))})]
    (u32-const newstate (bit-and (+ a b) 0xffffffff))))

;; Control instructions

(defn unreachable
  "Throw an exception"
  [state]
  (throw (ex-info "Unreachable"
                  {:state state})))

(defn nop
  "No operation - no change of state"
  [state]
  state)
