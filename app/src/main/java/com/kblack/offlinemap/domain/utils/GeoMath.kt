package com.kblack.offlinemap.domain.utils

import com.kblack.offlinemap.domain.models.GeoCoordinate
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
// https://github.com/junjunguo/PocketMaps/blob/master/PocketMaps/app/src/main/java/com/junjunguo/pocketmaps/util/GeoMath.java
// https://vi.wikipedia.org/wiki/Kho%E1%BA%A3ng_c%C3%A1ch_Euclid\
// https://en.wikipedia.org/wiki/Haversine_formula
// The original project used Degree Approximation to calculate the distance.
// Haversine
object GeoMath {
    private const val EARTH_RADIUS_METERS = 6371000.0

    fun distanceMeters(a: GeoCoordinate, b: GeoCoordinate): Double {
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)

        val h = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(h))
    }

    fun nearestPointIndex(path: List<GeoCoordinate>, target: GeoCoordinate): Int {
        if (path.isEmpty()) return -1
        var bestIndex = 0
        var bestDistance = Double.MAX_VALUE
        var i = 0
        while (i < path.size) {
            val d = distanceMeters(path[i], target)
            if (d < bestDistance) {
                bestDistance = d
                bestIndex = i
            }
            i++
        }
        return bestIndex
    }

    fun distancePointToSegmentMeters(p: GeoCoordinate, a: GeoCoordinate, b: GeoCoordinate): Double {
        val ax = a.longitude
        val ay = a.latitude
        val bx = b.longitude
        val by = b.latitude
        val px = p.longitude
        val py = p.latitude

        val vx = bx - ax
        val vy = by - ay
        val wx = px - ax
        val wy = py - ay

        val c1 = wx * vx + wy * vy
        if (c1 <= 0) return distanceMeters(p, a)

        val c2 = vx * vx + vy * vy
        if (c2 <= c1) return distanceMeters(p, b)

        val t = c1 / c2
        val proj = GeoCoordinate(
            latitude = ay + t * vy,
            longitude = ax + t * vx
        )
        return distanceMeters(p, proj)
    }

    fun bearingDegrees(from: GeoCoordinate, to: GeoCoordinate): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var brng = Math.toDegrees(atan2(y, x))
        brng = (brng + 360.0) % 360.0
        return brng
    }
}