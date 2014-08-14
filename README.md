What is this about?
==============

This is the underlying framework of the TestObject visual testing language.
The idea is to record a test script while the user is interacting with his app.
Once recorded, the interactions can be replayed to validate functional correctness.

If you have any question or want to collaborate, contact erik.nijkamp@gmail.com

[![TestObject Scripting](https://raw.githubusercontent.com/wiki/testobject/visual-scripting/recorder.png)](http://www.youtube.com/watch?v=8PSkyYzxQ0I)

How does this work?
==============

The testing approach follows a capture & replay procedure:
 1. Capture: A test-script is recorded by capturing the interactions between user and application-under-test (AUT).
 2. Replay: The captured interactions can be reproduced in automated fashion to ensure functional correctness of the AUT (i.e. no regressions were introduced).

The novelty of this approach lies in the inference of a test script that is solely based on images (e.g. screenshots of the AUT) and user inputs (e.g. mouse clicks).

A test script might look like this:

      click(imageOf('login button'))   
      waitAppears(imageOf('failed dialog'))

This is how the framework captures such a script and in turn replays it:

**(1) Capture a test script on 1 device**
 
 1. Capturing - the end-user is remotely controlling an application-under-test (e.g. on an Android phone). The user actions (i.e. mouse inputs) and the reactions of the application (i.e. sequence of screenshots) are captured
 2. Parsing - a parser translates each captured screenshot into a hierarchical representation of GUI elements
 3. Inference - the sequence of inputs and GUI elements are correlated to infer the user action (here: "click(imageOf('login button'))")
 4. Assertions - the user can define assertions that represent segments of the screen must appear to ensure functional correctness (i.e. the reactions of the AUT)
 5. Script - a test script is defined as a sequence of steps (either user actions or assertions) and associated GUI elements
 
**(2) Replay a test script on n devices**

 1. Loop - perform each step (e.g. "click(imageOf('login button'))") of the test script until it fails or terminates
 2. Matching - resolve a locator (e.g. "imageOf('login button')") on the current screen
 3. Perform - reproduce the captured user-action (e.g. "click(imageOf('login button'))")
 4. Assert - wait for the GUI to respond to the replicated user input and validate assertions (e.g. "waitAppears(imageOf('failed dialog')")

What does this contain?
==============

(1) Parser - A map-reduce inspired approach for the extraction of GUI objects from images ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Takes an image as input, partitions the image into segments and assigns a GUI object class and probability to each segment. ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Multi-pass map-reduce logic which can be utilized to reduce the results multiple map tasks, e.g. fine-grained character segmentation for text-recognition and coarse-grained segmentation to identify contours of larger elements, say buttons or textfields ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Post-processing logic (grouping, cleansing, indexing) to enhance GUI segments ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Spatial R-tree based indexing for blob grouping ([Nijkamp](mailto:erik.nijkamp@gmail.com)) 

(2) Segmentation - A set of specialized image segmentation algorithms  ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Hierarhical, fill-based blob segmentation algorithm for black-white images ([Nijkamp](mailto:erik.nijkamp@gmail.com)) 
   * Hierarchical image segmentation algorithm for black-white images in linear time ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Hierarchical color-based image segmentation algorithm

(3) Classification - Bayesian inference of GUI objects ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Invariant features for GUI elements (e.g. histogram feature, contour, color) ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Supervised, probabilistic classifiers utilizing rule-based descriptions of GUI elements ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Bayesian GUI hierarchy inference using generative models ([Nijkamp](mailto:erik.nijkamp@gmail.com))

(4) Inference - Determine interfaction from GUI hierarchy and inputs ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Rule-based finite state machine algorithm to determine compound user inputs from events (e.g. type('a'), type('b'), type('c') -> type('abc')) ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Algorithm which determines the most likely GUI segment locator for a given user input (e.g. GUI element for given click xy-coordinates) ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Occlusion-tracking algorithms which can infer transformations of two given GUI hierarchies with multiple z-levels ([Nijkamp](mailto:erik.nijkamp@gmail.com))

(5) Matching - Identify a GUI locator on a given screenshot ([Nijkamp](mailto:erik.nijkamp@gmail.com), [Bielski](prishelec@gmail.com))
   * Graph-based grammar to describe abstract GUI element relations (e.g. hierarchies) and element features (e.g. generative visual model, color histogram, contour, ...) ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Abstract formalism to query GUI elements in terms of statistical features ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Fast multi-pass image templating algorithm based on normalized crosss-correlation with hierarchical "zooming" ([Nijkamp](mailto:erik.nijkamp@gmail.com), [Bielski](prishelec@gmail.com))
   * Probabilistic sub-graph matching algorithm to resolve GUI elements based on invariant features ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   
(6) Text reconigition - ([Nijkamp](mailto:erik.nijkamp@gmail.com), [Lüdeke](mailto:andreas.luedeke@googlemail.com))
   * Binary classifiers which detect potential text candidates in a given set of segments ([Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Tesseract based OCR text recognition algorithm optizmied for GUIs ([Lüdeke](mailto:andreas.luedeke@googlemail.com), [Nijkamp](mailto:erik.nijkamp@gmail.com))
   * Custom OCR algorithm specialized for low resolution (~90 DPI) text reconigition ([Nijkamp](mailto:erik.nijkamp@gmail.com))



Credits
==============

Erik Nijkamp (erik.nijkamp@gmail.com) - Maintainer & Contributor   
Leonti Bielski (prishelec@gmail.com) - Contributor   
Andreas Lüdeke (andreas.luedeke@googlemail.com) - Contributor  



