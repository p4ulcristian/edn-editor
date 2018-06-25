(ns edn-editor.prod
  (:require [edn-editor.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
