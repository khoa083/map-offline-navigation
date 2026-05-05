package com.kblack.offlinemap.usecase.routing

import com.kblack.offlinemap.data.repository.RoutingRepository

class InitializeRouterUseCase(
    private val routingRepository: RoutingRepository
) {
    suspend operator fun invoke(graphDirectoryPath: String) {
        if (routingRepository.isInitialized()) return
        routingRepository.initialize(graphDirectoryPath)
    }
}
