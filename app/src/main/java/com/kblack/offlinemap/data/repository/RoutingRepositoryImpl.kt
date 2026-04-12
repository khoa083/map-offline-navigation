package com.kblack.offlinemap.data.repository

import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.ResponsePath
import com.graphhopper.config.CHProfile
import com.graphhopper.config.Profile
import com.graphhopper.routing.ev.MaxSpeed
import com.graphhopper.util.Instruction
import com.graphhopper.util.Parameters
import com.graphhopper.util.details.PathDetail
import com.kblack.offlinemap.domain.models.GeoCoordinate
import com.kblack.offlinemap.domain.models.Route
import com.kblack.offlinemap.domain.models.RouteInstruction
import com.kblack.offlinemap.domain.models.RoutePathDetail
import com.kblack.offlinemap.domain.models.RoutingOptions
import com.kblack.offlinemap.domain.models.TravelMode
import com.kblack.offlinemap.domain.repository.RoutingRepository
import com.kblack.offlinemap.domain.utils.GeoMath
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class RoutingRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher = IO
) : RoutingRepository {

    @Volatile
    private var hopper: GraphHopper? = null

    override fun isInitialized(): Boolean = hopper != null

    override suspend fun initialize(graphDirectoryPath: String) {
        withContext(ioDispatcher) {
            val graphDir = File(graphDirectoryPath)
            require(graphDir.exists() && graphDir.isDirectory) {
                "Graph not found: $graphDirectoryPath"
            }

            if (hopper != null) return@withContext
            //https://github.com/graphhopper/graphhopper/blob/b5ad8b3e120df2228c116938fe7de8f99f392014/android/app/src/main/java/com/graphhopper/android/MainActivity.java#L384
            val localHopper = GraphHopper().forMobile()
            localHopper
                .setProfiles(
                    Profile("car").setVehicle("car").setWeighting("fastest"),
                    Profile("motorcycle").setVehicle("motorcycle").setWeighting("short_fastest"),
                    Profile("foot").setVehicle("foot").setWeighting("shortest")
                )
                .chPreparationHandler.setCHProfiles(
                    CHProfile("car"),
                    CHProfile("motorcycle")
                )

            val loaded = localHopper.load(graphDir.absolutePath)
            if (!loaded) {
                throw IllegalStateException("Cannot load GraphHopper graph from ${graphDir.absolutePath}")
            }
            hopper = localHopper

            Timber.d("[CAPTURE] Initialize GraphHopper: $loaded")
        }
    }

    //https://github.com/graphhopper/graphhopper/blob/b5ad8b3e120df2228c116938fe7de8f99f392014/android/app/src/main/java/com/graphhopper/android/MainActivity.java#L443
    //https://github.com/graphhopper/graphhopper/blob/b5ad8b3e120df2228c116938fe7de8f99f392014/android/app/src/main/java/com/graphhopper/android/MainActivity.java#L454
    override suspend fun calculateRoute(
        from: GeoCoordinate,
        to: GeoCoordinate,
        options: RoutingOptions
    ): Route = withContext(ioDispatcher) {
        val h = hopper ?: throw IllegalStateException("GraphHopper engine is not initialized")
        val request = GHRequest(
            from.latitude, from.longitude,
            to.latitude, to.longitude
        )
            .setProfile(options.travelMode.vehicleKey)
            .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
        request.hints.putObject(Parameters.Routing.INSTRUCTIONS, true)

        if (options.instructionsEnabled) {
            request.pathDetails.add(MaxSpeed.KEY)
            request.pathDetails.add(Parameters.Details.AVERAGE_SPEED)
        }

        val response = h.route(request)
        if (response == null || response.hasErrors()) {
            if (options.allowDirectFallback) {
                return@withContext fallBackRoute(from, to, options.travelMode)
            }
            val firstError = if (response != null && response.getErrors().isNotEmpty()) {
                response.getErrors()[0].message
            } else {
                "Unknown route error"
            }
            throw IllegalStateException(firstError)
        }
//        Timber.d("[CAPTURE] CalculateRoute GraphHopper: ${response.best}")
        mapPath(response.best)

    }

    override fun close() {
        hopper?.close()
        hopper = null
        Timber.d("[CAPTURE] Close GraphHopper")
    }

    //https://github.com/graphhopper/graphhopper/blob/b5ad8b3e120df2228c116938fe7de8f99f392014/android/app/src/main/java/com/graphhopper/android/MainActivity.java#L410
    private fun mapPath(path: ResponsePath): Route {
        val points = mutableListOf<GeoCoordinate>()

        val sizePoints = path.points.size()
        for (i in 0 until sizePoints) {
            points.add(GeoCoordinate(path.points.getLat(i), path.points.getLon(i)))
        }

        val instructions = mutableListOf<RouteInstruction>()
        val instructionList = path.getInstructions()
        var carryDistance = 0.0
        var carryTime = 0L
        val carryPoints = mutableListOf<GeoCoordinate>()

        var insIndex = 0
        while (insIndex < instructionList.size) {
            val ins = instructionList[insIndex]
            val pointsForInstruction = mapInstructionPoints(ins)

            if (ins.getSign() == Instruction.CONTINUE_ON_STREET) {
                carryDistance += ins.distance
                carryTime += ins.time
                appendPointsWithoutDuplicate(carryPoints, pointsForInstruction)
                insIndex++
                continue
            }

            val mergedPoints = mutableListOf<GeoCoordinate>()
            appendPointsWithoutDuplicate(mergedPoints, carryPoints)
            appendPointsWithoutDuplicate(mergedPoints, pointsForInstruction)

            instructions.add(
                RouteInstruction(
                    sign = ins.sign,
                    name = ins.name ?: "",
                    distanceMeters = carryDistance + ins.distance,
                    durationMillis = carryTime + ins.time,
                    points = mergedPoints
                )
            )

            carryDistance = 0.0
            carryTime = 0L
            carryPoints.clear()
            insIndex++
        }

        if (instructions.isEmpty() && carryPoints.isNotEmpty()) {
            instructions.add(
                RouteInstruction(
                    sign = Instruction.CONTINUE_ON_STREET,
                    name = "",
                    distanceMeters = carryDistance,
                    durationMillis = carryTime,
                    points = carryPoints.toList()
                )
            )
        }

        val speedMap = mutableMapOf<String, List<RoutePathDetail>>()
        for ((key, value) in path.pathDetails) {
            speedMap[key] = mapPathDetails(value)
        }

        return Route(
            distanceMeters = path.distance,
            durationMillis = path.time,
            points = points,
            instructions = instructions,
            speedDetails = speedMap,
            isDirectFallback = false,
            debugInfo = path.debugInfo ?: ""
        )
    }

    private fun mapInstructionPoints(instruction: Instruction): List<GeoCoordinate> {
        val points = mutableListOf<GeoCoordinate>()

        val sizePoints = instruction.points.size()
        for (i in 0 until sizePoints) {
            points.add(GeoCoordinate(instruction.points.getLat(i), instruction.points.getLon(i)))
        }

        return points
    }

    private fun mapPathDetails(details: List<PathDetail>): List<RoutePathDetail> {
        val mapped = mutableListOf<RoutePathDetail>()
        for (detail in details) {
            mapped.add(
                RoutePathDetail(
                    firstIndex = detail.first,
                    lastIndex = detail.last,
                    value = detail.value?.toString() ?: ""
                )
            )
        }
        return mapped
    }

    private fun appendPointsWithoutDuplicate(
        target: MutableList<GeoCoordinate>,
        source: List<GeoCoordinate>
    ) {
        for (point in source) {
            val last = target.lastOrNull()
            if (last != null && last.latitude == point.latitude && last.longitude == point.longitude) {
                continue
            }
            target.add(point)
        }
    }
    private fun fallBackRoute(from: GeoCoordinate, to: GeoCoordinate, travelMode: TravelMode): Route {
        val points = listOf(from, to)
        val distanceMeters = GeoMath.distanceMeters(from, to)

        val speedKmH = when (travelMode) {
            TravelMode.Car -> 50.0
            TravelMode.Motorcycle -> 40.0
            TravelMode.Foot -> 5.5
        }

        val durationMillis = ((distanceMeters / 1000.0) / speedKmH * 3600.0 * 1000.0).toLong()

        val instruction = RouteInstruction(
            sign = Instruction.CONTINUE_ON_STREET,
            name = "direction to target",
            distanceMeters = distanceMeters,
            durationMillis = durationMillis,
            points = points
        )

        return Route(
            distanceMeters = distanceMeters,
            durationMillis = durationMillis,
            points = points,
            instructions = listOf(instruction),
            speedDetails = emptyMap(),
            isDirectFallback = true,
            debugInfo = "direct-fallback"
        )
    }
}