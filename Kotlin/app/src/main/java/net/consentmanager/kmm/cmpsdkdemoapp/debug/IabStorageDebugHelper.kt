package net.consentmanager.kmm.cmpsdkdemoapp.debug

import android.content.Context
import android.content.SharedPreferences

/**
 * Debug helpers that mutate/inspect the same SharedPreferences file the CMP SDK uses.
 * Behavior mirrors docs/cm-tcstring-tests Android native MainActivity.
 */
object IabStorageDebugHelper {
    const val TC_STRING_KEY = "IABTCF_TCString"
    const val CMP_ID_KEY = "IABTCF_CmpSdkID"
    const val CORRUPT_TC_STRING = "+=========---------......."
    const val CORRUPT_CMP_ID = 999999

    fun preferencesName(packageName: String): String = "${packageName}_preferences"

    fun preferences(context: Context): SharedPreferences =
        context.getSharedPreferences(preferencesName(context.packageName), Context.MODE_PRIVATE)

    data class CorruptResult(
        val committed: Boolean,
        val beforeTcString: Any?,
        val beforeCmpSdkId: Any?,
        val afterTcString: Any?,
        val afterCmpSdkId: Any?,
    )

    data class PrefEntry(
        val key: String,
        val value: Any?,
        val typeName: String,
    )

    fun corruptIabStorage(preferences: SharedPreferences): CorruptResult {
        val beforeTcString = preferences.all[TC_STRING_KEY]
        val beforeCmpSdkId = preferences.all[CMP_ID_KEY]
        val committed = preferences.edit()
            .putString(TC_STRING_KEY, CORRUPT_TC_STRING)
            .putInt(CMP_ID_KEY, CORRUPT_CMP_ID)
            .commit()
        val afterAll = preferences.all
        return CorruptResult(
            committed = committed,
            beforeTcString = beforeTcString,
            beforeCmpSdkId = beforeCmpSdkId,
            afterTcString = afterAll[TC_STRING_KEY],
            afterCmpSdkId = afterAll[CMP_ID_KEY],
        )
    }

    fun dumpPrefs(preferences: SharedPreferences): List<PrefEntry> {
        val all = preferences.all
        return all.keys.sorted().map { key ->
            val value = all[key]
            PrefEntry(
                key = key,
                value = value,
                typeName = value?.javaClass?.name ?: "null",
            )
        }
    }

    fun formatCorruptLogLines(result: CorruptResult): List<String> = listOf(
        "IAB corrupt before $TC_STRING_KEY=${result.beforeTcString} " +
            "type=${result.beforeTcString?.javaClass?.name ?: "null"}",
        "IAB corrupt before $CMP_ID_KEY=${result.beforeCmpSdkId} " +
            "type=${result.beforeCmpSdkId?.javaClass?.name ?: "null"}",
        "IAB corrupt write committed=${result.committed}",
        "IAB corrupt after $TC_STRING_KEY=${result.afterTcString} " +
            "type=${result.afterTcString?.javaClass?.name ?: "null"}",
        "IAB corrupt after $CMP_ID_KEY=${result.afterCmpSdkId} " +
            "type=${result.afterCmpSdkId?.javaClass?.name ?: "null"}",
    )

    fun formatDumpLogLines(entries: List<PrefEntry>): List<String> {
        val header = listOf("dumpPrefs start count=${entries.size}")
        val body = entries.map { entry ->
            "pref key=${entry.key} value=${entry.value} type=${entry.typeName}"
        }
        val footer = listOf("dumpPrefs complete count=${entries.size}")
        return header + body + footer
    }
}
