package SysIO

trait IOError:
  val msg: String

case class WriteError(e: Throwable) extends IOError:
  val msg = s"Не удалось записать файл ${e.getMessage}"

case class ConvertSvgToPngError(e: Throwable) extends IOError:
  val msg = s"Ошибка конвертации SVG -> PNG. Убедись, что rsvg-convert установлена."

case class SetMacWallpaperError(e: Throwable) extends IOError:
  val msg = s"Не удалось поменять обои на Mac: ${e.getMessage}"
