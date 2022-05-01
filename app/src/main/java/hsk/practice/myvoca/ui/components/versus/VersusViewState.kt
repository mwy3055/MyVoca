package hsk.practice.myvoca.ui.components.versus

import androidx.compose.runtime.Stable

@Stable
interface VersusViewState {
    var leftValue: Int
    var rightValue: Int
}