package com.example.spaceinvaders

import android.app.Activity
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Bundle

class MainActivity : Activity() {
  private var spaceView: SpaceView? = null
  private lateinit var backgroundMusic: MediaPlayer

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val soundEffects = SoundEffects(this)

    soundEffects.playSound(SoundEffects.backgroundMusic)

    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)

    backgroundMusic = MediaPlayer.create(this, R.raw.background_music)
    backgroundMusic.isLooping = true
    backgroundMusic.start()
    // inicializa la vista
    spaceView = SpaceView(this, size)
    setContentView(spaceView)
  }

  override fun onResume() {
    super.onResume()

    spaceView?.resume()
  }

  override fun onPause() {
    super.onPause()

    backgroundMusic.release()
    spaceView?.pause()
    finish()
  }
}
