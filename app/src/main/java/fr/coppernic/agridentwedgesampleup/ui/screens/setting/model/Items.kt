package fr.coppernic.agridentwedgesampleup.ui.screens.setting.model

data class Items(
    val titleResId: Int,
    val result: String,
    val getDataClick: GetDataItemClick,
)

data class EditableItems(
    val titleResId: Int,
    val result: String,
    val getDataClick: GetDataItemClick,
    val onEditClick: () -> Unit,
)
