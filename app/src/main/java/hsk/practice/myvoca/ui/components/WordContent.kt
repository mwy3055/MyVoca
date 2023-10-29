package hsk.practice.myvoca.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hsk.practice.myvoca.data.MeaningImpl
import hsk.practice.myvoca.data.VocabularyImpl
import hsk.practice.myvoca.data.WordClassImpl
import hsk.practice.myvoca.ui.theme.MyVocaTheme
import hsk.practice.myvoca.ui.theme.Paybooc
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
    val padding = 8.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(padding)
    ) {
        if (word.id != 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WordTitle(
                    title = word.eng,
                    modifier = Modifier.weight(1f)
                )
                iconContent()
            }
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
    Text(
        modifier = modifier,
        text = title,
        fontFamily = Paybooc,
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
    val meaningsTruncated = meanings.size >= 3
    val canExpand = showExpandButton and meaningsTruncated

    val (firstMeanings, lastMeanings) = getTruncatedMeanings(meaningsTruncated, meanings)
    Row(
        modifier = if (canExpand) modifier.clickable { onClick(expanded) } else modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            firstMeanings.forEach { meaning ->
                WordMeaning(meaning = meaning)
            }
            if (expanded) {
                lastMeanings.forEach { meaning ->
                    WordMeaning(meaning = meaning)
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        background = MaterialTheme.colorScheme.primary,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    append(meaning.type.korean.first())
                }
            },
            fontFamily = Paybooc
        )
        Text(
            text = meaning.content,
            fontFamily = Paybooc
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