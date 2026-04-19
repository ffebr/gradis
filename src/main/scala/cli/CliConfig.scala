package cli

import engine.PaletteStyle

case class GenerationConfig(
    rawHex: String,
    steps: Int,
    isDarkTheme: Boolean,
    setAsWallpaper: Boolean,
    skipCleanUp: Boolean,
    style: PaletteStyle,
    customOutPath: Option[String]
)
