package com.s0und.sutapp.ui.timetableScreen.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Black,
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
