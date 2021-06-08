package com.example.spaceinvaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import java.util.*

class Boss(context: Context, row: Int, column: Int, screenX: Int, screenY: Int) {
    var width = screenX / 5f
    private var height = screenY / 5f
    private val padding = screenX / 100

    var life=10

    var position = RectF(
        column * (width + padding),
        10 + row * (width + padding / 4),
        column * (width + padding) + width,
        100 + row * (width + padding / 4) + height
    )

    // velocidad del invader
    private var speed = 140f

    private val left = 1
    private val right = 2

    // dirección (derecha o izquierda)
    private var shipMoving = right

    var isVisible = true

    companion object {
        lateinit var bitmap1: Bitmap
        lateinit var bitmap2: Bitmap

        // cantidad de invaders activos
        var numberOfInvaders = 0
    }

    init {
        bitmap1 = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.boss1
        )

        bitmap2 = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.boss1a
        )

        // escalado de los sprites
        bitmap1 = Bitmap.createScaledBitmap(
            bitmap1,
            (width.toInt()),
            (height.toInt()),
            false
        )
        bitmap2 = Bitmap.createScaledBitmap(
            bitmap2,
            (width.toInt()),
            (height.toInt()),
            false
        )

        numberOfInvaders++
    }

    fun update(fps: Long) {
        if (shipMoving == left)
            position.left -= speed / fps

        if (shipMoving == right)
            position.left += speed / fps

        position.right = position.left + width
    }

    fun dropDownAndReverse(waveNumber: Int) {
        shipMoving = if (shipMoving == left)
            right
        else
            left

        position.top += height/2
        position.bottom += height/2

        // aumento de la velocidad por oleadas
        speed *= (1.1f + (waveNumber.toFloat() / 20))
    }

    fun takeAim(
        playerShipX: Float,
        playerShipLength: Float,
        waves: Int
    )
            : Boolean {

        val generator = Random()
        var randomNumber: Int

        if (playerShipX + playerShipLength > position.left &&
            playerShipX + playerShipLength < position.left + width ||
            playerShipX > position.left && playerShipX < position.left + width
        ) {

            randomNumber = generator.nextInt((100 * numberOfInvaders) / waves)
            if (randomNumber == 0)
                return true
        }

        randomNumber = generator.nextInt(150 * numberOfInvaders)
        return randomNumber == 0
    }

    fun Life():Int{
        this.life=this.life-1

        return this.life
    }


}