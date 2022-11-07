package com.s0und.sutapp.ui.timetableScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.s0und.sutapp.R
import com.s0und.sutapp.data.openGithubURL
import com.s0und.sutapp.states.TimetableState
import com.s0und.sutapp.ui.theme.BonchBlue

@Composable
fun TimetableDrawer(
    viewModel: TimetableState,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(vertical = 64.dp)
    ) {
        Text(
            text = stringResource(R.string.SETTINGS),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.ExtraBold,
            modifier = modifier
                .padding(bottom = 16.dp)
        )

        //GroupSelector(viewModel, modifier)

        Text(
            text = stringResource(R.string.WIP) + " uwu",
            color = MaterialTheme.colors.secondaryVariant,
            style = MaterialTheme.typography.body1,
        )

        Spacer(modifier.weight(1f))

//        TextButton(
//            onClick = { /*TODO*/ },
//            colors = ButtonDefaults.outlinedButtonColors(
//                contentColor = BonchBlue
//            )
//        ) {
//            Text(
//                text = stringResource(R.string.ABOUTAPP),
//                color = MaterialTheme.colors.secondaryVariant
//            )
//        }

        Row {
            val ctx = LocalContext.current
            Box (
                modifier = modifier
                    .clickable { openGithubURL(ctx) }
                    ) {
                    Row {
                        Icon(
                            painterResource(R.drawable.github),
                            "Github",
                            tint = BonchBlue,
                            modifier = modifier
                                .size(16.dp)
                                .offset(y = 1.dp)
                        )
                        Text(
                            text = "Github",
                            color = BonchBlue,
                            style = MaterialTheme.typography.body2,
                            //fontWeight = FontWeight.SemiBold,
                            modifier = modifier
                                .padding(end = 12.dp, start = 4.dp)
                        )
                        Text(
                            text = "1.0beta",
                            color = MaterialTheme.colors.secondaryVariant,
                            style = MaterialTheme.typography.body2,
                        )
                    }
                }
            }
        }
    }

@Composable
private fun GroupSelector(viewModel: TimetableState, modifier: Modifier) {
    GroupChangeButton(viewModel.group1.value[0], viewModel.group1.value[1], viewModel, modifier)
    GroupChangeButton(viewModel.group2.value[0], viewModel.group2.value[1], viewModel, modifier)
    GroupChangeButton(viewModel.group3.value[0], viewModel.group3.value[1], viewModel, modifier)
}

@Composable
fun GroupChangeButton(groupID: String, groupName: String, viewModel: TimetableState, modifier: Modifier) {

    val groupSet = groupID.isNotEmpty()
    val isCurrentGroup = groupID != viewModel.selectedGroupID.value
    val text = if (groupSet) groupName else stringResource(R.string.ADDGROUP)

    OutlinedButton(
        onClick = {
            if (groupSet) viewModel.changeGroup(groupID, groupName)
            else Unit
        },
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BonchBlue
        ),
        enabled = isCurrentGroup,
        modifier = modifier
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = text,
            color = BonchBlue,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            modifier = modifier
                .requiredWidth(256.dp)
                .requiredHeight(36.dp)

        )
    }
}