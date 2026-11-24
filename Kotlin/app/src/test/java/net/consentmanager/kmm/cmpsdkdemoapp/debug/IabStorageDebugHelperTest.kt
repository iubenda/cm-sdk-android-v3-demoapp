package net.consentmanager.kmm.cmpsdkdemoapp.debug

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IabStorageDebugHelperTest {

    @Test
    fun preferencesName_matchesSdkConvention() {
        assertEquals(
            "net.consentmanager.kmm.cmpsdkdemoapp_preferences",
            IabStorageDebugHelper.preferencesName("net.consentmanager.kmm.cmpsdkdemoapp"),
        )
    }

    @Test
    fun corruptIabStorage_writesExpectedValues_whenKeysAbsent() {
        val prefs = InMemorySharedPreferences()
        val result = IabStorageDebugHelper.corruptIabStorage(prefs)

        assertTrue(result.committed)
        assertEquals(null, result.beforeTcString)
        assertEquals(null, result.beforeCmpSdkId)
        assertEquals(IabStorageDebugHelper.CORRUPT_TC_STRING, result.afterTcString)
        assertEquals(IabStorageDebugHelper.CORRUPT_CMP_ID, result.afterCmpSdkId)
        assertEquals(
            IabStorageDebugHelper.CORRUPT_TC_STRING,
            prefs.getString(IabStorageDebugHelper.TC_STRING_KEY, null),
        )
        assertEquals(
            IabStorageDebugHelper.CORRUPT_CMP_ID,
            prefs.getInt(IabStorageDebugHelper.CMP_ID_KEY, -1),
        )
    }

    @Test
    fun corruptIabStorage_reportsBeforeAfter_whenKeysPresent() {
        val prefs = InMemorySharedPreferences()
        prefs.edit()
            .putString(IabStorageDebugHelper.TC_STRING_KEY, "C0001")
            .putInt(IabStorageDebugHelper.CMP_ID_KEY, 31)
            .commit()

        val result = IabStorageDebugHelper.corruptIabStorage(prefs)

        assertEquals("C0001", result.beforeTcString)
        assertEquals(31, result.beforeCmpSdkId)
        assertEquals(IabStorageDebugHelper.CORRUPT_TC_STRING, result.afterTcString)
        assertEquals(IabStorageDebugHelper.CORRUPT_CMP_ID, result.afterCmpSdkId)
    }

    @Test
    fun dumpPrefs_returnsSortedEntriesWithTypes() {
        val prefs = InMemorySharedPreferences()
        prefs.edit()
            .putInt("z_key", 1)
            .putString("a_key", "hello")
            .commit()

        val entries = IabStorageDebugHelper.dumpPrefs(prefs)

        assertEquals(listOf("a_key", "z_key"), entries.map { it.key })
        assertEquals("hello", entries[0].value)
        assertEquals(String::class.java.name, entries[0].typeName)
        assertEquals(1, entries[1].value)
        assertEquals(Integer::class.java.name, entries[1].typeName)
    }

    @Test
    fun formatCorruptLogLines_includesCommittedFlag() {
        val result = IabStorageDebugHelper.CorruptResult(
            committed = true,
            beforeTcString = null,
            beforeCmpSdkId = null,
            afterTcString = IabStorageDebugHelper.CORRUPT_TC_STRING,
            afterCmpSdkId = IabStorageDebugHelper.CORRUPT_CMP_ID,
        )
        val lines = IabStorageDebugHelper.formatCorruptLogLines(result)
        assertTrue(lines.any { it.contains("committed=true") })
        assertTrue(lines.any { it.contains(IabStorageDebugHelper.TC_STRING_KEY) })
    }

    /**
     * Minimal SharedPreferences for JVM unit tests (no Robolectric).
     */
    private class InMemorySharedPreferences : SharedPreferences {
        private val data = linkedMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = LinkedHashMap(data)

        override fun getString(key: String?, defValue: String?): String? =
            data[key] as? String ?: defValue

        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
            @Suppress("UNCHECKED_CAST")
            return (data[key] as? Set<String>)?.toMutableSet() ?: defValues
        }

        override fun getInt(key: String?, defValue: Int): Int =
            data[key] as? Int ?: defValue

        override fun getLong(key: String?, defValue: Long): Long =
            data[key] as? Long ?: defValue

        override fun getFloat(key: String?, defValue: Float): Float =
            data[key] as? Float ?: defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean =
            data[key] as? Boolean ?: defValue

        override fun contains(key: String?): Boolean = data.containsKey(key)

        override fun edit(): SharedPreferences.Editor = Editor()

        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?,
        ) = Unit

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?,
        ) = Unit

        private inner class Editor : SharedPreferences.Editor {
            private val pending = linkedMapOf<String, Any?>()
            private val removals = mutableSetOf<String>()
            private var clearAll = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                pending[key!!] = value
                return this
            }

            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?,
            ): SharedPreferences.Editor {
                pending[key!!] = values
                return this
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                pending[key!!] = value
                return this
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                pending[key!!] = value
                return this
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                pending[key!!] = value
                return this
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                pending[key!!] = value
                return this
            }

            override fun remove(key: String?): SharedPreferences.Editor {
                removals.add(key!!)
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                clearAll = true
                return this
            }

            override fun commit(): Boolean {
                apply()
                return true
            }

            override fun apply() {
                if (clearAll) data.clear()
                removals.forEach { data.remove(it) }
                data.putAll(pending)
                pending.clear()
                removals.clear()
                clearAll = false
            }
        }
    }
}
