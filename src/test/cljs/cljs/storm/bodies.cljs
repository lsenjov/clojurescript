(ns cljs.storm.bodies
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [cljs.storm.tests.bodies :as b]
            [cljs.storm.utils :as u]))

(use-fixtures :each u/reset-captured-traces-fixture)

(deftest try-catch-test
  (let [r (b/tried)]
    (is (= 4 r) "function return should be right.")
    (is (= [[:fn-call "cljs.storm.tests.bodies" "tried" [] 375669735]
            [:expr-exec 2 "3,1" 375669735]
            [:expr-exec "#object[...]" "3,2,1" 375669735]
            [:expr-exec "#object[...]" "3,3,1" 375669735]
            [:bind "e" "#object[...]" ""]
            [:fn-return 4 "3,3,3" 375669735]]
           (u/capture)) "captured traces should match.")))

(deftest letfn-test
  (let [r (b/letfn-fn)]
    (is (= 5 r) "function return should be right.")
    (is (= [[:fn-call "cljs.storm.tests.bodies" "letfn-fn" [] 1673004201]
            [:fn-call "cljs.storm.tests.bodies" "square" [2] 1673004201]
            [:bind "x" 2 ""]
            [:expr-exec 2 "3,1,0,2,1" 1673004201]
            [:expr-exec 2 "3,1,0,2,2" 1673004201]
            [:fn-return 4 "3,1,0,2" 1673004201]
            [:expr-exec 4 "3,2,2" 1673004201]
            [:fn-return 5 "3,2" 1673004201]]
           (u/capture)) "captured traces should match.")))

(deftest loops-test
  (let [r (b/looper)]
    (is (= 3 r) "function return should be right.")
    (is (= [[:fn-call "cljs.storm.tests.bodies" "looper" [] -1606941997]
            [:bind "s" 0 "3"]
            [:bind "n" 2 "3"]

            [:bind "s" 0 "3"]
            [:bind "n" 2 "3"]
            [:expr-exec 2 "3,2,1,1" -1606941997]
            [:expr-exec false "3,2,1" -1606941997]
            [:expr-exec 0 "3,2,3,1,1" -1606941997]
            [:expr-exec 2 "3,2,3,1,2" -1606941997]
            [:expr-exec 2 "3,2,3,1" -1606941997]
            [:expr-exec 2 "3,2,3,2,1" -1606941997]
            [:expr-exec 1 "3,2,3,2" -1606941997]

            [:bind "s" 2 "3"]
            [:bind "n" 1 "3"]
            [:expr-exec 1 "3,2,1,1" -1606941997]
            [:expr-exec false "3,2,1" -1606941997]
            [:expr-exec 2 "3,2,3,1,1" -1606941997]
            [:expr-exec 1 "3,2,3,1,2" -1606941997]
            [:expr-exec 3 "3,2,3,1" -1606941997]
            [:expr-exec 1 "3,2,3,2,1" -1606941997]
            [:expr-exec 0 "3,2,3,2" -1606941997]

            [:bind "s" 3 "3"]
            [:bind "n" 0 "3"]
            [:expr-exec 0 "3,2,1,1" -1606941997]
            [:expr-exec true "3,2,1" -1606941997]

            [:fn-return 3 "3,2,2" -1606941997]]
           (u/capture)) "captured traces should match.")))

(deftest let-test
  (let [r (b/letter)]
    (is (= 15 r) "function return should be right.")
    (is (= [[:fn-call "cljs.storm.tests.bodies" "letter" [] 844078910]
            [:bind "a" 5 "3"]
            [:expr-exec 5 "3,1,3,1" 844078910]
            [:expr-exec 10 "3,1,3" 844078910]
            [:bind "b" 10 "3"]
            [:expr-exec 5 "3,1,5,1,1,1" 844078910]
            [:expr-exec 10 "3,1,5,1,1,2" 844078910]
            [:expr-exec 15 "3,1,5,1,1" 844078910]
            [:bind "z" 15 "3,1,5"]
            [:bind "c" 15 "3"]
            [:fn-return 15 "3,2" 844078910]]
           (u/capture)) "captured traces should match.")))

(deftest case-test
  (let [r (b/casey :first)]
    (is (= 42 r) "function return should be right.")
    (is (= [[:fn-call "cljs.storm.tests.bodies" "casey" [":first"] -742825312]
            [:bind "x" ":first" ""]
            [:expr-exec ":first" "3,1" -742825312]
            [:fn-return 42 "3,3" -742825312]]
           (u/capture)) "captured traces should match.")))

(deftest do-test
  (let [r (b/doer)]
    (is (= 8 r) "function return should be right.")
    (is (= [[:fn-call "cljs.storm.tests.bodies" "doer" [] -1895706795]
            [:expr-exec 2 "3,1" -1895706795]
            [:expr-exec 4 "3,2" -1895706795]
            [:expr-exec 6 "3,3,1" -1895706795]
            [:fn-return 8 "3,3,2" -1895706795]]
           (u/capture)) "captured traces should match.")))

(deftest set!-test
  (let [r (b/setter)]
    (is (= 42 r) "function return should be right.")
    (is (= [[:fn-call "cljs.storm.tests.bodies" "setter" [] 807580509]
            [:expr-exec 2 "3" 807580509]
            [:expr-exec 40 "4" 807580509]
            [:expr-exec 2 "5,1" 807580509]
            [:expr-exec 40 "5,2" 807580509]
            [:fn-return 42 "5" 807580509]]
           (u/capture)) "captured traces should match.")))

(deftest interop-test
  (let [r (b/interopter #js {:num 2 :f (fn f [x] x)})]
    (is (= 42 r) "function return should be right.")
    (is (= [[:fn-call "cljs.storm.tests.bodies" "interopter" ["#js {:num 2, :f #object[...]}"] 919708180]
            [:bind "o" "#js {:num 2, :f #object[...]}" ""]
            [:expr-exec "#js {:num 2, :f #object[...]}" "3,1,1" 919708180]
            [:expr-exec 2 "3,1" 919708180]
            [:expr-exec "#js {:num 2, :f #object[...]}" "3,2,1" 919708180]
            [:expr-exec 40 "3,2" 919708180]
            [:fn-return 42 "3" 919708180]]
           (u/capture)) "captured traces should match.")))
