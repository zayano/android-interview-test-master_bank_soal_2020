package com.tokopedia.climbingstairs

object Solution {
    fun climbStairs(n: Int): Long {
        // TODO, return in how many distinct ways can you climb to the top. Each time you can either climb 1 or 2 steps.
        // 1 <= n < 90
        if (n < 2) return 1

        var a = 1
        var b = 1
        var c = 1

        for (i in 2..n) {
            c = b
            b += a
            a = c
        }
        return b.toLong()
    }
}
