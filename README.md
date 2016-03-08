# 如何启动
## `cider-jack-in` 启动REPL
## 启动, 重启, 停止所有组件
```clojure
  ;; 启动
  (go)

  ;; 重启
  (reset)

  ;; 停止
  (stop)
```
## CLJ和CLJS的REPL切换
```clojure
  ;; CLJ->CLJS
  (in-ns 'user)
  (cljs-repl)

  ;; CLJS->CLJ
  :cljs/quit
```
