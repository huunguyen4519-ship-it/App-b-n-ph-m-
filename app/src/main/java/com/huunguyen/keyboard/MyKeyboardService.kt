package com.huunguyen.keyboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class MyKeyboardService : InputMethodService() {

    private lateinit var glow: GlowView
    private var shift = false
    private val letterViews = mutableListOf<TextView>()

    private val keyFill = Color.parseColor("#161B22")
    private val keyStroke = Color.parseColor("#1F3A5C")
    private val keyText = Color.parseColor("#C9D6E8")
    private val keyHeightDp = 56

    private val rows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")

    override fun onCreateInputView(): View {
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#0D1117"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(8), dp(6), dp(8))
        }

        rows.forEachIndexed { index, row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            if (index == 2) {
                line.addView(makeKey("⇧", weight = 1.5f) { toggleShift() })
            }
            row.forEach { ch ->
                val key = makeKey(ch.toString()) { commitLetter(ch) }
                letterViews.add(key)
                line.addView(key)
            }
            if (index == 2) {
                line.addView(makeKey("⌫", weight = 1.5f) { deleteOne() })
            }
            column.addView(line)
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        bottom.addView(makeKey("Space", weight = 5f) { currentInputConnection?.commitText(" ", 1) })
        bottom.addView(makeKey("↵", weight = 2f) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        })
        column.addView(bottom)

        root.addView(column, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        glow = GlowView(this)
        root.addView(glow, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        return root
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (::glow.isInitialized) glow.play()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        if (::glow.isInitialized) glow.cancel()
        super.onFinishInputView(finishingInput)
    }

    private fun commitLetter(ch: Char) {
        val text = if (shift) ch.uppercaseChar() else ch
        currentInputConnection?.commitText(text.toString(), 1)
        if (shift) {
            shift = false
            refreshLetterCase()
        }
    }

    private fun toggleShift() {
        shift = !shift
        refreshLetterCase()
    }

    private fun refreshLetterCase() {
        letterViews.forEach { v ->
            val base = v.tag as String
            v.text = if (shift) base.uppercase() else base
        }
    }

    private fun deleteOne() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
    }

    private fun makeKey(label: String, weight: Float = 1f, onTap: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            tag = label
            gravity = Gravity.CENTER
            setTextColor(keyText)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(8).toFloat()
                setColor(keyFill)
                setStroke(dp(1), keyStroke)
            }
            layoutParams = LinearLayout.LayoutParams(0, dp(keyHeightDp), weight).apply {
                setMargins(dp(3), dp(3), dp(3), dp(3))
            }
            setOnClickListener { onTap() }
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
