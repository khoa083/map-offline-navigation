package com.kblack.offlinemap.data.mapper

import com.kblack.offlinemap.data.local.entity.PlaceSearchEntity
import com.kblack.offlinemap.data.models.Feature
import com.kblack.offlinemap.models.PlaceSearch
import com.kblack.offlinemap.utils.toGlobalSearchVector
import java.text.Normalizer
import java.util.Locale

fun Feature.toPlaceSearch(): PlaceSearch = PlaceSearch(
    osmId = properties.osmId,
    osmValue = properties.osmValue,
    name = properties.name,
    street = properties.street,
    locality = properties.locality,
    district = properties.district,
    city = properties.city,
    state = properties.state,
    country = properties.country,
    postcode = properties.postcode,
    lat = geometry.lat,
    lng = geometry.lng,
)

fun PlaceSearch.toPlaceSearchEntity(): PlaceSearchEntity {
    val rawSearchText = buildString {
        append(name)
        city.let { append(" $it") }
        state?.let { append(" $it") }
        country?.let { append(" $it") }
    }
    return PlaceSearchEntity(
        osmId = osmId,
        osmValue = osmValue,
        name = name,
        street = street,
        locality = locality,
        district = district,
        city = city,
        state = state,
        country = country,
        searchVector = rawSearchText.toGlobalSearchVector(),
        postcode = postcode,
        lat = lat,
        lng = lng
    )
}

fun PlaceSearchEntity.toPlaceSearch(): PlaceSearch = PlaceSearch(
    osmId = osmId,
    osmValue = osmValue,
    name = name,
    street = street,
    locality = locality,
    district = district,
    city = city,
    state = state,
    country = country,
    postcode = postcode,
    lat = lat,
    lng = lng
)