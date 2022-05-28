package com.practice.macrobenchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// TODO: Measure when AGP version is updated to 7.3.0 or above (7.2.0 has a bug related to Baseline Profile)

// TODO: 이거 측정하고 앱 버전 2.0.3으로 업데이트하기

@RunWith(AndroidJUnit4::class)
class BaselineProfileBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupNoCompilation() {
        startup(CompilationMode.None())
    }

    @Test
    fun startupBaselineProfile() {
        startup(
            CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            )
        )
    }

    private fun startup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = "hsk.practice.myvoca",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD,
            compilationMode = compilationMode
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

}