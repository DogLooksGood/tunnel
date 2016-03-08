(ns tunnel.db-test
  (:require [clojure.test :refer :all]
            [tunnel.db :refer :all]))


(deftest user-login-test
  (testing ":user-login, 通过用户名和密码查找一个用户"
    (is (= {:user/username "dog" :user/password "123"}
          (query :user-login '[:user/username :user/password]
            {:username "dog" :password "123"})))
    (is (nil?
          (query :user-login '[:user/username :user/password]
            {:username "dog" :password "1234"})))))




