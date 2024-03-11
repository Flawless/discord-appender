(ns build
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(def lib 'io.github.flawless/discord-appender)
(def dirty? (not (str/blank? (b/git-process {:git-args "status --porcelain"}))))
(def version (cond-> (or (b/git-process {:git-args "describe --tags --abbrev=0"})
                         (b/git-process {:git-args "rev-parse --short HEAD"}))
               dirty? (str "-dirty")))
(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))
(def jar-base (format "target/%s.jar" (name lib)))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"})
  (b/delete {:path "pom.xml"})
  (b/delete {:path "pom.properties"}))

(defn jar [_]
  (println :building version)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/write-pom {#_#_:class-dir class-dir
                :target "."
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["src"]
                :pom-data [[:licenses
                            [:license
                             [:name "Apache License 2.0"]
                             [:url "http://www.apache.org/licenses/"]
                             [:distribution "repo"]]]]})
  (b/jar {:class-dir class-dir
          :jar-file jar-file})
  (b/copy-file {:src jar-file :target jar-base})
  (println :built jar-file jar-base))
