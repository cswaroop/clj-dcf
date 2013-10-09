(defproject clj-dcf "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main clj-dcf.core  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [local/ojdbc6 "11.2.0.3"]
                 [org.clojure/java.jdbc "0.3.0-alpha4"]
                 ]
  )


; mvn install:install-file -X -DgroupId=local -DartifactId=ojdbc6 -Dversion=11.2.0.3 -Dpackaging=jar -Dfile=D:/ojdbc6.jar -DgeneratePom=true