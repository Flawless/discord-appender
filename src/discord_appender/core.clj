(ns discord-appender.core
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- trunc
  [s n]
  (subs s 0 (min (count s) n)))

(defn discord-appender
  "Returns discord appender. Required params:
    `hook-id` - discord webhook id
    `token`   - discord hook token
  https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks"
  [& [{:keys [token hook-id username]
       :or {username "good bot"}}]]
  (let [webhook-url (format "https://discord.com/api/webhooks/%s/%s" hook-id token)]
    {:enabled?   true
     :async?     true
     :output-fn :inherit
     :fn (fn [data]
           (let [[first-line :as message] (-> data :output_ force str/split-lines)
                 n 2000]
             (try
               (client/post webhook-url {:form-params {:content (trunc first-line n)
                                                       :username username}})
               (when (< (count message) 2000)
                 (client/post
                   webhook-url {:multipart
                                [{:name "username" :content username}
                                 {:name "content.txt" :content (-> (.getBytes (str/join "\n" message)
                                                                     "UTF-8")
                                                                 io/input-stream)}]}))
               (catch Exception e
                 (println :error e "can't append to discord")))))}))
