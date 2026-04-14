package com.kblack.offlinemap.domain.usecase.routing

import com.kblack.offlinemap.domain.models.GeoCoordinate
import com.kblack.offlinemap.domain.models.NavigationSnapshot
import com.kblack.offlinemap.domain.models.Route
import com.kblack.offlinemap.domain.models.RouteInstruction
import com.kblack.offlinemap.domain.utils.GeoMath

// https://github.com/junjunguo/PocketMaps/blob/master/PocketMaps/app/src/main/java/com/junjunguo/pocketmaps/navigator/NaviEngine.java
// nearest-point + off-track detection
// https://stackoverflow.com/questions/47109609/algorithm-to-find-the-nearest-point-to-a-path-using-latitude-and-longitude
class BuildNavigationUseCase {

    operator fun invoke(
        route: Route,
        currentLocation: GeoCoordinate,
        offTrack: Double = 30.0
    ): NavigationSnapshot {
        val points = route.points
        if (points.isEmpty()) {
            return NavigationSnapshot(
                nearestPointIndex = -1,
                remainingDistanceMeters = 0.0,
                remainingDurationMillis = 0,
                isOffTrack = true,
                nextInstruction = null
            )
        }

        val nearestIndex = GeoMath.nearestPointIndex(points, currentLocation)
        val offTrackDistance = minDistanceToRoute(points, currentLocation)
        val isOffTrack = offTrackDistance > offTrack

        var remainingDistance = 0.0
        var i = nearestIndex
        while (i < points.size - 1) {
            remainingDistance += GeoMath.distanceMeters(points[i], points[i + 1])
            i++
        }

        val routeDistance = if (route.distanceMeters <= 0.0) 1.0 else route.distanceMeters
        val remainingRatio = (remainingDistance / routeDistance).coerceIn(0.0, 1.0)
        val remainingDuration = (route.durationMillis * remainingRatio).toLong()

        return NavigationSnapshot(
            nearestPointIndex = nearestIndex,
            remainingDistanceMeters = remainingDistance,
            remainingDurationMillis = remainingDuration,
            isOffTrack = isOffTrack,
            nextInstruction = findInstruction(route.instructions, points[nearestIndex])
        )
    }

    private fun minDistanceToRoute(
        points: List<GeoCoordinate>,
        currentLocation: GeoCoordinate
    ): Double {
        if (points.size == 1) {
            return GeoMath.distanceMeters(points[0], currentLocation)
        }

        var minDistance = Double.MAX_VALUE
        var i = 0
        while (i < points.size - 1) {
            val dist = GeoMath.distancePointToSegmentMeters(
                p = currentLocation,
                a = points[i],
                b = points[i + 1]
            )
            if (dist < minDistance) {
                minDistance = dist
            }
            i++
        }
        return minDistance
    }

    private fun findInstruction(
        instructions: List<RouteInstruction>,
        nearestPoint: GeoCoordinate
    ): RouteInstruction? {
        if (instructions.isEmpty()) return null

        var result: RouteInstruction? = instructions[0]
        var best = Double.MAX_VALUE
        for (ins in instructions) {
            if (ins.points.isEmpty()) continue
            val d = GeoMath.distanceMeters(ins.points[0], nearestPoint)
            if (d < best) {
                best = d
                result = ins
            }
        }
        return result
    }
}