(ns re-frisk.debugger
  (:require [hiccups.runtime :as h]
            [re-frisk.data :refer [re-frame-data]]
            [reagent.core :as r]
            [cognitect.transit :as t]
            [datafrisk.core :as f])
  (:require-macros [hiccups.core :refer [html]]
                   [reagent.ratom :refer [reaction]]))

(defonce deb-data (r/atom {:w-c true}))
(defonce rdr (t/reader :json))
(defonce wr (t/writer :json))

(defn debugger-shell []
  (let [expand-by-default (reduce #(assoc-in %1 [:data-frisk %2 :expanded-paths] #{[]}) {} (range 1))
        state-atom (r/atom expand-by-default)]
    (fn []
      [:div {:style {:backgroundColor "#FAFAFA"
                     :fontFamily "Consolas,Monaco,Courier New,monospace"
                     :fontSize "12px"
                     :height "100%"
                     :width "100%"}}
       [:div
        (map-indexed (fn [id x]
                       ^{:key id} [f/Root x id state-atom]) [(:data @deb-data)])]])))

(defn run [data]
  (swap! deb-data assoc :data (t/read rdr data))
  (when-not (:rendered @deb-data)
    (do
      (swap! deb-data assoc :rendered true)
      (r/render [debugger-shell] (js/document.getElementById "app")))))

(defn debugger-page [src]
  [:html
   [:head
    [:title "re-frisk debugger"]
    [:meta {:charset "UTF-8"}]
    [:meta
     {:content "width=device-width, initial-scale=1", :name "viewport"}]]
   [:body  {:style {:margin "0" :padding "0"}};}}
    [:div#app {:style {:height "100%" :width "100%"}}
     [:h2 "re-frisk debugger"]
     [:p "ENJOY!"]]]
   [:script {:type "text/javascript", :src src}]])

(defn i-h []
  (swap! deb-data assoc :w-c (.-closed (:w @deb-data)))
  ((:f @deb-data) (:w @deb-data) (t/write wr @(:app-db @re-frame-data))))

(defn open-debugger-window []
  (let [w (js/window.open "" "Debugger" "width=500,height=400,resizable=yes,scrollbars=yes,status=no,directories=no,toolbar=no,menubar=no")
        d (.-document w)]
    (swap! deb-data assoc :w w)
    (.open d)
    (.write d (html (debugger-page (:p @deb-data))))
    (.close d)
    (aset w "onload" (fn [] (js/setInterval i-h 100)))))

(defn register [p f]
  (swap! deb-data assoc :p p)
  (swap! deb-data assoc :f f))
