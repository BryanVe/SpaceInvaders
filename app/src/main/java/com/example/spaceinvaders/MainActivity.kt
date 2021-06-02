package com.example.spaceinvaders

import android.app.Activity
import android.graphics.Point
import android.os.Bundle

class MainActivity : Activity() {
  private var spaceView: SpaceView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)

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

    spaceView?.pause()
  }
}
