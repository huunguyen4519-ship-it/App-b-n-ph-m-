package com.huunguyen.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (16 * resources.displayMetrics.density).toInt()
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        column.addView(TextView(this).apply {
            text = "Bước 1: bật bàn phím trong Cài đặt.\nBước 2: chọn bàn phím này làm mặc định.\nBước 3: gõ thử bên dưới."
            textSize = 16f
        })

        column.addView(Button(this).apply {
            text = "Mở cài đặt bàn phím"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        })

        column.addView(Button(this).apply {
            text = "Chọn bàn phím"
            setOnClickListener {
                getSystemService(InputMethodManager::class.java).showInputMethodPicker()
            }
        })

        column.addView(EditText(this).apply {
            hint = "Gõ thử ở đây"
        })

        setContentView(column)
    }
}
