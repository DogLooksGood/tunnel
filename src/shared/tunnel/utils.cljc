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
            (and in-y (not in-x))
            (recur (rest ks) (update delta :add conj y-item))
            (and in-x (not in-y))
            (recur (rest ks) (update delta :remove conj k))
            (not= x-item y-item)
            (recur (rest ks) (update delta :update assoc k y-item))
            :else (recur (rest ks) delta)))))))

(defn delta?
  [delta]
  (not
    (and
      (empty? (:remove delta))
      (empty? (:update delta))
      (empty? (:add delta)))))

(defn join
  "把变化应用在一个列表上,获得变化之后的列表."
  [x delta]
  (let [{:keys [key add update remove]} delta]
    (->> x
      (filter #(not (contains? remove (get % key))))
      (map #(get update (get % key) %))
      (lazy-cat add)
      dedupe
      vec)))

;; bi-directional map
;; http://stackoverflow.com/questions/1183232/a-bi-directional-map-in-clojure
(defn bimap [& args]
  (let [m (apply hash-map args)]
    (with-meta m
      (clojure.set/map-invert m))))

(defn bi-assoc
  [m k v]
  (vary-meta (assoc m k v)
    assoc v k))

(defn bi-dissoc
  [m k]
  (vary-meta (dissoc m k)
    (dissoc k)))

(defn get-invert
  [m k]
  ((meta m) k))

;; http://stackoverflow.com/questions/14488150/how-to-write-a-dissoc-in-command-for-clojure
(defn dissoc-in
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))
