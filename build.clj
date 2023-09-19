(ns build
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(def lib 'org.last-try/discord-appender)
(def dirty? (not (str/blank? (b/git-process {:git-args "status --porcelain"}))))
(def version (cond-> (or (b/git-process {:git-args "describe --tags --abbrev=0"})
                         (b/git-process {:git-args "rev-parse --short HEAD"}))
               dirty? (str "-dirty")))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-base (format "target/%s.jar" (name lib)))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file})
  (b/copy-file {:src jar-file :target jar-base}))
