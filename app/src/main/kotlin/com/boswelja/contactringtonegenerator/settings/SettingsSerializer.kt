package com.boswelja.contactringtonegenerator.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.boswelja.contatringtonegenerator.settings.Settings
import java.io.InputStream
import java.io.OutputStream

val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsSerializer
)

object SettingsSerializer : Serializer<Settings> {

    override val defaultValue: Settings = Settings(
        volumeMultiplier = 1.0f,
        loudnessEqualization = true
    )

    override suspend fun readFrom(input: InputStream): Settings {
        return Settings.ADAPTER.decode(input)
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        Settings.ADAPTER.encode(output, t)
    }
}
