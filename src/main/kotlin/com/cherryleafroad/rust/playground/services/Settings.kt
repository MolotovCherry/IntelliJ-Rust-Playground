package com.cherryleafroad.rust.playground.services

import com.cherryleafroad.rust.playground.settings.GlobalConfiguration
import com.cherryleafroad.rust.playground.settings.plugin.PluginConfiguration
import com.cherryleafroad.rust.playground.utils.PlayRunConfigMap
import com.cherryleafroad.rust.playground.utils.ScratchConfigMap
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.CachedSingletonsRegistry
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.Transient

@Suppress("MemberVisibilityCanBePrivate")
@State(
    name = "RustPlayground",
    storages = [Storage("rust-playground.xml")]
)
class Settings : PersistentStateComponent<Settings> {
    /******* Service Setup *******/
    override fun getState(): Settings {
        return this
    }

    override fun loadState(state: Settings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        private var ourInstance = CachedSingletonsRegistry.markCachedField(Settings::class.java)

        fun getInstance(): Settings {
            return ourInstance ?: run {
                val result = ApplicationManager.getApplication().getService(Settings::class.java)
                ourInstance = result
                result
            }
        }
    }

    /************* Settings and Properties ******************/

    @Tag("global")
    var global: GlobalConfiguration = GlobalConfiguration()
    @Transient
    var plugin: PluginConfiguration = PluginConfiguration()
    @Tag("Scratches")
    var scratches: ScratchConfigMap = ScratchConfigMap()
    @Tag("RunConfigurations")
    var runConfigurations: PlayRunConfigMap = PlayRunConfigMap()
}
