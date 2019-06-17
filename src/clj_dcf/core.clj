(ns clj-dcf.core
  (:gen-class)
  (:require [clojure.java.jdbc :as j]
            [clojure.tools.cli :refer [cli]])
  (:import [java.util Properties]
           [oracle.jdbc   OracleConnection]
           [oracle.jdbc.dcn DatabaseChangeEvent]
           [oracle.jdbc.dcn DatabaseChangeListener]
           [oracle.jdbc.dcn DatabaseChangeRegistration]
           [oracle.jdbc OracleStatement]
           [java.util Arrays]))

;; grant change notification to scott

(def db
  {:auto-commit        true
   :minimum-idle       10
   :maximum-pool-size  1
   :pool-name          "db-pool"
   :adapter            "oracle"
   :password           "1234"
   :driverType         "thin" 
   :register-mbeans    false
   :classname "oracle.jdbc.OracleDriver"
   :subprotocol "oracle"
   :subname "thin:@10.105.87.4:1521/DEVDB482"
   :user "system"})

(defn- parse-arguments 
  [args]
  (let [[options args banner] 
        (cli args
             ["-l" "--local-port" "The local port that will be used for the callback" :default "6666"]
             ["-q" "--query" "The query used for registration" :default "select id from T1_OS_ADMIN_D.stemp WHERE ID  = 1"]
             ["-h" "--help" "Show help" :default false :flag true])]
    ;; Handle --help
    (when (:help options)  
      (println banner)
      (System/exit 0))
    [options args banner]))

(defn- create-db-spec 
  "Returns a properly formated db spec - see clojure.java.jdbc/get-connection documentation"
  []
  db)

(defn- set-notification-options 
  "Set the connection options, returns a property object with the selected options"
  [options]
  (let [prop (new Properties)]
    (doto prop
      (.setProperty OracleConnection/DCN_NOTIFY_ROWIDS "true")
      (.setProperty OracleConnection/DCN_QUERY_CHANGE_NOTIFICATION "true")
      (.setProperty OracleConnection/NTF_LOCAL_TCP_PORT (:local-port options))
      ;(.setProperty OracleConnection/NTF_LOCAL_HOST "10.40.84.147")
      ) 
    prop))

(defn- set-registration-statement 
  "Returns a statement that is linked to the registratation"
  [conn dcr]
  (let [stmt (.createStatement conn)]
    (.setDatabaseChangeRegistration stmt dcr) ; add the registration to the statement
    stmt))

(defn- add-shutdown-hook
  "House-keeping on Control-C: unregister the DCN from the connection"
  [conn dcr]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn [] 
                               (.unregisterDatabaseChangeNotification conn dcr)
                               (.close conn)))))
(defn -main
  "Oracle Database Change Notification JDBC demo"
  [& args]
  ;; work around dangerous default behaviour in Clojure (NB: this comment is from leiningen)
  ;(alter-var-root #'*read-eval* (constantly false))

  (let [[options args banner] (parse-arguments args)
        db (create-db-spec)
        conn (j/get-connection db)                           ; the connection to the database
        prop (set-notification-options options)              ; properties used to specify registration options
        dcr (.registerDatabaseChangeNotification conn prop)  ; create the notification register with the correct option on the right connection
        stmt (set-registration-statement conn dcr)           ; this statement is "linked" to to registration
        rs (.executeQuery stmt (:query options))]            ; specify the tables by using a query
    
    ;; Add the listener - using reify but proxy works as well of course
    (.addListener dcr
                  (reify DatabaseChangeListener
                    (onDatabaseChangeNotification [_ evt]
                      (println (.toString evt)))))
    
    ;; Add a shutdown hook to remove registration and close connection
    (add-shutdown-hook conn dcr)
    
    (while (.next rs)) ; Loop through the results - all examples do this, although it works without it. Leaving it as black magic.
    
    (.close rs)
    (.close stmt)

    ;; The main thread only exits after the JDBC connection is closed, so no need for a run loop
    ;; It will keep on listening until Control-C is pressed 
    
    ;; Debug output
    (println "> Registered Tables: " (Arrays/toString (.getTables dcr)))
    (println "> DCN Registration ID: " (.getRegistrationId dcr))
    (println "> Waiting for DCN events...")))
