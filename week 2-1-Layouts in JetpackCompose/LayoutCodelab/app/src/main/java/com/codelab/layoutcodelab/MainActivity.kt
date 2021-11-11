package com.codelab.layoutcodelab

import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atLeast
import coil.compose.rememberImagePainter
import com.codelab.layoutcodelab.ui.theme.LayoutCodelabTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LayoutCodelabTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    DecoupledConstraintLayout()
                }
            }
        }
    }
}

@Stable
fun Modifier.padding(all: Dp) = this.then(
    PaddingModifier(start = all, top = all, end = all, bottom = all, rtlAware = true)
)

fun Modifier.firstBaselineToTop(
    firstBaselineToTop: Dp
) = this.then(
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints = constraints)

        check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
        val firstBaseline = placeable[FirstBaseline]

        val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
        val height = placeable.height + placeableY
        layout(placeable.width, height) {
            placeable.placeRelative(0, placeableY)
        }
    }
)

private fun decoupledConstraints(margin: Dp): ConstraintSet {
    return ConstraintSet {
        val button = createRefFor("button")
        val text = createRefFor("text")

        constrain(button) {
            top.linkTo(parent.top, margin)
        }
        constrain(text) {
            top.linkTo(button.bottom, margin)
        }
    }
}

@Composable
fun TwoTexts(
    modifier: Modifier = Modifier,
    text1: String,
    text2: String
) {
    Row (modifier = modifier.height(IntrinsicSize.Min)) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
                .wrapContentWidth(Alignment.Start),
            text = text1
        )

        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .wrapContentWidth(Alignment.End),
            text = text2
        )
    }
}

@Preview
@Composable
fun TwoTextsPreview() {
    LayoutCodelabTheme {
        Surface {
            TwoTexts(text1 = "Hi", text2 = "there")
        }
    }
}

@Composable
fun DecoupledConstraintLayout() {
    BoxWithConstraints {
        val constraints = if (maxWidth < maxHeight) {
            decoupledConstraints(16.dp)
        } else {
            decoupledConstraints(32.dp)
        }

        ConstraintLayout(constraintSet = constraints) {
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.layoutId("button")
            ) {
                Text(text = "Button")
            }

            Text("text", Modifier.layoutId("text"))
        }
    }
}

@Preview
@Composable
fun DecoupledConstraintLayoutPreview() {
    LayoutCodelabTheme {
        DecoupledConstraintLayout()
    }
}

@Composable
fun LargeConstraintLayout() {
    ConstraintLayout {
        val text = createRef()

        val guideline = createGuidelineFromStart(fraction = 0.5f)
        Text(
            text = "This is a very very very very very very very long text",
            Modifier.constrainAs(text) {
                linkTo(start = guideline, end = parent.end)
                width = Dimension.preferredWrapContent.atLeast(100.dp)
            }
        )
    }
}

@Preview
@Composable
fun LargeConstraintLayoutPreview() {
    LayoutCodelabTheme {
        LargeConstraintLayout()
    }
}

@Composable
fun ConstraintLayoutContent() {
    ConstraintLayout {
        val (button1, button2, text) = createRefs()

        Button(
            onClick = { /* do something. */},
            modifier = Modifier.constrainAs(button1) {
                top.linkTo(parent.top, margin = 16.dp)
            }
        ) {
            Text(text = "Button1")
        }

        Text(
            text = "Text",
            Modifier.constrainAs(text) {
                top.linkTo(button1.bottom, margin = 16.dp)
                centerHorizontallyTo(parent)
            }
        )

        val barrier = createEndBarrier(button1, text)
        Button(
            onClick = { /* do something. */ },
            modifier = Modifier.constrainAs(button2) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(barrier)
            }
        ) {
            Text(text = "Button2")
        }
    }
}

@Preview
@Composable
fun ConstraintLayoutContentPreview() {
    LayoutCodelabTheme {
        ConstraintLayoutContent()
    }
}

