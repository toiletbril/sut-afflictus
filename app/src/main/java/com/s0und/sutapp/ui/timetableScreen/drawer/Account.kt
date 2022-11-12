package com.s0und.sutapp.ui.timetableScreen.drawer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.s0und.sutapp.R
import com.s0und.sutapp.parser.AccountData
import com.s0und.sutapp.states.TimetableState
import com.s0und.sutapp.ui.Loading
import com.s0und.sutapp.ui.theme.BonchBlue
import com.s0und.sutapp.ui.theme.SlightBonchBlue

@Composable
fun AccountOptions(viewModel: TimetableState, modifier: Modifier, ctx: Context) {

    if (viewModel.loggedIn.value) {
        LoggedIn(viewModel, modifier)
    }

    else {
        LoginScreen(viewModel, modifier, ctx)
    }
}

@Composable
private fun LoggedIn(viewModel: TimetableState, modifier: Modifier) {

    val account = remember { mutableStateOf<AccountData?>(null) }

    if (account.value == null)
        Loading(modifier = modifier.offset(y = 32.dp))

    viewModel.getAccount {
        if (it.isSuccess) {
            account.value = it.getOrNull()
        }
    }

    if (account.value != null) {
        val acc = account.value!!

        Text(
            text = acc.accountInfo["name"]!!,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.secondaryVariant,
            modifier = modifier.padding(top = 8.dp)
        )

        Text(
            text = acc.accountInfo["group"]!!,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.secondaryVariant,
            modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        OutlinedButton(
            onClick = {
                viewModel.logOut()
            },
            modifier = modifier
                .width(256.dp)
                .height(64.dp)
                .padding(horizontal = 32.dp, vertical = 12.dp),
            shape = CircleShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BonchBlue
            )
        ) {
            Text(
                text = stringResource(R.string.LOGOUT),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primaryVariant
            )

            //BottomSheetDrawer(viewModel, modifier, acc.messages)
        }
    }
}

@Composable
fun LoginScreen(viewModel: TimetableState, modifier: Modifier, ctx: Context) {

    val focusManager = LocalFocusManager.current

    Text(
        text = stringResource(R.string.LOGIN),
        style = MaterialTheme.typography.body1,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colors.secondaryVariant,
        modifier = modifier.padding(top = 8.dp, bottom = 2.dp)
    )

    var loginValue by remember { mutableStateOf(TextFieldValue("")) }
    var passwordValue by remember { mutableStateOf(TextFieldValue("")) }
    //var rememberMe by remember { mutableStateOf(false) }

    OutlinedTextField(
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = BonchBlue,
            unfocusedBorderColor = MaterialTheme.colors.secondaryVariant,
            textColor = MaterialTheme.colors.secondaryVariant,
            cursorColor = MaterialTheme.colors.primaryVariant,
            focusedLabelColor = MaterialTheme.colors.primaryVariant
        ),
        modifier = modifier
            .width(288.dp),
        value = loginValue,
        onValueChange = {newValue ->
            loginValue = newValue
        },
        label = { Text (
            text = stringResource(R.string.LOGINFIELD),
            style = MaterialTheme.typography.body2
        ) },
        placeholder = { Text("example@mail.com" ) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.body1,
        shape = CircleShape
    )

    OutlinedTextField(
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = BonchBlue,
            unfocusedBorderColor = MaterialTheme.colors.secondaryVariant,
            textColor = MaterialTheme.colors.secondaryVariant,
            cursorColor = MaterialTheme.colors.primaryVariant,
            focusedLabelColor = MaterialTheme.colors.primaryVariant
        ),
        modifier = modifier
            .width(288.dp)
            .padding(bottom = 2.dp),
        value = passwordValue,
        onValueChange = {newValue ->
            passwordValue = newValue
        },
        label = { Text (
            text = stringResource(R.string.PASSWORD),
            style = MaterialTheme.typography.body2
        ) },
        placeholder = { Text(stringResource(R.string.PASSWORDFIELD)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        ),
        textStyle = MaterialTheme.typography.body1,
        visualTransformation = PasswordVisualTransformation(),
        shape = CircleShape
        )

//    Row(
//        modifier = modifier.padding(end = 16.dp)
//    ) {
//        Checkbox(
//            checked = rememberMe,
//            onCheckedChange = { rememberMe = !rememberMe },
//            modifier = modifier.size(42.dp)
//        )
//        Text(
//            text = stringResource(R.string.REMEMBERME),
//            style = MaterialTheme.typography.body2,
//            color = MaterialTheme.colors.secondaryVariant,
//            //fontWeight = FontWeight.SemiBold,
//            modifier = modifier.padding(top = 10.dp)
//        )
//

    Button(
        onClick = {
            viewModel.logIn(loginValue.text, passwordValue.text) {

                if (it.isSuccess) {
                    viewModel.loggedIn.value = true
                }
                val text = if (viewModel.loggedIn.value) ctx.getString(R.string.LOGGEDIN)
                else ctx.getString(R.string.WRONGPASSWORD)

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show()
                }
            }
        },
        modifier = modifier
            .width(264.dp)
            .height(64.dp)
            .padding(horizontal = 32.dp, vertical = 10.dp),
        shape = RoundedCornerShape(32.dp),
        enabled = passwordValue.text.isNotEmpty() && loginValue.text.isNotEmpty(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = SlightBonchBlue
        ),
    ) {
        Text(
            text = stringResource(R.string.READY),
            fontWeight = FontWeight.Bold,
        )
    }
}