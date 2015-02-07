# Presmedia To Do

I've started work on filters to go between the image source and the final destination.
I'm considering having image sources being able to register actions against preview panels.
That would make it easy to have a scrolling text input that responds to key presses or mouse clicks.
Even if I don't go with the action registration idea, I'll still need some way to set up scrolling input.

I'll also need the following:
* overlay (optionally with some degree of opacity)
  * This will involve resizing and positioning
* color box image source
* gradient image source
* video file image source
* audio support
  * Looking into ffmpeg bindings
