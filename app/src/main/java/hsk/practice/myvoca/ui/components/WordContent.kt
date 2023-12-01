package hsk.practice.myvoca.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun WordContent(
    word: VocabularyImpl,
    modifier: Modifier = Modifier,
    iconContent: @Composable RowScope.() -> Unit = {}
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    WordContent(
        modifier = modifier,
        word = word,
        expanded = expanded,
        onExpanded = { expanded = !it },
        iconContent = iconContent
    )
}

@Composable
fun WordContent(
    word: VocabularyImpl,
    expanded: Boolean,
    onExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showExpandButton: Boolean = true,
    iconContent: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
    ) {
        if (word.id != 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WordTitle(
                    title = word.eng,
                    modifier = Modifier.weight(1f)
                )
                iconContent()
            }
            Spacer(modifier = Modifier.height(4.dp))
            WordMeanings(
                meanings = word.meaning,
                showExpandButton = showExpandButton,
                expanded = expanded,
                onClick = onExpanded
            )
        }
    }
}

@Composable
fun WordTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    MyVocaText(
        modifier = modifier,
        text = title,
        style = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
fun WordMeanings(
    meanings: ImmutableList<MeaningImpl>,
    modifier: Modifier = Modifier,
    showExpandButton: Boolean = true,
    expanded: Boolean = false,
    onClick: (Boolean) -> Unit = {}
) {
    val textStyle = LocalTextStyle.current
    val meaningsTruncated = meanings.size >= 3
    val canExpand = showExpandButton and meaningsTruncated

    val (firstMeanings, lastMeanings) = getTruncatedMeanings(meaningsTruncated, meanings)
    Row(
        modifier = if (canExpand) modifier.clickable { onClick(expanded) }
        else modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            firstMeanings.forEach { meaning ->
                WordMeaning(
                    meaning = meaning,
                    textStyle = textStyle
                )
            }
            if (expanded) {
                lastMeanings.forEach { meaning ->
                    WordMeaning(
                        meaning = meaning,
                        textStyle = textStyle
                    )
                }
            }
        }

        if (canExpand) {
            val iconAngle by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
            )
            IconButton(
                onClick = { onClick(expanded) },
                modifier = Modifier
                    .align(Alignment.Bottom)
            ) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(iconAngle)
                )
            }
        }
    }
}

private fun getTruncatedMeanings(meaningsTruncated: Boolean, meanings: List<MeaningImpl>) =
    if (meaningsTruncated) {
        Pair(meanings.subList(0, 2), meanings.subList(2, meanings.size))
    } else {
        Pair(meanings, emptyList())
    }


@Composable
fun WordMeaning(
    meaning: MeaningImpl,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            MyVocaText(
                text = meaning.type.korean.first().toString(),
                color = MaterialTheme.colorScheme.onSecondary,
                style = textStyle
            )
        }
        MyVocaText(
            text = meaning.content,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = textStyle,
        )
    }
}

@Preview
@Composable
fun WordContentPreview() {
    val currentTime = System.currentTimeMillis()
    val word = VocabularyImpl(
        id = 0,
        eng = "test",
        meaning = listOf(
            MeaningImpl(WordClassImpl.NOUN, "(지식 등을 알아보기 위한) 시험"),
            MeaningImpl(WordClassImpl.NOUN, "(의료적인) 검사"),
            MeaningImpl(WordClassImpl.VERB, "시험하다"),
        ).toImmutableList(),
        addedTime = currentTime,
        lastEditedTime = currentTime,
        memo = ""
    )

    var expanded by remember { mutableStateOf(false) }
    MyVocaTheme {
        WordContent(
            word = word,
            expanded = expanded,
            onExpanded = { expanded = !it }
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null
            )
        }
    }
}