package cli

object Messages:

  object CLI:
    val summary = "Generates 10K macOS wallpapers based on OKLCH palettes"
    val colorArgHelp = "Base hex color code (e.g., '#9B5DE5' or '#50C878')"
    val stepsOptHelp = "Number of colors in the gradient"
    val darkOptHelp = "Use dark theme for the background"
    val skipBgOptHelp = "Do NOT set the generated image as desktop wallpaper"
    val skipCleanUpOptHelp = "Do NOT remove temp svg"
    val outOptHelp = "Custom output path for the PNG file"
    val styleOptHelp = "Palette generation strategy"

  object Logs:

    def start(hex: String, steps: Int, isDark: Boolean): String =
      s"Starting generation. Color: $hex, Steps: $steps, Dark theme: $isDark"

    def bgCalculated(bgHex: String): String =
      s"Background calculated: $bgHex"

    val writeSvg = "Writing SVG to disk..."
    val renderPng = "Rendering 10K PNG (this may take a few seconds)..."
    val setWallpaper = "Setting as desktop wallpaper..."
    val cleanup = "Cleaning up temporary files..."
    val success = "Done! Check your desktop."
