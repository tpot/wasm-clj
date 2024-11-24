(ns wasm-clj.core-test
  (:require [clojure.test :refer :all]
            [wasm-clj.core :refer :all]))

;; Control instructions

(deftest test-nop
  (testing "nop"
    (is (= {:stack []}
           (-> initial-state (nop)))
        "Test u32.const zero value")
    (is (= {:stack [42]}
           (-> initial-state (u32-const 42) (nop)))
        "Test u32.const positive value")))

(deftest test-unreachable
  (testing "unreachable"
    (try
      (-> initial-state (u32-const 42) (unreachable))
      (catch Exception e
        (is (= {:stack [42]} (get (ex-data e) :state)))))))

;; Memory instructions

(deftest test-s32-const
  (testing "s32.const"

    (testing "Basic operation"
      (is (= {:stack [0]}
             (-> initial-state (s32-const 0)))
          "Test s32.const zero value")
      (is (= {:stack [42]}
             (-> initial-state (s32-const 42)))
          "Test s32.const positive value")
      (is (= {:stack [-42]}
             (-> initial-state (s32-const -42)))
          "Test s32.const negative value"))

    (testing "Max and min values"
      (is (= {:stack [Integer/MAX_VALUE]}
             (-> initial-state (s32-const Integer/MAX_VALUE)))
          "Test s32.const max value")
      (is (= {:stack [Integer/MIN_VALUE]}
             (-> initial-state (s32-const Integer/MIN_VALUE)))
          "Test s32.const min value"))

    (testing "Too high and too low values"
      (is (thrown? ArithmeticException
                   (-> initial-state (s32-const (+ 1 Integer/MAX_VALUE))))
          "Test s32.const value too high")
      (is (thrown? ArithmeticException
                   (-> initial-state (s32-const (- 1 Integer/MIN_VALUE))))
          "Test s32-const value too low"))))

(deftest test-u32-const

  (testing "u32.const"
    (testing "Basic operation"
      (is (= {:stack [0]}
             (-> initial-state (u32-const 0)))
          "Test u32.const zero value")
      (is (= {:stack [42]}
             (-> initial-state (u32-const 42)))
          "Test u32.const positive value")))

  (testing "Max and min values"
    (let [MIN_VALUE 0
          MAX_VALUE (long 0xffffffff)]
      (is (= {:stack [MAX_VALUE]}
             (-> initial-state (u32-const MAX_VALUE)))
          "Test u32.const max value")
      (is (= {:stack [MIN_VALUE]}
             (-> initial-state (u32-const MIN_VALUE)))
          "Test u32.const min value")))

  (testing "Too high and too low values"
    (is (thrown? ArithmeticException
                 (-> initial-state (u32-const (long 0x100000000))))
        "Test u32.const value too high")
    (is (thrown? ArithmeticException
                 (-> initial-state (u32-const -1))))))

(deftest test-u32-add
  (testing "u32.add"

    (testing "Basic operation"
      (is (= {:stack [43]}
             (-> initial-state (u32-const 42) (u32-const 1) (u32-add)))
          "Test u32.add basic operation"))

    (testing "Addition overflow"
      (is (= {:stack [0]}
             (-> initial-state (u32-const 0xffffffff) (u32-const 1) (u32-add))))
      (is (= {:stack [0xfffffffe]}
             (-> initial-state (u32-const 0xffffffff) (u32-const 0xffffffff) (u32-add)))))))
