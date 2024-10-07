package fr.coppernic.agridentwedgesampleup.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.coppernic.agridentwedgesampleup.ui.components.TopAppBar

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    isNewScan: Boolean,
    dataIntentRead: String,
    onStartScan: () -> Unit,
    navigateToSetting: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(navigateUp = navigateToSetting)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStartScan) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }
    ) {
        Column(
            modifier = modifier
                .padding(it)
                .fillMaxSize()
                .background(Color.White),
        ) {
            IntentDataItem(
                modifier = Modifier.fillMaxWidth(),
                intentData = dataIntentRead
            )
            IntentDataItem(
                modifier = Modifier.fillMaxWidth(),
                isNewScan = isNewScan,
                isKeyboard = true
            )
        }
    }
}

@Composable
fun IntentDataItem(
    modifier: Modifier = Modifier,
    isNewScan: Boolean = false,
    isKeyboard: Boolean = false,
    intentData: String = "",
) {
    var textFieldValue by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Request focus on the text field for keyboard wedge
    LaunchedEffect(isKeyboard) {
        if (isKeyboard) {
            focusRequester.requestFocus()  // Set focus on the TextField
        }
    }

    // Clear keyboard for each new scan
    LaunchedEffect(isNewScan) {
        if (isNewScan) {
            textFieldValue = ""
        }
    }

    Card(
        modifier = modifier.height(120.dp),
        elevation = CardDefaults.cardElevation(14.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Data read from service",
                fontSize = 18.sp
            )
            Spacer(Modifier.height(10.dp))
            if (isKeyboard) {
                OutlinedTextField(
                    modifier = Modifier
                        .focusRequester(focusRequester),
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.None),
                )
            } else {
                Text(
                    text = intentData,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPrev() {
    MainScreen(Modifier,false, "", {}, {})
}