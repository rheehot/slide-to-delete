package com.seunghyun.slidetodelete

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

class SlideToDeleteTouchListener(private val container: ViewGroup, private val content: View) : View.OnTouchListener {
    private var firstX = 0f
    private var firstViewX = 0f
    private var velocity = 0f
    private var isSlidedToRight = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val x = event.x
        val parentWidth = container.width.toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                firstX = x
                firstViewX = content.x
            }
            MotionEvent.ACTION_MOVE -> {
                if (firstViewX != 0f && firstViewX != parentWidth && firstViewX != parentWidth * -1) return true
                val distance = x - firstX
                var viewX = firstViewX + distance

                isSlidedToRight = (distance > 0 || viewX > 0) && firstViewX >= 0

                if (viewX > parentWidth) viewX = parentWidth

                velocity += viewX - content.x
                velocity /= 2
                content.x = viewX
                onViewMove()
            }
            MotionEvent.ACTION_UP -> {
                var textX = content.x
                val autoSlideVelocity = AUTO_SLIDE_VELOCITY_RATIO * parentWidth
                val autoSlideWidth = parentWidth * AUTO_SLIDE_RATIO
                isSlidedToRight = textX > 0

                if (isSlidedToRight || firstViewX == parentWidth) {
                    when {
                        velocity > autoSlideVelocity -> autoSlide(FLAG_SLIDE_TO_RIGHT)
                        velocity < autoSlideVelocity * -1 -> autoSlide(FLAG_RESET)
                        textX > autoSlideWidth -> autoSlide(FLAG_SLIDE_TO_RIGHT)
                        textX < autoSlideWidth -> autoSlide(FLAG_RESET)
                    }
                } else {
                    velocity *= -1
                    textX *= -1
                    when {
                        velocity > autoSlideVelocity -> autoSlide(FLAG_SLIDE_TO_LEFT)
                        velocity < autoSlideVelocity * -1 -> autoSlide(FLAG_RESET)
                        textX > autoSlideWidth -> autoSlide(FLAG_SLIDE_TO_LEFT)
                        textX < autoSlideWidth -> autoSlide(FLAG_RESET)
                    }
                }
            }
        }
        return true
    }

    private fun onViewMove() {
        var x = content.x
        val parentWidth = container.width

        if (!isSlidedToRight) x *= -1
        val alpha =
                if (firstViewX == 0f) 1 - x / (parentWidth / 2)
                else 2 - 2 * x / parentWidth

        content.alpha = alpha
    }

    private fun autoSlide(flag: Int) {
        val end = when (flag) {
            FLAG_RESET -> 0f
            FLAG_SLIDE_TO_LEFT -> container.width.toFloat() * -1
            FLAG_SLIDE_TO_RIGHT -> container.width.toFloat()
            else -> return
        }

        val animator = ValueAnimator.ofFloat(content.x, end).apply {
            duration = AUTO_SLIDE_MS
        }
        animator.addUpdateListener {
            content.x = it.animatedValue.toString().toFloat()
            onViewMove()
        }
        animator.start()
    }

    companion object {
        private const val AUTO_SLIDE_RATIO = 0.5
        private const val AUTO_SLIDE_VELOCITY_RATIO = 0.02
        private const val AUTO_SLIDE_MS = 200L
        private const val FLAG_RESET = 0
        private const val FLAG_SLIDE_TO_RIGHT = 1
        private const val FLAG_SLIDE_TO_LEFT = 2
    }
}