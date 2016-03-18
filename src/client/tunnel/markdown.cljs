(ns tunnel.markdown)

(.setOptions js/marked
  #js {:header false
       :sanitize true
       :breaks true
       :highlight (fn [code]
                    (prn "---")
                    (-> code
                      js/hljs.highlightAuto
                      .-value))})

(defn markdown
  [text]
  (if text
    (js/marked text)
    (do
      (js/console.error "marked parsing nil")
      "NIL CONTENT")))
