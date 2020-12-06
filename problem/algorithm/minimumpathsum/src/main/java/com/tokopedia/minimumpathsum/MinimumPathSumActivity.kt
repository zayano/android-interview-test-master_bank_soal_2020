package com.tokopedia.minimumpathsum

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tokopedia.core.loadFile

class MinimumPathSumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem)
        val webView = findViewById<WebView>(R.id.webView)
        webView.loadFile("minimum_path_sum.html");

        // example of how to call the function
        val value = arrayOf(
                intArrayOf(1, 3, 1),
                intArrayOf(1, 5, 1),
                intArrayOf(4, 2, 1))

        val number = Solution.minimumPathSum(value)
        Log.d("TAG", "Minimum Path Sum: $number")

        val input = findViewById<TextView>(com.tokopedia.resources.R.id.input)
        input.text = value.contentDeepToString()

        val result = findViewById<TextView>(com.tokopedia.resources.R.id.result)
        result.text = number.toString()
    }

}