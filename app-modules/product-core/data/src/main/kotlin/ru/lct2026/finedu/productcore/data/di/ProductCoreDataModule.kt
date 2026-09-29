package ru.lct2026.finedu.productcore.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import ru.lct2026.finedu.productcore.data.local.GameStateSerializer
import ru.lct2026.finedu.productcore.data.local.StoredGameDto
import ru.lct2026.finedu.productcore.data.repository.ContentRepositoryImpl
import ru.lct2026.finedu.productcore.data.repository.GameRepositoryImpl
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface ProductCoreDataModule {

    @Binds
    fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository

    companion object {
        private const val GAME_FILE = "game.json"

        @Provides
        @Singleton
        fun provideJson(): Json = Json { ignoreUnknownKeys = true }

        /** Повреждённый файл профиля заменяется пустым: приложение не падает, игра начнётся заново. */
        @Provides
        @Singleton
        fun provideGameDataStore(@ApplicationContext context: Context, json: Json): DataStore<StoredGameDto> =
            DataStoreFactory.create(
                serializer = GameStateSerializer(json),
                corruptionHandler = ReplaceFileCorruptionHandler { StoredGameDto() },
                produceFile = { context.dataStoreFile(GAME_FILE) }
            )
    }
}
