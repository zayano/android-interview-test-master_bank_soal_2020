package com.tokopedia.oilreservoir

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tokopedia.core.loadFile

/**
 * Created by fwidjaja on 2019-09-24.
 */
class OilReservoirActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem)

        val webView = findViewById<WebView>(R.id.webView)
        webView.loadFile("oil_reservoir.html")

        // example of how to call the function
        val value = intArrayOf(0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1)

        val number = Solution.collectOil(value)
        Log.d("TAG", "onCreate: $number")

        val input = findViewById<TextView>(com.tokopedia.resources.R.id.input)
        input.text = value.contentToString()

        val result = findViewById<TextView>(com.tokopedia.resources.R.id.result)
        result.text = number.toString()
    }

}