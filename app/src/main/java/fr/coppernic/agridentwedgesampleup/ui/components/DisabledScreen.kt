package fr.coppernic.agridentwedgesampleup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.coppernic.agridentwedgesampleup.ui.theme.Pink80

@Composable
fun DisabledScreen(
    modifier: Modifier = Modifier,
    isReaderBusy: Boolean
) {
    if (isReaderBusy) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(color = Pink80.copy(alpha = 0.8F)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Module busy ...",
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 27.sp
            )
            Spacer(modifier = Modifier.height(30.dp))
            CircularProgressIndicator(
                modifier = Modifier.width(80.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun DisabledScreenPrev() {
    DisabledScreen(isReaderBusy = true)
}