@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    rows: Int = 3,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val rowWidths = IntArray(rows) { 0 }
        val rowHeights = IntArray(rows) { 0 }

        val placeables = measurables.mapIndexed { index, measurable ->
            val placeable = measurable.measure(constraints = constraints)

            val row = index % rows
            rowWidths[row] += placeable.width
            rowHeights[row] = Math.max(rowHeights[row], placeable.height)

            placeable
        }

        val width = rowWidths.maxOrNull()
            ?.coerceIn(constraints.minWidth.rangeTo(constraints.maxWidth)) ?: constraints.minWidth
        val height = rowHeights.sumOf { it }
            .coerceIn(constraints.minHeight.rangeTo(constraints.maxHeight))

        val rowY = IntArray(rows) { 0 }
        for (i in 1 until rows) {
            rowY[i] = rowY[i - 1] + rowHeights[i - 1]
        }

        layout(width, height) {
            val rowX = IntArray(rows) { 0 }

            placeables.forEachIndexed { index, placeable ->
                val row = index % rows
                placeable.placeRelative(
                    x = rowX[row],
                    y = rowY[row]
                )
                rowX[row] += placeable.width
            }
        }
    }
}

@Composable
fun Chip(modifier: Modifier = Modifier, text: String) {
    Card(
        modifier = modifier,
        border = BorderStroke(color = Color.Black, width = Dp.Hairline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp, 16.dp)
                    .background(color = MaterialTheme.colors.secondary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text)
        }
    }
}

@Preview
@Composable
fun ChipPreview() {
    LayoutCodelabTheme {
        Chip(text = "Hi there")
    }
}

@Composable
fun MyOwnColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints = constraints)
        }

        var yPosition = 0

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                placeable.placeRelative(0, yPosition)

                yPosition += placeable.height
            }
        }
    }
}

@Preview
@Composable
fun TextWithPaddingToBaselinePreview() {
    LayoutCodelabTheme {
        Text(
            text = "Hi there !",
            modifier = Modifier.firstBaselineToTop(32.dp)
        )
    }
}

@Preview
@Composable
fun TextWithNormalPaddingPreview() {
    LayoutCodelabTheme {
        Text(
            text = "Hi there !",
            modifier = Modifier.padding(top = 32.dp)
        )
    }
}

@Composable
fun ScrollingList() {
    val listSize = 100
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        Row {
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollToItem(0)
                }
            }) {
                Text(text = "Scroll to the top")
            }

            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollToItem(listSize - 1)
                }
            }) {
                Text(text = "Scroll to end")
            }
        }

        LazyColumn(state = scrollState) {
            items(listSize) {
                ImageListItem(index = it)
            }
        }
    }
}

@Composable
fun ImageList() {
    val scrollState = rememberLazyListState()

    LazyColumn(state = scrollState) {
        items(100) {
            ImageListItem(index = it)
        }
    }
}

@Composable
fun ImageListItem(index: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = rememberImagePainter(
                data = "https://developer.android.com/images/brand/Android_Robot.png"
            ),
            contentDescription = "Android Logo",
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("Item #$index", style = MaterialTheme.typography.subtitle1)
    }
}

@Preview
@Composable
fun ImageListPreview() {
    ImageList()
}

@Composable
fun LazyList() {
    val scrollState = rememberLazyListState()

    LazyColumn(state = scrollState) {
        items(100) {
            Text("Item #$it")
        }
    }
}

@Preview
@Composable
fun LazyListPreview() {
    LazyList()
}

@Composable
fun SimpleList() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.verticalScroll(state = scrollState)
    ) {
        repeat(100) {
            Text(text = "Item #$it")
        }
    }
}

@Composable
fun LayoutCodelab() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "LayoutCodelab")
                },
                actions = {
                    IconButton(onClick = { /* do something. */ }) {
                        Icon(imageVector = Icons.Filled.Favorite, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        BodyContent(Modifier.padding(innerPadding))
    }
}

val topics = listOf(
    "Arts & Crafts", "Beauty", "Books", "Business", "Comics", "Culinary",
    "Design", "Fashion", "Film", "History", "Maths", "Music", "People", "Philosophy",
    "Religion", "Social sciences", "Technology", "TV", "Writing"
)

@Composable
fun BodyContent(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.LightGray, shape = RectangleShape)
            .padding(16.dp)
            .size(200.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        StaggeredGrid {
            for (topic in topics) {
                Chip(
                    modifier = Modifier.padding(8.dp),
                    text = topic
                )
            }
        }
    }
}

@Preview
@Composable
fun LayoutCodelabPreview() {
    LayoutCodelabTheme {
        LayoutCodelab()
    }
}

@Composable
fun PhotographerCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.surface)
            .clickable { /* do nothing. */ }
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
        ) {
            // Image goes here
        }

        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                "Alfred Sisley",
                fontWeight = FontWeight.Bold
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = "3 minutes ago",
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Preview
@Composable
fun PhotographerCardPreview() {
    LayoutCodelabTheme {
        PhotographerCard()
    }
}
