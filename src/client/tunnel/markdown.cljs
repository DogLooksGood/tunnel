(ns tunnel.markdown)

(.setOptions js/marked
  #js {:highlight (fn [code]
                    (prn "---")
                    (-> code
                      js/hljs.highlightAuto
                      .-value))})

(defn markdown
  [text]
  (js/marked text))
