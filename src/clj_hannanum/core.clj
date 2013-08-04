(ns clj-hannanum.core
  (:gen-class)
  (:import (kr.ac.kaist.swrc.jhannanum.hannanum Workflow)
           (kr.ac.kaist.swrc.jhannanum.plugin.MajorPlugin.MorphAnalyzer.ChartMorphAnalyzer ChartMorphAnalyzer)
           (kr.ac.kaist.swrc.jhannanum.plugin.MajorPlugin.PosTagger.HmmPosTagger HMMTagger)
           (kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.MorphemeProcessor.SimpleMAResult09 SimpleMAResult09)
           (kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.MorphemeProcessor.SimpleMAResult22 SimpleMAResult22)
           (kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.MorphemeProcessor.UnknownMorphProcessor UnknownProcessor)
           (kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.InformalSentenceFilter InformalSentenceFilter)
           (kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.SentenceSegmentor SentenceSegmentor)
           (kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PosProcessor.SimplePOSResult09 SimplePOSResult09)
           (kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PosProcessor.SimplePOSResult22 SimplePOSResult22))
  (:use ring.adapter.jetty
        ring.util.response
        ring.middleware.params
        ring.middleware.json
        ))

(def workflow (Workflow.))

(def conf-dir "conf/")
(def plugin-dir (str conf-dir "plugin/"))
(def major-plugin-dir (str plugin-dir "MajorPlugin/"))

(defn setup-phase1 []
  (.appendPlainTextProcessor workflow (SentenceSegmentor.) nil)
  (.appendPlainTextProcessor workflow (InformalSentenceFilter.) nil))

(defn setup-phase2 []
  (.setMorphAnalyzer workflow
                     (ChartMorphAnalyzer.)
                     (str major-plugin-dir
                          "MorphAnalyzer/ChartMorphAnalyzer.json"))
  (.appendMorphemeProcessor workflow
                            (UnknownProcessor.) nil))

(defn setup-phase3 []
  (.setPosTagger workflow
                 (HMMTagger.)
                 (str major-plugin-dir
                      "PosTagger/HmmPosTagger.json")))

(defn analyzed-data-to-json [analyzed-str]
  (map (fn [[k v]]
         {:before k
          :parsed (map (fn [w]
                         (zipmap 
                          [:str :type]
                          (clojure.string/split
                           w
                           #"\/")))
                       (clojure.string/split v #"\+"))})
       (partition-all 2
                      (filter 
                       #(not (.isEmpty %))
                       (clojure.string/split analyzed-str #"\s+")))))

(defn analyze [korean]
  (.analyze workflow korean)
  (.getResultOfDocument workflow))

(defn handler [{params :params}]
  {:status 200
   :headers {"Content-Type"
             "text/html;charset=utf-8"}
   :body (if (nil? (params "text"))
           "{}"
           (analyzed-data-to-json
            (analyze (params "text"))))})

(defn -main []
  (do
    (try
      (println "Workflow Setup Phase 1")
      (setup-phase1)
      (println "Workflow Setup Phase 2")
      (setup-phase2)
      (println "Workflow Setup Phase 3")
      (setup-phase3)
      (.activateWorkflow workflow true)
      (catch Exception e
        (println (str "Exception: " e))))
    (run-jetty
     (wrap-json-response
      (wrap-params handler :encoding "UTF-8"))
     {:port 8080})))