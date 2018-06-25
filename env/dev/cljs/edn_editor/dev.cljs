(ns ^:figwheel-no-load edn-editor.dev
  (:require
    [edn-editor.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
