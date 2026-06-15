package com.andyl.iris.domain.usecase.contract

import com.andyl.iris.domain.model.PredefinedPack

interface InstallPredefinedPackUseCase {
    suspend operator fun invoke(
        pack: PredefinedPack, 
        targetPackId: String? = null,
        overrideUrls: List<String?>? = null
    ): Result<Unit>
}
