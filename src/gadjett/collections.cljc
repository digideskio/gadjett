(ns gadjett.collections
  (:require [clojure.set]
            [clojure.string :refer [blank? join split-lines]]
            [clojure.zip :as zip]))

(def infinity #?(:cljs js/Infinity
                 :clj Double/POSITIVE_INFINITY))

#?(:cljs 
    (defn to-regular-array[arr]
      (IndexedSeq. arr 0 nil)))


(defn =without-keys?
  "Compare two maps exclusing some keys

~~~klipse
  (=without-keys? {:a 1 :b 2 :c 3} {:a 1 :b 5} #{:b :c})
~~~
  "
  [obj-a obj-b keys-list]
  (apply = (map #(apply dissoc % keys-list) [obj-a obj-b])))

(defn vec->map
  "Converts a 2d vec to a hash-map.

~~~klipse
   (vec->map [[:a 1] [:b 2]])
~~~
   "
  [vec]
  (into {} vec))

(defn map-2d-vec [f m]
  "Maps the values of a `2D` vector where each element of the vector is a key-value pair.
`f` is a `1-ary` function that receives the key.

~~~klipse
  (map-2d-vec inc [[:a 1] [:b 2]])
~~~
"
  (map (fn[[k id]] [k (f id)]) m))

(defn map-2d-vec-kv 
  "Maps the values of a `2D` vector where each element of the vector is a key-value pair.
`fk` is a `1-ary` function that receives the key.
`fv` is a `1-ary` function that receives the value.

~~~klipse
    (map-2d-vec-kv name inc [[:a 1] [:b 2]])
~~~
"
 [fk fv m]
  (map (fn[[k id]] [(fk k) (fv id)]) m))

(defn map-object
  "Returns a map with the same keys as `m` and with the values transformed by `f`. `f` is a `1-ary` function that receives the key.

~~~klipse
  (map-object inc {:a 1 :b 2 :c 3})
~~~
  "
  [f m]
  (vec->map (map-2d-vec f m)))


(defn map-object-with-key
  "Returns a map with the same keys as `m` and with the values transformed by `f`. `f` must be a `2-ary` function that receives the key and the value as arguments.

  ~~~klipse
  (map-object-with-key list {:a 1 :b 2 :c 3})
  ~~~
  "
  [f m]
  (into {} (map (fn [[a b]] [a (f a b)]) m)))

(defn map-object-kv
  "Returns a map with the keys mapped by `fk` and the values mapped by `fv`.

~~~klipse
    (map-object-kv name inc {:a 1 :b 2 :c 3})
~~~
"
[fk fv m]
  (vec->map (map-2d-vec-kv fk fv m)))


(defn map-reverse-hierarchy
  "Turns a hash map inside out.
  See:  [here](http://stackoverflow.com/a/23653784/813665)

  ~~~klipse
  (map-reverse-hierarchy {:monday {:banana 2 :apple 3} 
                          :tuesday {:banana 5 :orange 2}})
  ~~~
"
[m]
  (or (apply merge-with conj
         (for [[k1 v1] m [k2 v2] v1] {k2 {k1 v2}}))
      {}))

(defn mean
  "Calculates the mean (a.k.a average) of a sequence of numbers.

  ~~~klipse
  (mean [1 2 10 -1 12.3])
  ~~~
  "
 [x]
  (if (empty? x) 0
    (/ (apply + x)
       (count x))))

(defn sequence->map
  "Converts a sequence into a map where the keys are the indexes of the elements in the sequence.

~~~klipse
  (sequence->map [10 20 30])
~~~
  "
  [s]
  (zipmap (range (count s)) s))

(defn- range-with-end
  ([end] [end (range end)])
  ([start end] [end (range start end)])
  ([start end steps] [end (range start end steps)]))

(defn range-till-end[& args]
  "Like `range` but including the `end`.

~~~klipse
  (range-till-end 10)
~~~

~~~klipse
(range-till-end 10 18)
~~~

~~~klipse
(range-till-end 10 100 5)
~~~

  "
  (let [[end lis] (apply range-with-end args)]
    (concat lis [end])))

(defn append-cyclic[lst a]
  "Appends an element to a list popping out the first element.

  ~~~klipse
  (-> (repeat 3 nil)
      (append-cyclic 1)
      (append-cyclic 2)
      (append-cyclic 3)
      (append-cyclic 4))
  ~~~
  "
  (if (seq lst)
    (concat (rest lst) [a])
    lst))

(defn assoc-cyclic
  "Assoc a key-value pair to a map popping out an element of the map.
  If the key already exists, no element is popped out.
  If `n` is supplied, no elmement is popped out if the map has less than `n` entries.

  ~~~klipse
  (-> {:a 1 :b 2 :c 3}
      (assoc-cyclic :d 4)
      (assoc-cyclic :e 5)
      (assoc-cyclic :f 6)
      (assoc-cyclic :g 7))
  ~~~
  "
  ([coll k v]
   (if (contains? coll k)
     (assoc coll k v)
     (into {} (append-cyclic coll [k v]))))
  ([coll k v n]
   (if (< (count coll) n)
     (assoc coll k v)
     (assoc-cyclic coll k v))))

(defn max-and-min
  "Returns a couple of the `max` and the `min` of a sequence.

  ~~~klipse
  (max-and-min (range 5))
  ~~~
  "
  [x]
  (if (empty? x)
    [0 0]
    ((juxt #(apply max %) #(apply min %)) x)))

(defn compactize-map
  "Removes entries with `nil` values.

  ~~~klipse
  (compactize-map {:a 1 :b nil :c 3})
  ~~~
  "
  [m]
  (into {} (remove (comp nil? second) m)))

(defn filter-map
  "Run a function on the elements of a map and keep only those elements for which the function returns true
  
  ~~~klipse
  (filter-map even? {:a 1 :b 2 :c 3})
  ~~~
  "
  [f m]
  (into {} (filter (comp f val) m)))

(defn abs[x]
  "Absolute value of a number
  
  ~~~klipse
  (map abs (range -5 5))
  ~~~

  "
  (max x (- x)))

(defn nearest-of-ss
  "Returns the nearest number to `x` of a sorted set

  ~~~klipse
  (nearest-of-ss (apply sorted-set (range 5)) 1.2)
  ~~~
  "
  [ss x]
  (let [greater (first (subseq ss >= x))
        smaller (first (rsubseq ss <= x))]
    (apply min-key #(abs (- % x)) (remove nil? [greater smaller]))))

(defn nearest-of-seq
  "Maps each element of `b` to its nearest element in `a`.
  If `a` is empty, returns `b`.

  ~~~klipse
  (nearest-of-seq (range 5) [1.2 3.4 4])
  ~~~
  "
  [a b]
  (if (empty? a)
    b
    (map (partial nearest-of-ss (apply sorted-set a)) b)))

(defn map-to-object
  "Returns a map whose keys are the elements of `lst` and values are mapped by `f`.

  ~~~klipse
  (map-to-object inc (range 5))
  ~~~
  "
  [f lst]
  (zipmap lst (map f lst)))

(defn mapify
  "
  Takes a seq, and returns a map where the keys are the result of applying f to the elements in the seq.
  The result of f should be unique for each element in the seq, otherwise you will loose some data.
  If it is not unique, consider using [group-by](https://clojuredocs.org/clojure.core/group-by).

  ~~~klipse
  (mapify inc (range 5) )
  ~~~
  "
  [f s]
  (zipmap (map f s) s))

(defn map-with-index
  "Maps a sequence to a sequence of maps with index and value

~~~klipse
      (map-with-index [10 20 30] :idx :val)
~~~
  "
  [s idx-key val-key]
  (map-indexed (fn [i v] {idx-key i val-key v}) s))



(defn map-to-object-with-index [f s]
    (into {} (map-indexed #(vector %1 (f %2)) s)))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new nested structure. `keys` is a sequence of keys. Any empty maps that result will not be present in the new structure. See [assoc-in](https://clojuredocs.org/clojure.core/assoc-in)

  ~~~klipse
  (dissoc-in {:a 1 :b 2} [:b])
  ~~~

  ~~~klipse
  (dissoc-in {:a {:b 2 :B 3} :c 3} [:a :b])
  ~~~

  ~~~klipse
  (dissoc-in {:a {:b 2} :c 3} [:a :b])
  ~~~
  "
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn split-by-predicate
  "Splits a collection to items where the separator is a repetition of at least n elements that satisfy `pred`.

  Inspired by: [this question](http://stackoverflow.com/a/23555616/813665).

  ~~~klipse
  (split-by-predicate (shuffle (range 30)) even? 2)
  ~~~
  "
[coll pred n] 
  (let [part  (partition-by  pred coll)
        ppart (partition-by (fn [x] (and
                                      (>= (count x) n) 
                                      (every? pred x))) part)]
        (map #(apply concat %) ppart)))

(defn positions
  "Receives a collection of lengths and returns a list of start and end positions. Options:
  * `max-val`: (default `infinity`) - max value for `end`
  * `first-val`: (default 0) - first value of `start`

  ~~~klipse
  (positions '(10 10 20) :first-val 100 :max-val 137)
  ~~~
  
  "
[coll-of-lengths & {:keys [max-val first-val] :or {max-val infinity first-val 0}}]
  (let [end-pos (rest (reductions + first-val coll-of-lengths))
        start-pos (concat [first-val] end-pos)]
    (map #(list (min max-val %1) (min max-val %2)) start-pos end-pos)))

(defn submap?
  "Checks if `m1` is a submap of `m2`.
  Map `m1` is a submap of `m2` if all key/value pairs in `m1` exist in `m2`.

  ~~~klipse
  (submap? {:a 1} {:a 1 :b 2})
  ~~~

  ~~~klipse
  (submap? {:a 1} {:a 1 :b 2 :c nil})
  ~~~
  "
  [m1 m2]
  (= m1 (select-keys m2 (keys m1))))

(defn subsequence
  "
  Returns a lazy subsequence of `coll`, starting at `start, ending at `end` (not included).

  ~~~klipse
  (subsequence (range) 10 20)
  ~~~
  "
  [coll start end]
  (->> (drop start coll)
       (take (- end start))))

(defn split-by-predicate-opt [coll pred n d]
  (let [lengths (map #(* d %) (map count (split-by-predicate (take-nth d coll) pred (/ n d))))
        pos (positions lengths :max-val (count coll))]
    pos))

(defn index-of [s element]
  (or (ffirst (filter #(= (second %) element) (map-indexed #(vector %1 %2) s)))
      -1))

(defn display-sequence [long-seq short-seq value abs-step]
  (let [old-step (- (second short-seq) (first short-seq))
        step (* (- (second long-seq) (first long-seq)) abs-step)
        position-in-old-sequence (/ (- value (first short-seq)) old-step)]
    (cond
      (<= 0 position-in-old-sequence 4) (range (- value (* step position-in-old-sequence)) (+ value (* step (- 5 position-in-old-sequence))) step)
      (= position-in-old-sequence 5) (range (- value (* step (- position-in-old-sequence 1))) (+ value step) step)
      (empty? short-seq) (range (- value step) (+ value (* 4 step)) step)
      :else (range value (+ value (* 5 step)) step))))

(defn highest-below-y [m v]
  (second (last (sort-by first (group-by second (filter (fn [[x y]] (<= y v)) m))))))

(defn lowest-above-y [m v]
  (second (first (sort-by first (group-by second (filter (fn [[x y]] (>= y v)) m))))))

(defn highest-below-x [m v]
  (second (last (sort-by first (group-by first (filter (fn [[x y]] (<= x v)) m))))))

(defn lowest-above-x [m v]
  (second (first (sort-by first (group-by first (filter (fn [[x y]] (>= x v)) m))))))

(defn find-keys-with-values-in [m s]
  (filter (comp s m) (keys m)))

(defn replace-keys [coll key-map]
  (zipmap (map #(get key-map % %) (keys coll)) (vals coll)))

(defn find-keys-with-value [m v]
  (find-keys-with-values-in m #{v}))

(defn linear-y [x x1 y1 x2 y2]
  (+  y1 (/ (* (- y2 y1) (- x x1)) (- x2 x1))))

(defn log-x-linear-y [x x1 y1 x2 y2]
  (+ y1 (/ (* (- y2 y1) (- (Math/log x) (Math/log x1))) (- (Math/log x2) (Math/log x1)))))

(defn linear-y-func [{:keys [x y] :as axes}]
  ;Add more options if needed
  (case [x y]
    [:linear :linear] linear-y
    [:log :linear] log-x-linear-y
    linear-y))

(defn interpolate-linear-y [m x 
  & {:keys [interpolate? axes] :or {interpolate? (constantly true) axes {:x :linear :y :linear}}}]
    (or (get m x)
        (let [[x-below y-below] (last (sort (highest-below-x m x)))
              [x-above y-above] (first (sort (lowest-above-x m x)))]
        (when (and x-below x-above (interpolate? x-below x-above))
          ((linear-y-func axes) x x-below y-below x-above y-above)))))

(defn linear-x [y x1 y1 x2 y2]
  (+ x1 (/ (* (- x2 x1) (- y y1)) (- y2 y1))))

(defn linear-x-func [{:keys [x y] :as axes}]
  ;Add more options if needed
  (case [x y]
    [:linear :linear] linear-x
    linear-x))

(defn below-and-above-y [y [x1 y1] [x2 y2]]
  (when (or (< y1 y y2) (> y1 y y2)) [[x1 y1] [x2 y2]]))

(defn find-below-and-above-y [m y]
  (as->
    (map vec m) $
    (sort-by first $)
    (map (partial below-and-above-y y) $ (rest $))
    (remove nil? $)))

(defn calc-interpolated-values [m y interpolate? axes]
  (as->
    (fn [[[x-below y-below] [x-above y-above]]]
      (when (and y-below y-above (interpolate? y-below y-above))
        ((linear-x-func axes) y x-below y-below x-above y-above))) $
    (keep $ (find-below-and-above-y m y))))

(defn min-coll [coll]
  (when-not (empty? coll)
    (apply min coll)))

(defn interpolate-linear-x
  "Returns the interpolated x for a given y acording to the select-func thats passed

- `:interpolate?` -  a predicate for deciding eather to calc th interpolation or not.
-   `:axes` -  a map that defines what are the axes scales
-   `:select-func` - what functionality to use if there are multiple interpolated values

~~~klipse
  (interpolate-linear-x {10 30 20 50 70 60} 32)
~~~
  "
  [m y
  & {:keys [interpolate? axes select-func]
     :or {interpolate? (constantly true) axes {:x :linear :y :linear} select-func min-coll}}]
  (let [values (find-keys-with-value m y)
        interpolated-values (calc-interpolated-values m y interpolate? axes)]
    (select-func (concat values interpolated-values))))

(defn linear-equation [x1 y1 x2 y2]
  (let [a (/ (- y1 y2) (- x1 x2))
        b (- y1 (* a x1))]
    [a b]))

(defn log-x-linear-equation [x1 y1 x2 y2]
  (let [a (/ (- y1 y2) (- (Math/log x1) (Math/log x2)))
        b (- y1 (* a (Math/log x1)))]
    [a b]))

(defn linear-equation-func [{:keys [x y] :as axes}]
  ;Add more options if needed
  (case [x y]
    [:linear :linear] linear-equation
    [:log :linear] log-x-linear-equation
    linear-equation))

(defn intersection-point [a1 b1 a2 b2]
  (let [x (/ (- b2 b1) (- a1 a2))
        y (+ ( * a1 x) b1)]
    [x y]))

(defn log-x-intersection-point [a1 b1 a2 b2]
  (let [x (/ (- b2 b1) (- a1 a2))
        y (+ ( * a1 x) b1)]
    [(Math/exp x) y]))

(defn intersection-point-func [{:keys [x y] :as axes}]
  ;Add more options if needed
  (case [x y]
    [:linear :linear] intersection-point
    [:log :linear] log-x-intersection-point
    intersection-point))

(defn intersection-point-from-2-lines-points [[ax1 ay1 ax2 ay2] [bx1 by1 bx2 by2]
  & {:keys [axes] :or {axes {:x :linear :y :linear}}}]
  (let [[a1 b1] ((linear-equation-func axes) ax1 ay1 ax2 ay2)
        [a2 b2] ((linear-equation-func axes) bx1 by1 bx2 by2)
        [x y] ((intersection-point-func axes) a1 b1 a2 b2)]
    [x y]))


(defn select-keys-in-order
  "Thanks [Jay Fields](http://blog.jayfields.com/2011/01/clojure-select-keys-select-values-and.html)"
  [m keyseq]
  (map m keyseq))

(defn select-vals [map keyseq]
  (vals (select-keys map keyseq)))

(defn select-vals-in-order
  "Thanks [Jay Fields](http://blog.jayfields.com/2011/01/clojure-select-keys-select-values-and.html)"
  [map ks]
  (reduce #(conj %1 (map %2)) [] ks))

(defn flatten-keys* [a ks m]
  (if (map? m)
    (if (seq m)
      (reduce into (map (fn [[k v]] (flatten-keys* a (conj ks k) v)) (seq m)))
      {})
    (assoc a ks m)))

(defn flatten-keys "Thanks to [Jay Fields](http://blog.jayfields.com/2010/09/clojure-flatten-keys.html)"
  [m] (flatten-keys* {} [] m))

(defn unflatten-keys [m]
  (reduce-kv (fn [a b c] (assoc-in a b c)) {} m))

(defn take-from-map 
  "Creates a map with n leaves which are nested values of m.

      (= n (count (flatten-keys (take-from-map n m)))))))"
  [n m]
  (->> m
       flatten-keys
       (take n)
       (into {}) 
       unflatten-keys))

(defn recursive-vals [m]
  (when m (vals (flatten-keys m))))

(defn sort-keys-by [a-func a-map]
  (map first (sort-by a-func a-map)))

(defn deep-merge* [& maps]
  (let [f (fn [old new]
            (if (and (map? old) (map? new))
              (merge-with deep-merge* old new)
              new))]
    (if (every? map? maps)
      (apply merge-with f maps)
      (last maps))))

(defn deep-merge [& maps]
  (let [maps (filter identity maps)]
    (assert (every? map? maps))
    (apply merge-with deep-merge* maps)))

(defn branches-and-leaves 
  "Returns all branches and leaves off a nested map object.

~~~klipse
(branches-and-leaves {:a {:b 1 :c {:d 2}} :e 3})
~~~
  "
  [m]
  (as-> (tree-seq coll? #(if (map? %) (vals %) %) m) $
        (group-by coll? $)
        (assoc $ true (or (get $ true) []))
        (assoc $ false (or (get $ false) []))
        (clojure.set/rename-keys $ {true :branches false :leaves})))

(defn filter-branches
  "Filters branches of a (nested) map `m` according to a predicate `m`.

~~~klipse
(filter-branches {:x {:id 19 :b 1 :c {:id 2}} :e 3} :id)
~~~
  "
  [m p]
  (->> (branches-and-leaves m)
       :branches
      (filter p)))

(defn out-of-bound?
  "check if index `idx` is in range of vector `v`. More efficient than ```(get v idx)```"
  [v idx]
   (or (<= (count v) idx) (> 0 idx)))

(defn partition-between
  "Splits a collection between two items according to predicate `pred` - which means split the sequence on breaking point.

  See: [here](http://stackoverflow.com/questions/23207490/partition-a-seq-by-a-windowing-predicate-in-clojure)

  For instance, split each time the series stop being ascending:

~~~klipse
(partition-between > [1 2 4 9 8 7 6 5 1 2 4 5 11])
~~~
"
  [pred coll]
    (let [switch (reductions not= true (map pred coll (rest coll)))]
      (map (partial map first) (partition-by second (map list coll switch)))))


(defn seqify
  "Ensure `s` is a sequence: if `s` is a sequence returns it; otherwise returns (s)"
  [s]
  (if (seq? s) s (list s)))

(defn edn-zip [root]
  (zip/zipper
    #(or (vector? %) (map? %) (seq? %))
    (fn [node]
      (cond
        (vector? node) (vec node)
        (map? node)    (vec node)
        (seq? node)    (seq node)))

    (fn [node children]
      (->
        (cond
          (vector? node) (vec children)
          (map? node)    (into {} children)
          (seq? node)    children)
        (with-meta (meta node))))
    root))

(defn- loc-my-replace [smap loc]
  (if-let [[_ [replacement & values]] (find smap (zip/node loc))]
    (as-> loc $
      (zip/replace $ replacement)
      (reduce (fn [agg v] (zip/insert-right agg v)) $ (reverse values)))
    loc))

(defn my-replace
  "Recursively transforms `form` by replacing keys in `smap` with their
  values, spliced. The values in `smap` must be sequences. Like clojure.walk/prewalk-replace but supports list in values."
  [smap form]
  {:pre [(every? seq? (vals smap))]}
  (loop [loc (edn-zip form)]
    (if (zip/end? loc)
      (zip/root loc)
      (recur (zip/next (loc-my-replace smap loc))))))

(defn fix-blank-lines
  "Removes blank lines from the begining and from the end (not from the middle)

~~~klipse
  ; we use (char 10) for end-of-line due to technical issues with string manipulation with `codox`
  (let [lines (clojure.string/join (char 10) [\"  \", \"aa\", \"  \", \"bb\", \" \t  \"])]
  (fix-blank-lines lines))
~~~
  "
  [s]
  (->> s
    split-lines
    (drop-while blank?)
    reverse
    (drop-while blank?)
    reverse
    (join "\n")))

(defn remove-blank-lines
"Removes blank lines.
~~~klipse
  ; we use (char 10) for end-of-line due to technical issues with string manipulation with `codox`
  (let [lines (clojure.string/join (char 10) [\"  \", \"aa\", \"  \", \"bb\", \" \t  \"])]
  (remove-blank-lines lines))
~~~
  "
  [s]
  (->> s
    split-lines
    (remove blank?)
    (join "\n")))

#?(:cljs
    (defn compact 
      "(clojurescript only)

      Compacts an expression by taking only the first `max-elements-in-coll` from collections and first `max-chars-in-str` from strings. Functions are displayed as \"lambda()\".

It works recursively. It is useful for logging and reporting.

Default settings:

- `max-elements-in-coll` 10
- `max-chars-in-str` 20

~~~klipse
(compact {:infinite-list (range)
          :long-str \"a very very very very long string - too long to be true\"
                                                      :long-map (zipmap (range 100) (range 100))
                                                        :function #(+ 1 2)})
~~~
                  "
      [x & {:keys [max-elements-in-coll max-chars-in-str] :or {max-elements-in-coll 10 max-chars-in-str 20} :as args}]
      (cond
        (= x true) x
        (= x false) x
        (nil? x) x
        (keyword? x) x
        (number? x) x
        (string? x) (subs x 0 max-chars-in-str)
        (map? x) (take-from-map max-elements-in-coll (map-object #(compact % args) x))
        (seqable? x) (take max-elements-in-coll (map #(compact % args) x))
        (array? x) (str "***[" (type x) "]***")
        (= js/Function (type x)) "lambda()"
        (instance? js/Object x) (str "***[" (subs (str (type x)) 0 15) "]***")
        :else (str "***[" (type x) "]***")))
    )


