package com.example.spaceinvaders

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.annotation.RequiresApi

class SpaceView2(
    context: Context,
    private val size: Point
) : SurfaceView(context), Runnable {
    private val soundEffects = SoundEffects(context)
    private val gameThread = Thread(this)
    private var playing = false
    private var paused = true

    private var canvas: Canvas = Canvas()
    private val paint: Paint = Paint()

    private var ship = Ship(context, size.x, size.y)

    // invaders
    private val invaders = ArrayList<Boss>()
    private var numInvaders = 0

    // escudos
    private val bricks = ArrayList<DefenseBrick>()
    private var numBricks: Int = 0

    private var playerBullet = Bullet(size.y, 1200f, 40f)

    private val invadersBullets = ArrayList<Bullet>()
    private var nextBullet = 0
    private val maxInvaderBullets = 20

    private var score = 0
    private var waves = 1
    private var lives = 3

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "Kotlin Invaders",
        Context.MODE_PRIVATE
    )

    private var highScore = prefs.getInt("highScore", 0)
    private var menaceInterval: Long = 1000
    private var uhOrOh: Boolean = false
    private var lastMenaceTime = System.currentTimeMillis()

    private fun prepareLevel() {
        // prepara el nivel y rellena de invaders
        Invader.numberOfInvaders = 0
        numInvaders = 0
                invaders.add(
                    Boss(
                        context,
                        1,
                        1,
                        size.x,
                        size.y
                    )
                )





        for (i in 0 until maxInvaderBullets)
            invadersBullets.add(Bullet(size.y))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun run() {
        var fps: Long = 0

        while (playing) {
            val startFrameTime = System.currentTimeMillis()

            if (!paused)
                update(fps)

            draw()

            val timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1)
                fps = 1000 / timeThisFrame

            if (!paused && ((startFrameTime - lastMenaceTime) > menaceInterval))
                menacePlayer()
        }
    }

    private fun menacePlayer() {
        if (uhOrOh) {
            soundEffects.playSound(SoundEffects.uhID)
        } else {
            soundEffects.playSound(SoundEffects.ohID)
        }

        // Reset the last menace time
        lastMenaceTime = System.currentTimeMillis()
        // Alter value of uhOrOh
        uhOrOh = !uhOrOh

    }

    private fun update(fps: Long) {
        ship.update(fps)

        // si la posición de los invaders llegó al borde de la pantalla
        var bumped = false
        // flag de game over
        var lost = false

        // actualiza la visibilidad de los invaders
        for (invader in invaders) {
            if (invader.isVisible) {
                invader.update(fps)

                if (invader.takeAim(
                        ship.position.left,
                        ship.width,
                        waves
                    )
                ) {

                    if (invadersBullets[nextBullet].shoot(
                            invader.position.left + invader.width / 2,
                            invader.position.top, playerBullet.down
                        )
                    ) {
                        nextBullet++

                        if (nextBullet == maxInvaderBullets)
                            nextBullet = 0
                    }
                }

                // determinamos si se llego a un borde para alternar el movimiento
                if (invader.position.left > size.x - invader.width
                    || invader.position.left < 0
                )
                    bumped = true
            }
        }

        if (playerBullet.isActive) {
            playerBullet.update(fps)
        }

        for (bullet in invadersBullets)
            if (bullet.isActive)
                bullet.update(fps)

        // alternamos el moviento
        if (bumped)
            for (invader in invaders) {
                invader.dropDownAndReverse(waves)

                if (invader.position.bottom >= size.y && invader.isVisible){
                   // lost = true
                    println("ddd pos"+invader.position.bottom+ " dada size"+size.y)
                invader.position.top=10f
                }
            }

        // la bala llego arriba del todo
        if (playerBullet.position.bottom < 0)
            playerBullet.isActive = false

        // las balas de los invaders llegaron abajo del todo
        for (bullet in invadersBullets)
            if (bullet.position.top > size.y)
                bullet.isActive = false

        // si una bala golpeo a algun invader
        if (playerBullet.isActive)
            for (invader in invaders)
                if (invader.isVisible)
                    if (RectF.intersects(playerBullet.position, invader.position)) {
                        if (invader.Life() > -300) {
                            invader.Life()
                            soundEffects.playSound(SoundEffects.invaderExplodeID)
                            println("dsadasdsad"+invader.Life())
                        } else {
                            println("dsadasdsad"+invader.Life())
                            invader.isVisible = false
                            playerBullet.isActive = false
                            soundEffects.playSound(SoundEffects.invaderExplodeID)
                            playerBullet.isActive = false

                            Boss.numberOfInvaders--
                            score += 10
                            if (score > highScore) {
                                highScore = score
                            }

                            if (Invader.numberOfInvaders == 0) {
                                paused = true
                                lives++
                                invaders.clear()
                                bricks.clear()
                                invadersBullets.clear()
                                prepareLevel()
                                waves++
                                break
                            }

                            break
                        }

                    }

        if (playerBullet.isActive)
            for (brick in bricks)
                if (brick.isVisible)
                    if (RectF.intersects(playerBullet.position, brick.position)) {
                        // A collision has occurred
                        playerBullet.isActive = false
                        brick.isVisible = false
                        soundEffects.playSound(SoundEffects.damageShelterID)
                    }

        for (bullet in invadersBullets)
            if (bullet.isActive)
                if (RectF.intersects(ship.position, bullet.position)) {
                    bullet.isActive = false
                    lives--
                    soundEffects.playSound(SoundEffects.playerExplodeID)

                    // Is it game over?
                    if (lives == 0) {
                        lost = true
                        break
                    }
                }

        if (lost) {
            paused = true
            lives = 3
            score = 0
            waves = 1
            invaders.clear()
            bricks.clear()
            invadersBullets.clear()
            prepareLevel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun draw() {
        if (holder.surface.isValid) {
            paint.typeface = resources.getFont(R.font.pixelart)
            paint.textAlign = Paint.Align.CENTER

            canvas = holder.lockCanvas()

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.space)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            paint.color = Color.argb(255, 107, 214, 75)

            canvas.drawBitmap(
                ship.bitmap,
                ship.position.left,
                ship.position.top,
                paint
            )

            for (invader in invaders)
                if (invader.isVisible) {
                    if (uhOrOh)
                        canvas.drawBitmap(
                            Boss.bitmap1,
                            invader.position.left,
                            invader.position.top,
                            paint
                        )
                    else
                        canvas.drawBitmap(
                            Boss.bitmap2,
                            invader.position.left,
                            invader.position.top,
                            paint
                        )
                }

            for (brick in bricks)
                if (brick.isVisible)
                    canvas.drawRect(brick.position, paint)

            if (playerBullet.isActive)
                canvas.drawRect(playerBullet.position, paint)

            for (bullet in invadersBullets)
                if (bullet.isActive)
                    canvas.drawRect(bullet.position, paint)

            paint.color = Color.argb(255, 255, 255, 255)
            paint.textSize = 70f
            val xPos = (canvas.width / 2).toFloat()
            val yPos = 100f
            canvas.drawText(
                "Score: $score | Lives: $lives | Wave: " + "$waves | HighScore: $highScore",
                xPos,
                yPos,
                paint
            )

            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun pause() {
        playing = false
        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }

        val prefs = context.getSharedPreferences(
            "Kotlin Invaders",
            Context.MODE_PRIVATE
        )

        val oldHighScore = prefs.getInt("highScore", 0)

        if (highScore > oldHighScore) {
            val editor = prefs.edit()
            editor.putInt("highScore", highScore)
            editor.apply()
        }
    }

    fun resume() {
        playing = true
        prepareLevel()
        gameThread.start()
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val motionArea = size.y - (size.y / 8)
        when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                paused = false

                if (motionEvent.y > motionArea) {
                    if (motionEvent.x > size.x / 2)
                        ship.moving = Ship.right
                    else
                        ship.moving = Ship.left
                }

                if (motionEvent.y < motionArea) {
                    if (playerBullet.shoot(
                            ship.position.left + ship.width / 2f,
                            ship.position.top,
                            playerBullet.up
                        ))
                        soundEffects.playSound(SoundEffects.shootID)
                }
            }

            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                if (motionEvent.y > motionArea)
                    ship.moving = Ship.stopped
            }
        }

        return true
    }
}