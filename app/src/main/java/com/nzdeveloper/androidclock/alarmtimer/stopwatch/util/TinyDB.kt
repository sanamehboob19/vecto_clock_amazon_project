package com.nzdeveloper.androidclock.alarmtimer.stopwatch.util

import android.content.SharedPreferences
import android.text.TextUtils
import androidx.core.content.edit
import com.nzdeveloper.androidclock.alarmtimer.stopwatch.app.MyApp
import javax.inject.Inject

class TinyDB @Inject constructor(private val sharedPreferences: SharedPreferences) {

    // -------------------- STRING --------------------
    /**
     * Save a string
     * Example:
     * val tinyDB = TinyDB()
     * tinyDB.putString("username", "Circle")
     */
    fun putString(key: String, value: String) {

        sharedPreferences.edit { putString(key, value) }
    }

    /**
     * Get a string
     * Example:
     * val name = tinyDB.getString("username", "DefaultName")
     */
    fun getString(key: String, default: String = ""): String {
        return sharedPreferences.getString(key, default) ?: default
    }

    // -------------------- LIST STRING --------------------
    /**
     * Save a list of strings
     * Example:
     * tinyDB.putStringList("colors", arrayListOf("Red","Blue"))
     */
    fun putStringList(key: String, list: ArrayList<String>) {
        val joined = TextUtils.join("‚‗‚", list)
        sharedPreferences.edit { putString(key, joined) }
    }

    /**
     * Get a list of strings
     * Example:
     * val colors = tinyDB.getStringList("colors")
     */
    fun getStringList(key: String): ArrayList<String> {
        val saved = sharedPreferences.getString(key, "") ?: ""
        return if (saved.isEmpty()) ArrayList() else ArrayList(listOf(*TextUtils.split(saved, "‚‗‚")))
    }

    // -------------------- INT --------------------
    /**
     * Save an integer
     * Example:
     * tinyDB.putInt("age", 26)
     */
    fun putInt(key: String, value: Int) {
        sharedPreferences.edit { putInt(key, value) }
    }

    /**
     * Get an integer
     * Example:
     * val age = tinyDB.getInt("age", 0)
     */
    fun getInt(key: String, default: Int = 0): Int {
        return sharedPreferences.getInt(key, default)
    }

    // -------------------- BOOLEAN --------------------
    /**
     * Save a boolean
     * Example:
     * tinyDB.putBoolean("isPremium", true)
     */
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }

    /**
     * Get a boolean
     * Example:
     * val isPremium = tinyDB.getBoolean("isPremium", false)
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    // -------------------- FLOAT --------------------
    /**
     * Save a float
     * Example:
     * tinyDB.putFloat("rating", 4.5f)
     */
    fun putFloat(key: String, value: Float) {
        sharedPreferences.edit { putFloat(key, value) }
    }

    /**
     * Get a float
     * Example:
     * val rating = tinyDB.getFloat("rating", 0f)
     */
    fun getFloat(key: String, default: Float = 0f): Float {
        return sharedPreferences.getFloat(key, default)
    }

    // -------------------- DOUBLE --------------------
    /**
     * Save a double (converted to long bits)
     * Example:
     * tinyDB.putDouble("pi", 3.14159)
     */
    fun putDouble(key: String, value: Double) {
        val longBits = java.lang.Double.doubleToRawLongBits(value)
        sharedPreferences.edit { putLong(key, longBits) }
    }

    /**
     * Get a double
     * Example:
     * val pi = tinyDB.getDouble("pi", 0.0)
     */
    fun getDouble(key: String, default: Double = 0.0): Double {
        val longBits = sharedPreferences.getLong(key, java.lang.Double.doubleToRawLongBits(default))
        return java.lang.Double.longBitsToDouble(longBits)
    }

    // -------------------- LONG --------------------
    /**
     * Save a long
     * Example:
     * tinyDB.putLong("timestamp", System.currentTimeMillis())
     */
    fun putLong(key: String, value: Long) {
        sharedPreferences.edit { putLong(key, value) }
    }

    /**
     * Get a long
     * Example:
     * val ts = tinyDB.getLong("timestamp", 0L)
     */
    fun getLong(key: String, default: Long = 0L): Long {
        return sharedPreferences.getLong(key, default)
    }

    // -------------------- SET STRING --------------------
    /**
     * Save a set of strings
     * Example:
     * tinyDB.putStringSet("tags", setOf("fun","work"))
     */
    fun putStringSet(key: String, set: Set<String>) {
        sharedPreferences.edit { putStringSet(key, set) }
    }

    /**
     * Get a set of strings
     * Example:
     * val tags = tinyDB.getStringSet("tags", emptySet())
     */
    fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String> {
        return sharedPreferences.getStringSet(key, default) ?: default
    }

    // -------------------- REMOVE / CLEAR --------------------
    /**
     * Remove a single key
     * Example:
     * tinyDB.remove("username")
     */
    fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
    }

    /**
     * Clear all keys
     * Example:
     * tinyDB.clearAll()
     */
    fun clearAll() {
        sharedPreferences.edit { clear() }
    }
}