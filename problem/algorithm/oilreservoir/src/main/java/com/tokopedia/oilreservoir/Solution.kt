package com.tokopedia.oilreservoir

/**
 * Created by fwidjaja on 2019-09-24.
 */
object Solution {
    fun collectOil(height: IntArray): Int {
        // TODO, return the amount of oil blocks that could be collected
        // below is stub
        if (height.isEmpty()) return 0
        var left = 0
        var right: Int = height.size - 1
        var leftMax = 0
        var rightMax = 0
        var ans = 0
        while (left < right) {
            if (height[left] > leftMax) leftMax = height[left]
            if (height[right] > rightMax) rightMax = height[right]
            if (leftMax < rightMax) {
                ans += 0.coerceAtLeast(leftMax - height[left])
                left++
            } else {
                ans += 0.coerceAtLeast(rightMax - height[right])
                right--
            }
        }
        return ans
    }
}
