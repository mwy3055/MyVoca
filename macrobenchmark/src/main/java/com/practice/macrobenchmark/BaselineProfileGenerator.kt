package com.practice.macrobenchmark

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * All baseline profile methods should be run in no-google-api emulator.
 */

@ExperimentalBaselineProfilesApi
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startUp() {
        baselineProfileRule.collectBaselineProfile(packageName = "hsk.practice.myvoca") {
            pressHome()
            // Critical user journey!
            startActivityAndWait()
        }
    }
}