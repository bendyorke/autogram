(ns autogram.associate)

(enable-console-print!)

(defn find-in
  "Returns the integer of a value in a collection"
  [coll n]
  (when-let [indices (keep-indexed #(when (= n %2) %1) coll)]
    (first indices)))

(defn associate
  "Given two strings, return a map of the letters
  corresponding postions
  i.e. {0 1 2 2}"
  [a b]
  (let [letters-in-a  (into [] a)
        letters-in-b  (into [] b)]
    (loop [index-a 0
           options letters-in-b
           result  {}]
      (let [letter (get letters-in-a index-a)]
        (if (or (not letter) (empty? (filter identity options)))
          result
          ;; If letter is present in the remaining options,
          ;; recur with the indices shifted into the result
          (if-let [index-b (find-in options letter)]
            (recur
              (inc index-a)
              (assoc options index-b nil)
              (assoc result index-a index-b))
            (recur
              (inc index-a)
              options
              result)))))))
