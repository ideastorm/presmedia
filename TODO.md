## Presmedia To Do

I've started work on filters to go between the image source and the final destination.
I'm considering having image sources being able to register actions against preview panels.
That would make it easy to have a scrolling text input that responds to key presses or mouse clicks.
Even if I don't go with the action registration idea, I'll still need some way to set up scrolling input.

I'll also need the following:
* gradient image source
* video file image source
* audio support
  * Looking into ffmpeg bindings
* ability to save named filter graphs and use them as presets/templates
* Video playback should have full controls on the control view, but not on the presentation view.
  * Maybe have the controls on the video preview window, so that we could have multiple videos in an overlay filter without dealing with controls on the overlay view.
