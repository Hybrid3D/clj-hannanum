(defproject clj-hannanum "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["lib/jhannanum.jar"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 ;[ring "1.2.0"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-json "0.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]]
  :jvm-opts ["-Dfile.encoding=utf-8"]
  :main clj-hannanum.core)
