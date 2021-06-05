package hsk.practice.myvoca.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import hsk.practice.myvoca.R
import hsk.practice.myvoca.setNightMode

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<SwitchPreferenceCompat>("dark_mode")?.setOnPreferenceChangeListener { preference, newValue ->
            setNightMode(newValue as Boolean)
            true
        }

        findPreference<Preference>("feedback")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, getString(R.string.dev_email))
                putExtra(Intent.EXTRA_SUBJECT, "[MyVoca] 버그 및 개선 사항")
                putExtra(Intent.EXTRA_TEXT, "기기명: \n안드로이드 버전: \n내용: ")
            }
            startActivity(intent)
            true
        }
    }
}