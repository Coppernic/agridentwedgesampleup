package fr.coppernic.agridentwedgesampleup.ui.screens.main

data class MainState(
    val intentData: String,
    val wedgeData: String,
    val isNewScan: Boolean,
) {
    companion object {
        fun noReadState() = MainState(intentData = "No data read yet", wedgeData = "", isNewScan = false)
    }
}
