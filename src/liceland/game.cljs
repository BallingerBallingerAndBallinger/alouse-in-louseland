(ns liceland.game)

(defonce app (.getElementById js/document "app"))
(defonce width (.getAttribute app "width"))
(defonce height (.getAttribute app "height"))
(declare forest)
(declare mosquito-dialog)
(declare caterpillar-dialog)

;; Sprites permit the following properties
;; sound: URL of a sound file to play when the sprite is visible (looping)
;; positionX: The x position of the sprite
;; positionY: The y position of the sprite
;; image:     URL of an image file for the sprite
;; click:     A symbol pointing to a scene to jump to when the sprite is clicked
;; scale:     A scaling factor

(defn clickable [sprite target]
  (cljs.core/merge sprite {:click target}))

(defn set-state [key value]
  #(assoc % key value))

(def devil-phrases
  ["\"Are you sure you are hear?\""
   "\"I tend to wax on indefinitely.\""
   "\"Let me know if you need a hand later.\""
   "\"Cutting down trees isn't very echo-logical, is it?\""
   "\"The forest alouse few to find this cave.\""
   "\"I'm not nearly as buzzy as you seem to be!\""
   "How many layers of eggs are there now?\""
   "\"Do you notice anything eary about this place?\""
   "\"I haven't a-loud word-play hear for fourty ears.\""])

(def hand
  {:positionX (* 0 width)
   :positionY (* -1 height)
   :scale 2
   :image "images/hand.png"})

(def gasoline
  {:positionX (* 0.7 width)
   :positionY (* 0.6 height)
   :image "images/gas.png"})

(def title
  {:positionX 0
   :positionY 0
   :image "images/title-screen.png"})

(def match
  {:positionX (* 0.1 width)
   :positionY (* 0.5 height)
   :image "images/match.png"})

(def sign
  {:positionX 195
   :positionY 132
   :image "images/well-sign.png"})

(def destroyed-nest
  {:positionX (* 0.25 width)
   :positionY (* 0.5 height)
   :image "images/louse-nest-destroyed.png"})

(def louse-nest
  {:positionX (* 0.25 width)
   :positionY (* 0.5 height)
   :image "images/louse-nest.png"})

(def louse-eggs
  {:positionX (* 0.25 width)
   :positionY (* 0.5 height)
   :image "images/louse-eggs.png"})

(def destroyed-eggs
  {:positionX (* 0.25 width)
   :positionY (* 0.5 height)
   :image "images/louse-eggs-destroyed.png"})

(def well
  {:positionX (* 0.7 width)
   :positionY (* 0.4 height)
   :image "images/well.png"})

(def well-no-rope
  {:positionX (* 0.7 width)
   :positionY (* 0.4 height)
   :image "images/well-no-rope.png"})

(def post
  {:positionX 120
   :positionY 0
   :image "images/post.png"})

(def rope
  {:positionX 57
   :positionY 81
   :scale 0.5
   :image "images/rope.png"})

(def fallen
  {:positionX (* 0.7 width)
   :positionY (* 0.3 height)
   :image "images/fallen.png"})

(def caterpillar
  {:positionX (* 0.60 width)
   :positionY (* 0.12 height)
   :image "images/Caterpillar.png"})

(def devilwig
  {:positionX (* 0.24 width)
   :positionY (* 0.12 height)
   :scale 3
   :image "images/devilwig.png"})

(def mosquito
  {:sound "audio/mosquito.mp3"
   :positionX (* 0.7 width)
   :positionY (* 0.4 height)
   :image "images/mosquito-flit1.png"})

;; Demonstrating one way of building things up from simpler pieces.
(def larger-mosquito
  (cljs.core/merge mosquito
                   {:positionX (* 0.6 width)
                    :positionY (* 0.5 height)
                    :scale 2 }))

(def largest-mosquito
  (cljs.core/merge mosquito
                   {:positionX (* 0.1 width)
                    :positionY (* -0.4 height)
                    :scale 8}))

(def axe
  {:positionX (* 0.34 width)
   :positionY (* 0.65 height)
   :image "images/axe.png"})

(defn cliffside-sprites [state]
  (cond
    (:babies state) [ (clickable post :ear) (clickable rope :ear) ]
    (:rumbled state) [ (clickable post :check) (clickable rope :check) ]
    (:tied state) [ (clickable post :rumbling) (clickable rope :rumbling) ]
    (:rope state)  [ (clickable post :roped) ]
    :default [ (clickable post :help) ]))

;; Scenes permit the following properties
;; background:  URL of a background for the scene
;; description: Text that appears beneath a scene
;; music:       Music to play while you're in the scene
;; right:       A symbol pointing to a scene to jump to when right is clicked
;; left:        A symbol pointing to a scene to jump to when left is clicked
;; forward:     A symbol pointing to a scene to jump to when forward is clicked
;; back:        A symbol pointing to a scene to jump to when back is clicked
;; sprites:     An array of sprites present in the scene
;; sound:       A sound to play when you enter a scene
;; update:      A function to update the state when a scene is entered.... AND FOR THE LOVE OF GOD

;; Be careful with the state...
;; It should be used extremely sparingly
;; e.g. It's easy to subtly break live-reloading by getting something stuck in your state.
;; e.g. It's easy to get into an "unreachable state" then keep developing worlds without realizing that no player will ever
;;      have the same state that you do...
;; e.g. Asset-preloading depends on sprites, backgrounds, music, and sounds to be reachable with an empty state.
;; However, with all of those warnings, having access to it give you great power, and with great power comes...! (Mayhem!)
;; You will find that things are happy and easy and everything is great and there are rainbows and unicorns
;; unless you ABUSE THE STATE THEN DEVIL HIMSELF WILL CRAWL SCREAMING OUT OF YOUR BUTTHOLE.

(defn scenes [state]
  (merge (mosquito-dialog state)
         (forest state)
         (caterpillar-dialog state)))

(defn forest [state]
  {:head-west
   {:background "images/forest9.png"
    :description "Nothing but trees"
    :music "audio/liceland.mp3"
    :forward :clearing
    :right :head
    :left :head-east }

   :title
   {:background "images/title-screen.png"
    :sprites [ (clickable title :head) ]}
   
   :clearing
   {:background "images/forest7.png"
    :sound (cond
             (:rumbled state) "audio/scratch.mp3"
             :default "audio/insect.mp3")
    :description (cond
                   (:rumbled state) nil
                   (:eggs state) "You will meet them soon. Wait and prepare."
                   (not (:chopped state)) "A nice little clearing")
    :sprites (cond
               (:match state)      [ destroyed-nest ( clickable destroyed-eggs :nooooo ) (clickable gasoline :the-end) ]
               (:babies state)     [ destroyed-nest ( clickable destroyed-eggs :nooooo ) ]
               (:rumbled state)    [ destroyed-nest (clickable destroyed-eggs :clearing) hand ]
               (not (:axe state)) [(clickable axe :get-axe) fallen]
               (not (:chopped state)) [(clickable fallen :chop-tree)]
               (:eggs state)    [louse-nest louse-eggs]
               (:chopped state) [(clickable louse-nest :lay-eggs)]
               :default [])
    :update (cond
              (:rumbled state) (set-state :babies :true)
              :default nil)

    :music "audio/liceland.mp3"
    :back :head-west }

   :unreachable-sprite-loader
   {:sprites [hand gasoline]}
   
   :nooooo
   {:background "images/forest7.png"
    :description "They're gone... My children..."
    :sound "audio/insect.mp3"
    :sprites (cond
               (:match state) [ destroyed-nest ( clickable destroyed-eggs :nooooo ) (clickable gasoline :the-end) ]
               :default [ destroyed-nest ( clickable destroyed-eggs :clearing ) ])
    :music "audio/liceland.mp3"
    :back :head-west }
   
   :get-axe
   {:background "images/forest7.png"
    :description "Still sharp"
    :music "audio/liceland.mp3"
    :sprites [(clickable fallen :chop-tree)]
    :update (set-state :axe true)
    :back :head-west}

   :chop-tree
   {:background "images/forest7.png"
    :music "audio/liceland.mp3"
    :sound "audio/chopping.mp3"
    :update (set-state :chopped :true)
    :sprites [(clickable louse-nest :lay-eggs)]
    :back :head-west}

   :lay-eggs
   {:background "images/forest7.png"
    :music "audio/liceland.mp3"
    :description "Your dear children are safe and warm"
    :update (set-state :eggs :true)
    :sprites [louse-nest louse-eggs]
    :back :head-west}
   
   :head
   {:background "images/forest8.png"
    :description "A vast forest stretches as far as the eye can see"
    :music "audio/liceland.mp3"
    :left :head-west
    :right :head-east }

   :heading-on
   {:background "images/forest5.png"
    :forward :well
    :left :well
    :back :head-east
    :sprites [ (clickable larger-mosquito :heading-on-2) ]
    :music "audio/liceland.mp3"
    :description "It just keeps going"}

   :heading-on-2
   {:background "images/forest11.png"
    :forward :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :sprites [ (cond
                 (:talked-to-mosq state) (clickable largest-mosquito :not-lookin-at-me)
                 (:mosq-one state) (clickable largest-mosquito :lookin-at-me-again)
                 :default (clickable largest-mosquito :lookin-at-me))]}

   :heading-on-3
   {:background "images/forest5.png"
    :description "You've lost your way"
    :music   "audio/liceland.mp3"
    :back    :head-east
    :left    :head-east
    :right   :head-east
    :forward :head-east }

   :well
   {:background "images/forest2.png"
    :music "audio/Liceland3.mp3"
    :sprites (cond
               (:rope state) [ well-no-rope (clickable caterpillar :caterpillar-1) ]
               (:eggs state) [ (clickable well :get-rope) (clickable caterpillar :caterpillar-1)]
               :default [ well (clickable sign :brb) ])
    :back  :heading-on
    :forward :cliffside}

   :brb
   {:back :well
    :music "audio/Liceland3.mp3"
    :background "images/well-sign-click.png"}
   
   :cliffside
   {:background "images/ledge.png"
    :music "audio/Liceland3.mp3"
    :description "The cliff is steep"
    :sprites (cliffside-sprites state)
    :back :well }

   :roped
   {:background "images/ledge.png"
    :music "audio/Liceland3.mp3"
    :description "An old rope is firmly tied around the trunk"
    :update (set-state :tied :true)
    :sprites [ (clickable post :rumbling) (clickable rope :rumbling) ]
    :back :rumbling }

   :rumbling
   {:background "images/ledge.png"
    :sound "audio/scratch.mp3"
    :update (set-state :rumbled :true)
    :sprites [ (clickable post :check) (clickable rope :check) ]
    :back :well
    :description "A great rending sound echos through the forest"}

   :check
   {:description "I have to check on my babies"
    :music "audio/Liceland3.mp3"
    :sprites (cliffside-sprites state)
    :background "images/ledge.png"
    :back :well}
   
   :help
   {:background "images/ledge.png"
    :music "audio/Liceland3.mp3"
    :description "You need help to descend safely"
    :sprites (cliffside-sprites state)
    :back :well }
   
   :ear
   {:background "images/Ear.png"
    :music "audio/liceland2.mp3"
    :back (cond
            (state :match) :clearing
            :default :cliffside)
    :forward :ear-canal}

   :ear-canal
   {:background "images/ear-canal.png"
    :music "audio/liceland2.mp3"
    :sprites (cond
               (:match state) [ (clickable devilwig :devilwig-phrase) ]
               :default [ (clickable match :get-match) (clickable devilwig :devilwig-phrase) ])
    :back :ear}

   :devilwig-phrase
   {:background "images/ear-canal.png"
    :description (get devil-phrases (rand-int (count devil-phrases)))
    :music "audio/liceland2.mp3"
    :sprites (cond
               (:match state) [ (clickable devilwig :ear-canal) ]
               :default [ (clickable match :get-match) (clickable devilwig :ear-canal) ])
    :back :ear}

   :get-match
   {:background "images/ear-canal.png"
    :music "audio/liceland2.mp3"
    :description "You know what you must do"
    :update (set-state :match :true)
    :back :ear
    :sprites [ (clickable devilwig :devilwig-phrase) ] }

   :the-end
   {:background "images/flames.png"
    :forward     :credits
    :backward    :credits
    :left        :credits
    :right       :credits
    :description "Revenge..."}

   :credits
   {:background "images/end-sign.png"}
   
   :head-east
   {:background "images/forest4.png"
    :forward :heading-on
    :music "audio/liceland.mp3"
    :sprites [  (clickable mosquito :heading-on) ]
    :right :head-west
    :left :head}})

(defn caterpillar-dialog [state]
  {
   :caterpillar-1
   {:background "images/forest2.png"
    :music "audio/Liceland3.mp3"
    :description "\"Oh dear, oh me, wherever might I be?\""
    :sprites (cond
               (and (:caterpillar-done state) (:rope state)) [ well-no-rope (clickable caterpillar :well) ]
               (:caterpillar-done state) [ (clickable well :get-rope) (clickable caterpillar :well) ]
               (:rope state) [ well-no-rope (clickable caterpillar :caterpillar-2) ]
               :default [ (clickable well :get-rope) (clickable caterpillar :caterpillar-2) ])
    :forward :cliffside
    :back  :heading-on }

   :caterpillar-2
   {:background "images/forest2.png"
    :music "audio/Liceland3.mp3"
    :description "\"Way back I saw a funny hare. Surely I'm not near there.\""
    :sprites (cond
               (:rope state) [ well-no-rope (clickable caterpillar :caterpillar-3) ]
               :default [ (clickable well :get-rope) (clickable caterpillar :caterpillar-3) ])
    :forward :cliffside
    :back  :heading-on }

   :caterpillar-3
   {:background "images/forest2.png"
    :music "audio/Liceland3.mp3"
    :description "\"Wherever would he sit! Neither hide nor hair could fit!\""
    :sprites (cond
               (:rope state) [ well-no-rope (clickable caterpillar :caterpillar-4) ]
               :default [ (clickable well :get-rope) (clickable caterpillar :caterpillar-4) ])
    :forward :cliffside
    :back  :heading-on }

   :caterpillar-4
   {:background "images/forest2.png"
    :music "audio/Liceland3.mp3"
    :description "\"Ho! Mistress so little, lend an ear to hear my riddle?\""
    :sprites (cond
               (:rope state) [ well-no-rope (clickable caterpillar :caterpillar-5) ]
               :default [ (clickable well :get-rope) (clickable caterpillar :caterpillar-5) ])
    :forward :cliffside
    :back  :heading-on }

   :caterpillar-5
   {:background "images/forest2.png"
    :music "audio/Liceland3.mp3"
    :description "\"What's at the bottom of this well? Tembling and Akakell!\""
    :sprites (cond
               (:rope state) [ well-no-rope (clickable caterpillar :well) ]
               :default [ (clickable well :get-rope) (clickable caterpillar :well) ])
    :update (set-state :caterpillar-done :true)
    :forward :cliffside
    :back  :heading-on }

    :get-rope
    {:background "images/forest2.png"
     :music "audio/Liceland3.mp3"
     :description "\"Shame it broke. Must be an old rope.\""
     :sprites [ (clickable well-no-rope :well) (clickable caterpillar :well) ]
     :update (set-state :rope :true)
     :forward :cliffside
     :back  :heading-on }})

(defn mosquito-dialog [state] {

   :not-lookin-at-me
   {:forward :heading-on-3
    :left :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :background "images/forest11.png"
    :sprites [ (clickable largest-mosquito :heading-on-2) ]
    :description "\"...\""}
   
   :lookin-at-me
   {:forward :heading-on-3
    :left :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :background "images/forest11.png"
    :sprites [ (clickable largest-mosquito :lookin-at-me-2) ]
    :description "\"Oh, another one\""}

   :lookin-at-me-2
   {:forward :heading-on-3
    :left :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :background "images/forest11.png"
    :sprites [ (clickable largest-mosquito :lookin-at-me-3) ]
    :description "\"You're no different from the others. Leave me be.\""}

   :lookin-at-me-3
   {:forward :heading-on-3
    :left :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :background "images/forest11.png"
    :sprites [ (clickable largest-mosquito :lookin-at-me-4) ]
    :description "\"I can't help one like you.\""}

   :lookin-at-me-4
   {:forward :heading-on-3
    :left :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :background "images/forest11.png"
    :update (set-state :mosq-one :true)
    :sprites [ (clickable largest-mosquito :heading-on-2) ]
    :description "\"What would be the point?\"" }

   :lookin-at-me-again
   {:forward :heading-on-3
    :left :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :background "images/forest11.png"
    :sprites [ (clickable largest-mosquito :lookin-at-me-5) ]
    :description "\"You'll die on this tiny world.\""}
   
   :lookin-at-me-5
   {:forward :heading-on-3
    :left :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :background "images/forest11.png"
    :sprites [ (clickable largest-mosquito :lookin-at-me-6) ]
    :description "\"Beyond?\""}

   :lookin-at-me-6
   {:forward :heading-on-3
    :left :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :background "images/forest11.png":sprites [ (clickable largest-mosquito :lookin-at-me-7) ]
    :description "\"Nothing for you there\""}

   :lookin-at-me-7
   {:forward :heading-on-3
    :left :heading-on-3
    :back :heading-on
    :music "audio/liceland.mp3"
    :background "images/forest11.png":sprites [ (clickable largest-mosquito :heading-on-2) ]
    :update (set-state :talked-to-mosq true)
    :description "\"Stop eyeing my wings, creep.\""}})

