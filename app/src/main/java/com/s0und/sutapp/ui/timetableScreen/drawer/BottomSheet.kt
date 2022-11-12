package com.s0und.sutapp.ui.timetableScreen.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.s0und.sutapp.parser.Message
import com.s0und.sutapp.states.TimetableState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetDrawer(viewModel: TimetableState, modifier: Modifier, messages: List<Message>) {
    val state = rememberModalBottomSheetState(initialValue = viewModel.bottomSheetState.value)


    ModalBottomSheetLayout(
        sheetBackgroundColor = MaterialTheme.colors.background,
        sheetElevation = 8.dp,
        sheetShape = CircleShape,
        sheetState = state,
        sheetContent = {

            LazyColumn(modifier = modifier
                .background(MaterialTheme.colors.background),
                contentPadding = PaddingValues(bottom = 8.dp)
            )

            { items(messages) { message ->
                MessageCard(message)
            }
            }
        }
    ) {

    }

}

@Composable
fun MessageCard(message: Message) {
    Card {
        Column {
            Row {
                Text(message.date)
                Text(message.header)
            }
            Text(message.content)
            for (i in message.files) {
                Text(i.key)
            }
        }

    }
}