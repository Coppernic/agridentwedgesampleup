package fr.coppernic.agridentwedgesampleup.ui.screens.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.coppernic.agridentwedgesampleup.R
import fr.coppernic.agridentwedgesampleup.ui.components.BaudRateMenu
import fr.coppernic.agridentwedgesampleup.ui.components.TopAppBar
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.model.EditType
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.model.EditableItems
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.model.GetDataItemClick
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.model.Items
import fr.coppernic.agridentwedgesampleup.ui.theme.Purple80
import fr.coppernic.agridentwedgesampleup.ui.theme.PurpleGrey80
import fr.coppernic.agridentwedgesampleup.ui.utils.BaudRate
import fr.coppernic.agridentwedgesampleup.ui.utils.OutputFormat
import fr.coppernic.agridentwedgesampleup.ui.utils.TagTypes
import fr.coppernic.agridentwedgesampleup.ui.utils.Timing

@Composable
fun Setting(
    modifier: Modifier = Modifier,
    firmware: String,
    serialNumber: String,
    amplitude: String,
    rssi: String,
    fdxRssi: String,
    hdxRssi: String,
    hdxFreq: String,
    tagType: String,
    timeout: String,
    baudRate: String,
    // tagReadCount: Int,
    output: String,
    delayTime: String,
    isRFFieldOn: Boolean,
    isReaderOpen: Boolean,
    isReaderBusy: Boolean,
    selectedBaudRate: String,
    baudRates: List<String>,
    logMessages: List<String>,
    onNavigateBack: () -> Unit,
    onCloseOpenReader: () -> Unit,
    onBaudRateSelected: (String) -> Unit,
    onRFFieldSwitchChange: (Boolean) -> Unit,
    onGetDataItemClick: (GetDataItemClick) -> Unit,
    onEditDataItemClick: (EditType) -> Unit,
    onDeleteLogs: () -> Unit,
) {
    val items =
        listOf(
            Items(R.string.firmware, firmware, GetDataItemClick.Firmware),
            Items(R.string.serial_number, serialNumber, GetDataItemClick.SerialNumber),
            Items(R.string.amplitude, amplitude, GetDataItemClick.Amplitude),
            Items(R.string.rssi, rssi, GetDataItemClick.Rssi),
            Items(R.string.fdx_rssi, fdxRssi, GetDataItemClick.FdxRssi),
            Items(R.string.hdx_rssi, hdxRssi, GetDataItemClick.HdxRssi),
            Items(R.string.hdx_freq, hdxFreq, GetDataItemClick.HdxFreq),
            Items(R.string.delay_time, delayTime, GetDataItemClick.DelayTime),
        )

    val editableItems =
        listOf(
            EditableItems(R.string.tag_type, tagType, GetDataItemClick.TagType) {
                onEditDataItemClick(EditType.TagType(TagTypes.entries.first().value))
            },
            EditableItems(R.string.timeout, timeout, GetDataItemClick.Timeout) {
                onEditDataItemClick(EditType.Timeout(Timing.entries.first().value))
            },
            EditableItems(R.string.baudrate, baudRate, GetDataItemClick.BaudRate) {
                onEditDataItemClick(EditType.BaudRate(BaudRate.entries.first().byteValue))
            },
            EditableItems(R.string.output, output, GetDataItemClick.Output) {
                onEditDataItemClick(EditType.OutputFormat(OutputFormat.entries.first().value))
            },
        )

    Scaffold(
        topBar = {
            TopAppBar(
                navigateBack = onNavigateBack,
                isSettingScreen = true,
            )
        },
    ) {
        Column(
            modifier =
                modifier
                    .padding(it)
                    .fillMaxSize(),
        ) {
            Row(
                modifier =
                    Modifier
                        .weight(1F)
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    enabled = !isReaderBusy,
                    onClick = onCloseOpenReader,
                ) {
                    Text(text = if (isReaderOpen) "Close" else "Open")
                }
                BaudRateMenu(
                    list = baudRates,
                    selectedItem = selectedBaudRate,
                    onItemSelected = onBaudRateSelected,
                )
                RfidSwitch(
                    enabled = !isReaderBusy && isReaderOpen,
                    isChecked = isRFFieldOn,
                    onCheckedChange = onRFFieldSwitchChange,
                )
            }
            Card(
                modifier =
                    Modifier
                        .weight(4F)
                        .padding(2.dp)
                        .fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = Purple80,
                    ),
            ) {
                LazyColumn(
                    modifier =
                        modifier
                            .padding(10.dp)
                            .fillMaxSize(),
                ) {
                    items(items) { item ->
                        DataItem(
                            enabled = !isReaderBusy,
                            title = stringResource(item.titleResId),
                            result = item.result,
                            onFetchData = { onGetDataItemClick(item.getDataClick) },
                        )
                    }
                    items(editableItems) { editableItems ->
                        EditableDataItem(
                            enabled = !isReaderBusy,
                            title = stringResource(editableItems.titleResId),
                            result = editableItems.result,
                            onFetchData = { onGetDataItemClick(editableItems.getDataClick) },
                            onEditData = editableItems.onEditClick,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier =
                    Modifier
                        .weight(4F)
                        .padding(2.dp)
                        .fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = PurpleGrey80,
                    ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onDeleteLogs) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete logs",
                                tint = colorResource(R.color.top_app_bar_grey),
                            )
                        }
                    }
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                    ) {
                        items(logMessages) { log ->
                            Text(log, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditDataDialog(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    type: EditType,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val baudRates = BaudRate.entries.toList().map { it.name }
    var selectedBaudRate by remember { mutableStateOf(baudRates[0]) }

    val outputFormats = OutputFormat.entries.toList().map { it.name }
    var selectedOutputFormat by remember { mutableStateOf(outputFormats[0]) }

    val tagTypes = TagTypes.entries.toList().map { it.name }
    var selectedTagType by remember { mutableStateOf(tagTypes[0]) }

    val timings = Timing.entries.toList().map { it.name }
    var selectedTiming by remember { mutableStateOf(timings[0]) }

    if (isVisible) {
        AlertDialog(
            modifier = modifier,
            icon = {},
            title = {
                Column {
                    Text(
                        text =
                            when (type) {
                                is EditType.BaudRate -> "BaudRate"
                                is EditType.TagType -> "Tag Type"
                                is EditType.Timeout -> "Timing"
                                is EditType.OutputFormat -> "Output Format"
                                is EditType.NotVisible -> ""
                            },
                    )
                    Text(text = "Select value")
                }
            },
            text = {
                // Determine the list and the currently selected item based on the EditType
                val list =
                    when (type) {
                        is EditType.BaudRate -> baudRates
                        is EditType.TagType -> tagTypes
                        is EditType.Timeout -> timings
                        is EditType.OutputFormat -> outputFormats
                        is EditType.NotVisible -> listOf()
                    }

                var selectedItem by remember {
                    mutableStateOf(
                        when (type) {
                            is EditType.BaudRate -> selectedBaudRate
                            is EditType.TagType -> selectedTagType
                            is EditType.Timeout -> selectedTiming
                            is EditType.OutputFormat -> selectedOutputFormat
                            is EditType.NotVisible -> ""
                        },
                    )
                }

                // Update the correct state variable based on the selected EditType
                val onItemSelected: (String) -> Unit = { newItem ->
                    selectedItem = newItem
                    when (type) {
                        is EditType.BaudRate -> selectedBaudRate = newItem
                        is EditType.TagType -> selectedTagType = newItem
                        is EditType.Timeout -> selectedTiming = newItem
                        is EditType.OutputFormat -> selectedOutputFormat = newItem
                        is EditType.NotVisible -> {}
                    }
                }

                BaudRateMenu(
                    modifier = modifier,
                    list = list,
                    selectedItem = selectedItem,
                    onItemSelected = onItemSelected,
                )
            },
            onDismissRequest = {
                onDismiss()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedItem =
                            when (type) {
                                is EditType.BaudRate -> selectedBaudRate
                                is EditType.OutputFormat -> selectedOutputFormat
                                is EditType.TagType -> selectedTagType
                                is EditType.Timeout -> selectedTiming
                                else -> ""
                            }
                        onConfirm(selectedItem)
                    },
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    },
                ) {
                    Text("Dismiss")
                }
            },
        )
    }
}

