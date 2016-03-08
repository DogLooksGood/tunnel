(ns tunnel.utils)

;; =============================================================================
;; Utillities

(defn vec->map
  [key xs]
  (into {}
    (mapv #(do [(key %) %]) xs)))

(defn diff
  "获取两份列表的差异,得到delta.
  delta中包含:
  key 作为主键的值
  add 新增的数据
  update 变化的数据
  remove 删除的数据"
  [key x-vec y-vec]
  (let [x (vec->map key x-vec)
        y (vec->map key y-vec)
        x-ks (set (keys x))
        y-ks (set (keys y))
        ks- (clojure.set/union x-ks y-ks)]
    (loop [ks ks-
           delta {:remove #{} :add [] :update {} :key key}]
      (if (empty? ks)
        delta
        (let [k (first ks)
              in-x (contains? x-ks k)
              in-y (contains? y-ks k)
              y-item (get y k)
              x-item (get x k)]
          (cond
            (and in-y (not in-x)) (recur (rest ks) (update delta :add conj y-item))
            (and in-x (not in-y)) (recur (rest ks) (update delta :remove conj k))
            (not= x-item y-item) (recur (rest ks) (update delta :update assoc k y-item))
            :else (recur (rest ks) delta)))))))

