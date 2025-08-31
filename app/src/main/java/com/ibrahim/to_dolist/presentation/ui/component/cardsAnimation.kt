package com.ibrahim.to_dolist.presentation.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import com.ibrahim.to_dolist.core.utility.BiometricHelper
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.presentation.ui.component.CardWithCalendar
import com.ibrahim.to_dolist.presentation.ui.component.TaskDialog
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

// ====================== Swipe Stack ======================
@Composable
fun TinderCardStack(
    cards: MutableList<ToDo>,
    maxVisible: Int = 3,
    cardWidth: Float = 300f,
    cardHeight: Float = 200f,
    viewModel: ToDoViewModel,
    onSwiped: (ToDo, Direction) -> Unit
) {

    val scope = rememberCoroutineScope()
    val offsetXState = remember { mutableStateOf(0f) }
    val offsetYState = remember { mutableStateOf(0f) }
    val dragFraction = remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .width(cardWidth.dp)
            .height(cardHeight.dp),
        contentAlignment = Alignment.Center
    ) {
        val visibleCards = cards.takeLast(maxVisible)
        visibleCards.forEachIndexed { index, card ->
            val isTop = index == visibleCards.lastIndex
            val backIndex = visibleCards.lastIndex - index
            val scale = 1f - backIndex * 0.05f
            val translateY = backIndex * 20f

            key(card.id) {
                if (isTop) {
                    AdvancedSwipeCard(
                        card = card,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        scale = scale,
                        translateY = translateY,
                        offsetXState = offsetXState,
                        offsetYState = offsetYState,
                        dragFraction = dragFraction,
                        onSwiped = { dir ->
                            scope.launch {
                                cards.remove(card)
                                cards.add(0, card) // رجع الكارد للأسفل
                                offsetXState.value = 0f
                                offsetYState.value = 0f
                                dragFraction.value = 0f
                                onSwiped(card, dir)
                            }
                        },viewModel
                    )
                } else {
                    AnimatedBackCard(
                        card = card,
                        scale = scale,
                        translateY = translateY,
                        dragFraction = dragFraction.value,
                        zIndex = backIndex.toFloat(),
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedBackCard(
    card: ToDo,
    scale: Float,
    translateY: Float,
    dragFraction: Float,
    zIndex: Float,
    cardWidth: Float,
    cardHeight: Float,
    viewModel: ToDoViewModel
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    // 🟢 انيميشن للانتقال المرن
    val animScale by animateFloatAsState(
        targetValue = scale + dragFraction * 0.05f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    val animTranslateY by animateFloatAsState(
        targetValue = translateY - dragFraction * 20f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val animTranslateX by animateFloatAsState(
        targetValue = dragFraction * 15f,
        animationSpec = tween(durationMillis = 1000)
    )
    val animAlpha by animateFloatAsState(
        targetValue = 1f - (dragFraction * 0.2f),
        animationSpec = tween(durationMillis = 1000)
    )

    Box(
        modifier = Modifier
            .width(cardWidth.dp)
            .height(cardHeight.dp)
            .zIndex(-zIndex) // دايمًا خلف
            .graphicsLayer {
                scaleX = animScale
                scaleY = animScale
                translationY = animTranslateY
                translationX = animTranslateX
                alpha = animAlpha
                shadowElevation = 12f
                clip = true
                shape = RoundedCornerShape(28.dp)
            }
            .background(card.cardColor.listColor.first()),
        contentAlignment = Alignment.Center
    ) {
        CardWithCalendar(
            modifier = Modifier
                .clickable {
                    if (card.locked) {
                        if (activity != null) {
                            BiometricHelper(
                                activity = activity,
                                onSuccess = {
                                    viewModel.selectToDo(card)
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            ).authenticate()
                        } else {
                            Toast.makeText(context, "Activity is null", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        viewModel.selectToDo(card)
                    }
                }
                .fillMaxSize(),
            text = card.title,
            colorArray = card.cardColor,
            state = card.state,
            onDeleteConfirmed = { viewModel.deleteToDoLocal(card) },
            onEditConfirmed = { updatedToDo ->
                viewModel.updateToDo(
                    card.copy(
                        title = updatedToDo.title,
                        cardColor = updatedToDo.cardColor,
                        state = updatedToDo.state,
                        locked = updatedToDo.locked
                    )
                )
            },
            onClick = { },
            isLocked = card.locked
        )

    }
}

@Composable
fun AdvancedSwipeCard(
    card: ToDo,
    cardWidth: Float,
    cardHeight: Float,
    scale: Float,
    translateY: Float,
    offsetXState: MutableState<Float>,
    offsetYState: MutableState<Float>,
    dragFraction: MutableState<Float>,
    onSwiped: (Direction) -> Unit,
    viewModel: ToDoViewModel
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation by animateFloatAsState(targetValue = (offsetX.value / 20f).coerceIn(-30f, 30f))
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val selectedToDo by viewModel.selectedToDo.collectAsState()

    Box(
        modifier = Modifier
            .zIndex(100f) // 🟢 الكارد العلوي فوق
            .width(cardWidth.dp)
            .height(cardHeight.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = translateY + offsetY.value
                translationX = offsetX.value
                rotationZ = rotation
                shadowElevation = 20f
                clip = true
                shape = RoundedCornerShape(28.dp)
            }
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = card.cardColor.listColor
                )
            )
            .pointerInput(card.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                            offsetXState.value = offsetX.value
                            offsetYState.value = offsetY.value
                            dragFraction.value =
                                (hypot(offsetX.value, offsetY.value) / 600f).coerceIn(0f, 1f)
                        }
                    },
                    onDragEnd = {
                        val thresholdX = cardWidth * 0.25f
                        val thresholdY = cardHeight * 0.2f
                        val x = offsetX.value
                        val y = offsetY.value

                        scope.launch {
                            when {
                                abs(x) > thresholdX && abs(x) > abs(y) -> {
                                    val dir = if (x > 0) Direction.RIGHT else Direction.LEFT
                                    offsetX.animateTo(
                                        targetValue = if (x > 0) cardWidth * 1.5f else -cardWidth * 1.5f,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                                    )
                                    onSwiped(dir)
                                }

                                abs(y) > thresholdY -> {
                                    val dir = if (y > 0) Direction.DOWN else Direction.UP
                                    offsetY.animateTo(
                                        targetValue = if (y > 0) cardHeight * 1.5f else -cardHeight * 1.5f,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                                    )
                                    onSwiped(dir)
                                }

                                else -> {
                                    offsetX.animateTo(0f, tween(200))
                                    offsetY.animateTo(0f, tween(200))
                                    dragFraction.value = 0f
                                }
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.TopStart
    ) {
        CardWithCalendar(
            modifier = Modifier
                .clickable {
                    if (card.locked) {
                        if (activity != null) {
                            BiometricHelper(
                                activity = activity,
                                onSuccess = {
                                    viewModel.selectToDo(card)
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            ).authenticate()
                        } else {
                            Toast.makeText(context, "Activity is null", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        viewModel.selectToDo(card)
                    }
                }
                .fillMaxSize(),
            text = card.title,
            colorArray = card.cardColor,
            state = card.state,
            onDeleteConfirmed = { viewModel.deleteToDoLocal(card) },
            onEditConfirmed = { updatedToDo ->
                viewModel.updateToDo(
                    card.copy(
                        title = updatedToDo.title,
                        cardColor = updatedToDo.cardColor,
                        state = updatedToDo.state,
                        locked = updatedToDo.locked
                    )
                )
            },
            onClick = { },
            isLocked = card.locked
        )
    }
    selectedToDo?.let { todo ->
        TaskDialog(
            todoTitle = todo.title,
            todoId = todo.id,
            viewModel = viewModel,
            onAddSubTask = { newText -> viewModel.addTask(todo.id, newText) },
            onUpdateSubTask = { updatedTask -> viewModel.updateTask(updatedTask) },
            onDeleteSubTask = { taskToDelete -> viewModel.deleteTask(taskToDelete) },
            onDismiss = { viewModel.clearSelectedToDo() }
        )
    }
}

enum class Direction {
    LEFT, RIGHT, UP, DOWN
}
