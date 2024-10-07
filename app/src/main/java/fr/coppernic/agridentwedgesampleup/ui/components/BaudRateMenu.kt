package fr.coppernic.agridentwedgesampleup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import fr.coppernic.agridentwedgesampleup.R

@Composable
fun BaudRateMenu(
    modifier: Modifier = Modifier,
    list: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
) {
    val isDropDownExpanded = remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier.clickable {
                        isDropDownExpanded.value = true
                    },
            ) {
                Text(text = selectedItem) // Use the selectedItem instead of itemPosition
                Image(
                    painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24),
                    contentDescription = "DropDown Icon",
                )
            }
            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {
                    isDropDownExpanded.value = false
                },
            ) {
                list.forEach { username ->
                    DropdownMenuItem(
                        text = {
                            Text(text = username)
                        },
                        onClick = {
                            isDropDownExpanded.value = false
                            onItemSelected(username) // Call the lambda function to update the selected item
                        },
                    )
                }
            }
        }
    }
}
