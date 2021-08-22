package com.cherryleafroad.rust.playground.config

import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.intellij.ide.util.PropertiesComponent

interface Setting<T> {
    val setting: String
    val properties: PropertiesComponent

    fun get(): T
    fun get(defaultVal: T): T
    fun set(value: T)

    fun unset() {
        properties.unsetValue(setting)
    }
}

interface ComboSetting {
    fun get(defaultVal: Int): Int
    fun set(value: Int)
}

class BooleanSetting (
    override val setting: String,
    override val properties: PropertiesComponent
) : Setting<Boolean> {
    override fun get(): Boolean {
        return properties.getBoolean(setting, false)
    }

    override fun get(defaultVal: Boolean): Boolean {
        return properties.getBoolean(setting, defaultVal)
    }

    override fun set(value: Boolean) {
        properties.setValue(setting, value)
    }
}

class StringSetting(
    override val setting: String,
    override val properties: PropertiesComponent
) : Setting<String> {
    override fun get(): String {
        return properties.getValue(setting, "")
    }

    override fun get(defaultVal: String): String {
        return properties.getValue(setting, defaultVal)
    }

    override fun set(value: String) {
        properties.setValue(setting, value)
    }
}

class EditionSetting(
    override val setting: String,
    override val properties: PropertiesComponent
) : Setting<Edition>, ComboSetting {
    override fun get(): Edition {
        val index = properties.getInt(setting, Edition.DEFAULT.index)
        return Edition.fromIndex(index)
    }

    override fun get(defaultVal: Edition): Edition {
        val index = properties.getInt(setting, defaultVal.index)
        return Edition.fromIndex(index)
    }

    override fun get(defaultVal: Int): Int {
        return properties.getInt(setting, defaultVal)
    }

    override fun set(value: Edition) {
        set(value.index)
    }

    override fun set(value: Int) {
        // this is needed to avoid "unset if value equals default" feature
        // basically, always save the setting as it should be, don't do any unset nonsense
        var defaultValue = Edition.DEFAULT.index
        if (value == defaultValue) {
            defaultValue++
        }

        properties.setValue(setting, value, defaultValue)
    }
}

class ToolchainSetting(
    override val setting: String,
    override val properties: PropertiesComponent
) : Setting<RustChannel>, ComboSetting {
    override fun get(): RustChannel {
        val index = properties.getInt(setting, RustChannel.DEFAULT.index)
        return RustChannel.fromIndex(index)
    }

    override fun get(defaultVal: RustChannel): RustChannel {
        val index = properties.getInt(setting, defaultVal.index)
        return RustChannel.fromIndex(index)
    }

    override fun set(value: RustChannel) {
        set(value.index)
    }

    override fun get(defaultVal: Int): Int {
        return properties.getInt(setting, defaultVal)
    }

    override fun set(value: Int) {
        // this is needed to avoid "unset if value equals default" feature
        // basically, always save the setting as it should be, don't do any unset nonsense
        var defaultValue = RustChannel.DEFAULT.index
        if (value == defaultValue) {
            defaultValue++
        }
        properties.setValue(setting, value, defaultValue)
    }
}

