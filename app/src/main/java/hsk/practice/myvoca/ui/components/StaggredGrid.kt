package hsk.practice.myvoca.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import kotlin.math.max
import kotlin.math.min

@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        val sizes = mutableListOf<Size>()
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints)
            sizes.add(Size(placeable.width, placeable.height))
            placeable
        }

        val sumOfWidth = sizes.fold(0) { sum, size -> sum + size.width }
        val width = min(sumOfWidth, constraints.maxWidth)

        // Calculate x, y coordinate of each placeable
        var rowX = 0
        var rowY = 0
        var maxHeight = 0
        val places = mutableListOf<Place>()
        sizes.forEach { size ->
            if (rowX + size.width > width) {
                rowX = 0
                rowY += maxHeight
                maxHeight = size.height
            } else {
                maxHeight = max(maxHeight, size.height)
            }
            places.add(Place(rowX, rowY))
            rowX += size.width
        }

        // Place each placeable
        val height = rowY + maxHeight
        layout(width, height) {
            places.zip(placeables).forEach { (place, placeable) ->
                placeable.place(
                    x = place.x,
                    y = place.y,
                )
            }
        }
    }
}

private data class Size(
    val width: Int,
    val height: Int,
)

private data class Place(
    val x: Int,
    val y: Int,
)