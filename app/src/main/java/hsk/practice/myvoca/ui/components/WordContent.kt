package hsk.practice.myvoca.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.*
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
import hsk.practice.myvoca.util.toTimeString

@Composable
fun WordContent(
    word: VocabularyImpl,
    iconContent: @Composable RowScope.() -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    WordContent(
        word = word,
        expanded = expanded,
        onExpanded = { expanded = !it },
        iconContent = iconContent
    )
}

@Composable
fun WordContent(
    word: VocabularyImpl,
    showExpandButton: Boolean = true,
    expanded: Boolean,
    onExpanded: (Boolean) -> Unit,
    iconContent: @Composable RowScope.() -> Unit = {}
) {
    val padding = 8.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
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
        style = MaterialTheme.typography.h5,
    )
}

@Composable
fun WordMeanings(
    meanings: List<MeaningImpl>,
    modifier: Modifier = Modifier,
    showExpandButton: Boolean = true,
    expanded: Boolean = false,
    onClick: (Boolean) -> Unit = {}
) {
    val meaningsTruncated = meanings.size >= 3
    val (firstMeanings, lastMeanings) = if (meaningsTruncated) {
        Pair(meanings.subList(0, 2), meanings.subList(2, meanings.size))
    } else {
        Pair(meanings, emptyList())
    }

    Row(
        modifier = if (meaningsTruncated) modifier.clickable { onClick(expanded) } else modifier
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

        if (meaningsTruncated and showExpandButton) {
            val iconAngle by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
            )
            IconButton(
                onClick = { onClick(expanded) },
                modifier = Modifier
//                    .size(40.dp)
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
                        background = MaterialTheme.colors.primary,
                        color = MaterialTheme.colors.onPrimary
                    )
                ) {
                    append(meaning.type.korean.first())
                }
            }
        )
        Text(
            text = meaning.content
        )
    }
}

@Composable
fun WordMetadataShort(addedTime: Long, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "${addedTime.toTimeString()} 추가",
            style = MaterialTheme.typography.caption,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
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
        ),
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