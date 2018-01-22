package printah.osu.printah

enum class PrintSidesOptions(val displayText: String, val lprParameter: String){
    OneSided("One sided", "one-sided"), TwoSided("Two sided", "one-sided");
    override fun toString(): String {
        return displayText
    }
}

enum class PrintOrientationOptions(val displayText: String, val lprParameter: String){
    Landscape("Portrait", "portrait"), Portrait("Landscape", "landscape");
    override fun toString(): String {
        return displayText
    }
}