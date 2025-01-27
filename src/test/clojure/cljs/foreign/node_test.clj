(ns cljs.foreign.node-test
  (:require [cljs.foreign.node :as node]
            [cljs.test-util :as test-util]
            [cljs.util :as util]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.test :as test :refer [deftest is testing]]))

(defn cleanup
  ([] (cleanup #()))
  ([f]
   (test-util/delete-node-modules)
   (doseq [f (map io/file
               ["package.json" "package-lock.json" "yarn.lock"
               "yarn-error.log"])]
     (when (.exists f)
       (io/delete-file f)))
   (f)))

(defn install
  ([lib version]
   (install :npm lib version))
  ([cmd lib version]
   (let [action ({:npm "install" :yarn "add"} cmd)]
     (sh/sh (name cmd) action (str lib "@" version)))))

(test/use-fixtures :once cleanup)

;; =============================================================================
;; Tests

(defn pkg-jsons []
  (-> (util/module-file-seq {})
    node/get-pkg-jsons))

(defn indexed-lib-specs []
  (as-> (-> (util/module-file-seq {})
          (node/node-file-seq->libs-spec* {}))
    xs (zipmap (map :file xs) xs)))

(defn relpath->data
  ([index path]
   (relpath->data index path :get))
  ([index path type]
   (let [abs-path (.getAbsolutePath (io/file path))]
    (case type
      :get  (get index abs-path)
      :find (find index abs-path)))))

(deftest test-basic
  (install "left-pad" "1.3.0")
  (testing "Install left-pad, verify that it is indexed and has a sensible lib-spec"
   (let [index (indexed-lib-specs)]
     (let [left-pad (relpath->data index "node_modules/left-pad/index.js")]
       (is (some? (:file left-pad)))
       (is (some? (:module-type left-pad)))
       (is (= #{"left-pad/index.js" "left-pad/index" "left-pad"}
             (into #{} (:provides left-pad))))))))

(deftest test-path->main-name
  (install :yarn "react-select" "5.7.2")
  (testing "Verify that path->main works as expected"
    (is (= "react-select"
           (node/path->main-name
              (.getAbsolutePath (io/file "node_modules/react-select/dist/react-select.cjs.js"))
              (relpath->data (pkg-jsons)
                "node_modules/react-select/package.json" :find)
              {:package-json-resolution :nodejs})))
    (is (= "react-select/creatable"
            (node/path->main-name
              (.getAbsolutePath (io/file "node_modules/react-select/creatable/dist/react-select-creatable.cjs.js"))
              (relpath->data (pkg-jsons)
                "node_modules/react-select/creatable/package.json" :find)
              {:package-json-resolution :nodejs})))
    (is (nil? (node/path->main-name
                (.getAbsolutePath (io/file "node_modules/react-select/dist/react-select.cjs.js"))
                (relpath->data (pkg-jsons)
                  "node_modules/react-select/package.json" :find)
                {:package-json-resolution :webpack})))))

(comment

  (test/run-tests)
  (cleanup)

  )
