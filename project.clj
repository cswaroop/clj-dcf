(defproject clj-dcf "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main clj-dcf.core  
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [com.oracle/ojdbc6 "12.1.0.1-atlassian-hosted"]
                 [org.clojure/tools.cli "0.3.5"]]
  :repositories {"oracle driver" "https://maven.atlassian.com/3rdparty/"}
  )
