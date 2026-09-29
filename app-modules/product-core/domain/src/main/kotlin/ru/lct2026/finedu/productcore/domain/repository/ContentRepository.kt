package ru.lct2026.finedu.productcore.domain.repository

import ru.lct2026.finedu.productcore.domain.model.GameContent

interface ContentRepository {
    suspend fun content(): GameContent
}
