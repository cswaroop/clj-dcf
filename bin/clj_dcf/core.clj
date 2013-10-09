(ns clj-dcf.core)


(require '[clojure.java.jdbc :as j]
         '[clojure.java.jdbc.sql :as s])

(def db {:classname "oracle.jdbc.driver.OracleDriver"
         :subprotocol "oracle"
         :subname "thin:@192.168.1.51:1521/AGS8XQA"
         :user "GRAINCORP_APP_7072"
         :password "GRAINCORP_APP_7072"})
(defn -main
  [(with-connection db
    (with-query-results rs ["select * from pcm_physical_contract_main"]
      (dorun (map #(println :internal_contract_ref_no %)) rs)))
   ]
  )
