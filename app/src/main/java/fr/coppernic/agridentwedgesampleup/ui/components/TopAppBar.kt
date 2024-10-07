package fr.coppernic.agridentwedgesampleup.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import fr.coppernic.agridentwedgesampleup.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    isSettingScreen: Boolean = false,
    navigateUp: () -> Unit = {},
    navigateBack: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = colorResource(id = R.color.top_app_bar_grey),
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        navigationIcon = {
            if (isSettingScreen) {
                IconButton(onClick = navigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
            }
        },
        title = {
            Text(
                style = TextStyle(fontSize = 20.sp),
                text = stringResource(id = R.string.app_name),
            )
        },
        actions = {
            if (!isSettingScreen) {
                IconButton(onClick = navigateUp) {
                    Icon(imageVector = Icons.Filled.Settings, null, tint = Color.White)
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview
@Composable
fun TopAppBarPreview() {
    TopAppBar()
}
