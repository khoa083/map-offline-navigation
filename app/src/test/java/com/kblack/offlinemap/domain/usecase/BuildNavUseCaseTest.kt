package com.kblack.offlinemap.domain.usecase

import com.kblack.offlinemap.domain.models.GeoCoordinate
import com.kblack.offlinemap.domain.models.NavigationSnapshot
import com.kblack.offlinemap.domain.models.Route
import com.kblack.offlinemap.domain.usecase.routing.BuildNavigationUseCase
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BuildNavUseCaseTest {
    private val buildNavigationUseCase = BuildNavigationUseCase()

    //todo: Example: https://graphhopper.com/maps/?point=21.067069%2C105.804058&point=21.067719%2C105.803948&point=21.068065%2C105.804279&point=21.068526%2C105.804634&point=21.069071%2C105.80513&point=21.069494%2C105.805438&point=21.070023%2C105.80585&point=21.07038%2C105.806746&profile=scooter&layer=Omniscale
    private val listGeo = listOf(
        GeoCoordinate(latitude = 21.067069, longitude = 105.804058),
        GeoCoordinate(latitude = 21.067719, longitude = 105.803948),
        GeoCoordinate(latitude = 21.068065, longitude = 105.804279),
        GeoCoordinate(latitude = 21.068526, longitude = 105.804634),
        GeoCoordinate(latitude = 21.069071, longitude = 105.805130),
        GeoCoordinate(latitude = 21.069494, longitude = 105.805438),
        GeoCoordinate(latitude = 21.070023, longitude = 105.805850),
        GeoCoordinate(latitude = 21.070380, longitude = 105.806746),
    )

    private fun currentLocation(
        latitude: Double = 21.067069,
        longitude: Double = 105.804058
    ): GeoCoordinate { return GeoCoordinate(latitude, longitude) }

    private fun routeMock(
        distanceMeters: Double = 498.0,
        durationMillis: Long = 55_000L,
        points: List<GeoCoordinate> = listGeo,
    ): Route {
        return Route(
            distanceMeters = distanceMeters,
            durationMillis = durationMillis,
            points = points,
            instructions = emptyList(),
            speedDetails = emptyMap(),
            isDirectFallback = false,
            debugInfo = ""
        )
    }

    private val defaultRoute = NavigationSnapshot(
        nearestPointIndex = -1,
        remainingDistanceMeters = 0.0,
        remainingDurationMillis = 0,
        isOffTrack = true,
        nextInstruction = null)

    @Test
    fun `BuildNavigationUseCase should default value if the route is null`() {
        val emptyRoute = routeMock(points = emptyList())

        val actual = buildNavigationUseCase(emptyRoute, currentLocation())

        assertThat(actual, `is`(defaultRoute))
    }

    //todo: Example: https://graphhopper.com/maps/?point=21.067654%2C105.803857&point=21.067719%2C105.803948&point=21.068065%2C105.804279&point=21.068526%2C105.804634&point=21.069071%2C105.80513&point=21.069494%2C105.805438&point=21.070023%2C105.80585&point=21.07038%2C105.806746&profile=scooter&layer=Omniscale
    @Test
    fun `BuildNavigationUseCase should isOffTrack = false when location is within default 30m threshold`() {
        val onRoadLocation = currentLocation(latitude = 21.067654, longitude = 105.803857)

        val actual = buildNavigationUseCase(routeMock(), onRoadLocation)

        assertFalse(actual.isOffTrack)
    }

    //todo: Example: https://graphhopper.com/maps/?point=21.068542%2C105.803229&point=21.067719%2C105.803948&point=21.068065%2C105.804279&point=21.068526%2C105.804634&point=21.069071%2C105.80513&point=21.069494%2C105.805438&point=21.070023%2C105.80585&point=21.07038%2C105.806746&profile=scooter&layer=Omniscale
    @Test
    fun `BuildNavigationUseCase should isOffTrack = true when location is within default 30m threshold`() {

        val farAwayLocation = currentLocation(latitude = 21.068542, longitude = 105.803229)

        val actual = buildNavigationUseCase(routeMock(), farAwayLocation)

        assertTrue(actual.isOffTrack)
    }

    //todo: Example: https://graphhopper.com/maps/?point=21.067895%2C105.803917&point=21.067719%2C105.803948&point=21.068065%2C105.804279&point=21.068526%2C105.804634&point=21.069071%2C105.80513&point=21.069494%2C105.805438&point=21.070023%2C105.80585&point=21.07038%2C105.806746&profile=scooter&layer=Omniscale
    @Test
    fun `BuildNavigationUseCase isOffTrack should be at any distance`() {
        val farAwayLocation = currentLocation(latitude = 21.067895, longitude = 105.803917)

        val actual10m = buildNavigationUseCase(routeMock(), farAwayLocation, offTrack=10.0)
        val actual50m = buildNavigationUseCase(routeMock(), farAwayLocation, offTrack=50.0)

        assertTrue(actual10m.isOffTrack)
        assertFalse(actual50m.isOffTrack)
    }

    @Test
    fun `BuildNavigationUseCase should remainingDurationMillis = 55000L when the location is close to the starting point`() {
        val startLocation = currentLocation()
        val durationMillis = routeMock().durationMillis

        val actual = buildNavigationUseCase(routeMock(), startLocation)

        assertEquals(durationMillis, actual.remainingDurationMillis)
    }

    @Test
    fun `BuildNavigationUseCase should remainingDurationMillis = 0L when the location is close to the end point`() {
        val endLocation = GeoCoordinate(latitude = 21.070380, longitude = 105.806746)
        val durationMillis = 0L

        val actual = buildNavigationUseCase(routeMock(), endLocation)

        assertEquals(durationMillis, actual.remainingDurationMillis)
    }

    @Test
    fun `BuildNavigationUseCase should remainingDistanceMeters = 498m when the location is close to the starting point`() {
        val startLocation = currentLocation()
        val distanceMeters = routeMock().distanceMeters

        val actual = buildNavigationUseCase(routeMock(), startLocation)

        assertEquals(distanceMeters, actual.remainingDistanceMeters,0.1)
    }

    @Test
    fun `BuildNavigationUseCase should remainingDistanceMeters = 0m when the location is close to the end point`() {
        val endLocation = GeoCoordinate(latitude = 21.070380, longitude = 105.806746)
        val distanceMeters = 0.0

        val actual = buildNavigationUseCase(routeMock(), endLocation)

        assertEquals(distanceMeters, actual.remainingDistanceMeters,0.1)
    }

    @Test
    fun `BuildNavigationUseCase nearestPointIndex = 0 when the location is close to the starting point`() {
        val startLocation = currentLocation()

        val actual = buildNavigationUseCase(routeMock(), startLocation)

        assertThat(actual.nearestPointIndex == 0, `is`(true))
        assertThat(actual.nearestPointIndex < listGeo.size, `is`(true))
    }

    @Test
    fun `BuildNavigationUseCase nearestPointIndex = 7 when the location is close to the end point`() {
        val endLocation = GeoCoordinate(latitude = 21.070380, longitude = 105.806746)

        val actual = buildNavigationUseCase(routeMock(), endLocation)

        assertThat(actual.nearestPointIndex == 7, `is`(true))
    }

    @Test
    fun `BuildNavigationUseCase should handle route with single point`() {
        val singlePointRoute = routeMock(points = listOf(listGeo[0]))

        val actual = buildNavigationUseCase(singlePointRoute, currentLocation())

        assertThat(actual.nearestPointIndex, `is`(0))
        assertEquals(0.0, actual.remainingDistanceMeters, 0.1)
    }

}