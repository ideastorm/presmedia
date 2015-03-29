## Presmedia To Do

I've started work on filters to go between the image source and the final destination.
I'm considering having image sources being able to register actions against preview panels.
That would make it easy to have a scrolling text input that responds to key presses or mouse clicks.
Even if I don't go with the action registration idea, I'll still need some way to set up scrolling input.

I'll also need the following:
* gradient image source
* video file image source
* audio support
  * Looking into ffmpeg bindings (xuggler)
* Video playback should have full controls on the control view, but not on the presentation view.
  * Maybe have the controls on the video preview window, so that we could have multiple videos in an overlay filter without dealing with controls on the overlay view.
* fade in/out filter
* video playback should be cued up and ready to play.  it should not lag between selecting the source and the start of playback.
* should be able to define transitions between sources (none, fade, etc)

I decided the UI was getting nasty, so I've simplified it considerably.  Instead of building up
custom filter graphs, you'll have some reasonable presets (reasonable for me, anyway)
that you can choose from.  If there's demand, I'll make the presets configurable, 
but for now, minimum viable product reigns supreme.