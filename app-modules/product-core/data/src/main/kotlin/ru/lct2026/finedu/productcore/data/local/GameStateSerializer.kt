package ru.lct2026.finedu.productcore.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Профиль в DataStore — один JSON-документ. */
internal class GameStateSerializer(private val json: Json) : Serializer<StoredGameDto> {

    override val defaultValue: StoredGameDto = StoredGameDto()

    override suspend fun readFrom(input: InputStream): StoredGameDto = try {
        json.decodeFromString(StoredGameDto.serializer(), input.readBytes().decodeToString())
    } catch (e: SerializationException) {
        throw CorruptionException("Не удалось прочитать профиль", e)
    } catch (e: IllegalArgumentException) {
        throw CorruptionException("Не удалось прочитать профиль", e)
    }

    override suspend fun writeTo(t: StoredGameDto, output: OutputStream) {
        output.write(json.encodeToString(StoredGameDto.serializer(), t).encodeToByteArray())
    }
}