@Composable
fun EditableDataItem(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    title: String,
    result: String,
    onFetchData: () -> Unit,
    onEditData: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$title $result")
        IconButton(enabled = enabled, onClick = onFetchData) {
            Icon(imageVector = Icons.Filled.Refresh, null)
        }
        IconButton(enabled = enabled, onClick = onEditData) {
            Icon(imageVector = Icons.Filled.Edit, null)
        }
    }
}

@Composable
fun DataItem(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    title: String,
    result: String,
    onFetchData: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$title $result")
        IconButton(enabled = enabled, onClick = onFetchData) {
            Icon(imageVector = Icons.Filled.Refresh, null)
        }
    }
}

@Composable
fun RfidSwitch(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Rfid")
        Spacer(modifier = Modifier.width(4.dp))
        Switch(
            modifier = modifier,
            enabled = enabled,
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            thumbContent =
                if (isChecked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
        )
    }
}

@Preview
@Composable
private fun SettingPrev() {
    Setting(
        onNavigateBack = {},
        onGetDataItemClick = {},
        firmware = "Firmware v1.0",
        serialNumber = "SN123456",
        amplitude = "100 mV",
        rssi = "-70 dBm",
        fdxRssi = "-80 dBm",
        hdxRssi = "-75 dBm",
        hdxFreq = "123 Hz",
        tagType = "Type A",
        timeout = "30s",
        baudRate = "9600",
        selectedBaudRate = "",
        output = "",
        // tagReadCount = -1,
        baudRates = listOf(),
        delayTime = "10ms",
        isReaderBusy = false,
        isReaderOpen = true,
        logMessages =
            listOf(
                "test00",
                "test01",
                "test02",
                "test03",
                "test04",
            ),
        isRFFieldOn = true,
        onRFFieldSwitchChange = {},
        onDeleteLogs = {},
        onEditDataItemClick = {},
        onCloseOpenReader = {},
        onBaudRateSelected = {},
    )
}
