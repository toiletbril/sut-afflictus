package com.s0und.sutapp.ui

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.s0und.sutapp.R
import com.s0und.sutapp.states.InitState
import com.s0und.sutapp.states.InitUIState
import com.s0und.sutapp.states.TimetableState
import com.s0und.sutapp.ui.theme.SlightBonchBlue

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun InitScreen (
    viewModel: InitState,
    timetableState: TimetableState,
    modifier: Modifier = Modifier,
) { Scaffold(modifier.animateContentSize()) {
        when (viewModel.uiState.value) {
            InitUIState.NotLoaded -> viewModel.getGroups()
            InitUIState.IsLoading -> {
                InitLoading(modifier)
            }
            InitUIState.ContentIsLoaded -> {
                GroupPicker(viewModel, modifier)
            }
            InitUIState.GroupPicked -> {
                viewModel.changeState(InitUIState.NotLoaded)
                timetableState.loadTimetable()
            }
            InitUIState.IsError -> {
                InitErrorText(viewModel.errorMessage.value, viewModel, modifier)
            }
        }
    }
}

@Composable
fun GroupPicker(viewModel: InitState, modifier: Modifier) {

    val groups = viewModel.groups.collectAsState().value
    val groupNames = mutableSetOf<String>()
    val groupIDs = mutableSetOf<String>()

    for (i in groups.indices) {
        groupNames.add(groups[i].name.uppercase())
        groupIDs.add(groups[i].ID)
    }

    LaunchedEffect(viewModel.searchText.value) {
        val groupIndex = groupNames.indexOf(viewModel.searchText.value)
        if (groupIndex != -1) {
            viewModel.isGroupFound(
                true,
                groupIDs.elementAt(groupIndex),
                groupNames.elementAt(groupIndex)
            )
        } else viewModel.isGroupFound(false)
    }

    Row(modifier = modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

            Text(text = stringResource(R.string.WELCOME), style = MaterialTheme.typography.h3, fontWeight = FontWeight.Black)
            Text(
                modifier = modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp),
                text = stringResource(R.string.CHOOSEYOURGROUP),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondaryVariant
            )
            var textValue by remember { mutableStateOf(TextFieldValue("")) }
            TextField(
                modifier = modifier
                    .padding(vertical = 16.dp)
                    .requiredWidthIn(330.dp, 330.dp),
                value = textValue,
                onValueChange = {newValue ->
                    if (textValue.text.length >= 9) {
                        if (newValue.text.length < textValue.text.length) textValue = newValue
                    } else textValue = newValue
                    viewModel.setSearchText(textValue.text.uppercase().replace(" ", ""))
                    },
                label = { Text (
                    text = stringResource(R.string.GROUP),
                    style = MaterialTheme.typography.body2
                    ) },
                placeholder = { Text("АБВГ-12" )},
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                textStyle = MaterialTheme.typography.body1,
                trailingIcon = {CheckIcon(viewModel, modifier)},
            )
            Button(
                onClick = {
                    viewModel.groupFound()
                },
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 6.dp),
                shape = RoundedCornerShape(32.dp),
                enabled = viewModel.groupFound.value,
            ) {
                Text(
                    text = stringResource(R.string.READY),
                    fontWeight = FontWeight.Bold,
                    modifier = modifier.padding(vertical = 6.dp))
            }
        }
    }
}

@Composable
fun CheckIcon(viewModel: InitState, modifier: Modifier) {
    val alpha = if (viewModel.groupFound.value) 1f else 0f
    Icon(
        painter = painterResource(R.drawable.checkicon),
        contentDescription = "Group found",
        modifier = modifier
            .size(24.dp)
            .alpha(alpha),
    )
}

@Composable
fun InitLoading(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(MaterialTheme.colors.background),
    ) {
                CircularProgressIndicator(modifier = modifier
                    .size(50.dp),
                    color = SlightBonchBlue,
                    strokeWidth = 4.dp)
    }
}


@Composable
fun InitErrorText(errorMsg: String, viewModel: InitState, modifier: Modifier) {
    val noInternet = "java.net.UnknownHostException: Unable to resolve host \"www.sut.ru\": No address associated with hostname"
    val text = "${stringResource(R.string.ERROR)}:\n${ if (errorMsg != noInternet) errorMsg else stringResource(R.string.NO_INTERNET)}"
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = modifier
            .padding(top = 250.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center
            )
        }
        IconButton(modifier = modifier
            .padding(end = 0.dp, top = 20.dp, bottom = 0.dp)
            .size(40.dp),
            onClick = { viewModel.getGroups() }
        ) { Icon(painter = painterResource(R.drawable.syncicon), contentDescription = "refresh") }
    }
}
