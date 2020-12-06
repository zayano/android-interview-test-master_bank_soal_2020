package com.tokopedia.minimumpathsum

object Solution {
    fun minimumPathSum(matrix: Array<IntArray>): Int {
        // TODO, find a path from top left to bottom right which minimizes the sum of all numbers along its path, and return the sum
        // below is stub
        val m = matrix.size
        val n = matrix[0].size

        for (i in 0 until m) {
            for (j in 0 until n) {
                if (i == 0 && j == 0) continue
                when {
                    i == 0 -> {
                        matrix[i][j] += matrix[i][j - 1]
                    }
                    j == 0 -> {
                        matrix[i][j] += matrix[i - 1][j]
                    }
                    else -> {
                        matrix[i][j] += matrix[i][j - 1].coerceAtMost(matrix[i - 1][j])
                    }
                }
            }
        }
        return matrix[m-1][n-1]
    }

}
