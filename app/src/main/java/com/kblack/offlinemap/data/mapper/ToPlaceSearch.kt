package com.kblack.offlinemap.data.mapper

import com.kblack.offlinemap.data.models.Feature
import com.kblack.offlinemap.models.PlaceSearch

fun Feature.toDomain(): PlaceSearch = PlaceSearch(
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