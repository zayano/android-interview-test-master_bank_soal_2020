package com.tokopedia.climbingstairs

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tokopedia.core.loadFile

class ClimbingStairsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem)
        val webView = findViewById<WebView>(R.id.webView)
        webView.loadFile("climbing_stairs.html")

        // example of how to call the function
        val value = Solution.climbStairs(10)
        Log.d("TAG", "onCreate: $value")

        val input = findViewById<TextView>(com.tokopedia.resources.R.id.input)
        input.text = 10.toString()

        val result = findViewById<TextView>(com.tokopedia.resources.R.id.result)
        result.text = value.toString()
    }
}