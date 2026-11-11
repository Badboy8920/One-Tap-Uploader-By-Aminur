package com.onetap.uploader

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val message = TextView(this)
        message.text = "OneTap Uploader is working!"
        message.textSize = 24f
        message.setPadding(30, 50, 30, 50)

        setContentView(message)
    }
}
