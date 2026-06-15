package SysIO

trait IOError:
  val msg: String

case class WriteError(e: Throwable) extends IOError:
  val msg = s"Failed to write file: ${e.getMessage}"

case class ConvertSvgToPngError(e: Throwable) extends IOError:
  val msg = "Error converting SVG to PNG. Please ensure rsvg-convert is installed."

case class SetWallpaperError(detail: String) extends IOError:
  val msg = s"Failed to set desktop wallpaper: $detail"

case class NoWallpaperBackendError(os: String, desktop: Option[String]) extends IOError:

  val msg =
    val where = desktop.fold(os)(d => s"$os / $d")
    s"No supported wallpaper backend detected for $where. " +
      "Use --skip-bg to only generate the file, or add a WallpaperSetter for your environment."

case class InvalidHexFormatError(hex: String) extends IOError:
  val msg = s"Invalid color format: '$hex'. The HEX code must be exactly 6 characters (e.g., #FF0000)."
