package com.s0und.sutapp.ui.timetableScreen.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val ctx = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(vertical = 64.dp)
    ) {
        Text(
            text = stringResource(R.string.ACCOUNT),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.ExtraBold,
            modifier = modifier
                .padding(bottom = 16.dp)
        )

        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.height(300.dp)
        ) {
            AccountOptions(viewModel, modifier, ctx)
        }

        Spacer(modifier.weight(1f))
//        Divider(
//            modifier = modifier.padding(horizontal = 32.dp, vertical = 8.dp),
//            color = MaterialTheme.colors.secondaryVariant,
//            thickness = 1.dp
//        )
        GroupSelector(viewModel, modifier)

        Spacer(modifier.weight(1f))

        Row {
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
                            modifier = modifier
                                .padding(end = 12.dp, start = 4.dp)
                        )
                        Text(
                            text = "1.1 alpha",
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
        shape = CircleShape,
        modifier = modifier
            .padding(bottom = 0.dp)
            .requiredWidth(256.dp)
    ) {
        Text(
            text = text,
            color = BonchBlue,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            modifier = modifier
                .padding(horizontal = 10.dp)
        )
    }
}
