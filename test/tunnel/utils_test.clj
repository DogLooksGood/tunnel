(ns tunnel.utils-test
  (:require [clojure.test :refer :all]
            [tunnel.utils :refer :all]))

(deftest test-diff
  (let [x [{:id 1 :name "dog"}
           {:id 2 :name "cat"}
           {:id 3 :name "foo"}]
        y [{:id 2 :name "cat"}
           {:id 3 :name "bar"}
           {:id 4 :name "baz"}]]
    
    (testing "测试用diff获取两份数据的差异."
      (is
        (=
          (diff :id x y)
          {:remove #{1}
           :add [{:id 4 :name "baz"}]
           :update {3 {:id 3 :name "bar"}}
           :key :id})))

    (testing "讲差异应用在一份数据上, 可以得到另外一份数据."
      (is
        ;; 因为无序, 所以这里转set
        (= (set (join x (diff :id x y)))
          (set y))))))
