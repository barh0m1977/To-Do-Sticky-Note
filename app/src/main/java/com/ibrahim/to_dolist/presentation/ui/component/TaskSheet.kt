package com.ibrahim.to_dolist.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.animation.AnimatedPlaceholder
import com.ibrahim.to_dolist.core.utility.LocaleHelper.isLeesThan
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.util.TaskSheetType
import com.ibrahim.to_dolist.ui.theme.ToDoListTheme

@Composable
fun TaskSheet(
    modifier: Modifier = Modifier,
    type: TaskSheetType = TaskSheetType.CREATE,
    task: ToDo? = null,
    onTaskAction: (ToDo) -> Unit = {}
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var selectedSticky by remember { mutableStateOf(task?.cardColor ?: ToDoStickyColors.SUNRISE) }
    var selectedState by remember { mutableStateOf(task?.state ?: ToDoState.PENDING) }
    var secureTask by remember { mutableStateOf(task?.locked ?: false) }
    val stickyColors = ToDoStickyColors.values()
    val states = ToDoState.values().toList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(12.dp, 12.dp, 12.dp, 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (type == TaskSheetType.CREATE) {
                stringResource(R.string.new_task)
            } else {
                stringResource(R.string.edit_task)
            },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Title",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { AnimatedPlaceholder(textFieldValue = title) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = Color.Transparent,
            ),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Color Label",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            stickyColors.forEach { sticky ->
                val previewColor = sticky.listColor.first()
                val isSelected = sticky == selectedSticky

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(previewColor)
                        .clickable { selectedSticky = sticky },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "State",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        SegmentedButton(
            segments = states,
            selectedSegment = selectedState,
            onSegmentSelected = { selectedState = it }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = "Secure Task",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Secure Task",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Lock with biometrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = secureTask,
                onCheckedChange = { secureTask = it },
                colors = SwitchDefaults.colors(
                    MaterialTheme.colorScheme.inversePrimary,
                    MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surface
                )
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (isLeesThan(title)) {
                    val resultTodo = task?.copy(
                        title = title,
                        cardColor = selectedSticky,
                        state = selectedState,
                        locked = secureTask
                    ) ?: ToDo(
                        title = title,
                        cardColor = selectedSticky,
                        state = selectedState,
                        locked = secureTask
                    )
                    onTaskAction(resultTodo)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A9E2A))
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Create Task",
                tint = MaterialTheme.colorScheme.inversePrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (type == TaskSheetType.CREATE) {
                    stringResource(R.string.create_task)
                }else{
                    stringResource(R.string.update)
                },
                color = MaterialTheme.colorScheme.inversePrimary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SegmentedButton(
    segments: List<ToDoState>,
    selectedSegment: ToDoState,
    onSegmentSelected: (ToDoState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .height(IntrinsicSize.Min)
    ) {
        segments.forEach { segment ->
            val isSelected = segment == selectedSegment
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                    )
                    .clickable { onSegmentSelected(segment) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                ToDoStateLabel(state = segment, isSelected = isSelected) {
                    onSegmentSelected(it)
                }
//                Text(
//                    text = segment.name,
//                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
//                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TaskSheetPreview() {
    ToDoListTheme {
        TaskSheet(type = TaskSheetType.UPDATE, onTaskAction = {})
    }
}
