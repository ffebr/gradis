package wallpaper

import zio.*
import SysIO.{ IOError, SysIO }

// macOS — set every desktop's picture via osascript.
object MacOsSetter extends WallpaperSetter:
  val name = "macOS"
  val detection = Detection(os = Set(Os.MacOS), requiredCommands = List("osascript"))

  def setWallpaper(absolutePath: String): IO[IOError, Unit] =
    val script =
      s"""tell application "System Events" to set picture of every desktop to "$absolutePath""""
    SysIO.runProcess(Seq("osascript", "-e", script))

// Windows — call SystemParametersInfo via a PowerShell P/Invoke shim. PowerShell
// always ships with Windows, so no extra command is needed for detection. The script
// goes through a temp .ps1 + -File because its embedded double quotes would be
// corrupted by Java's command-line quoting on Windows.
object WindowsSetter extends WallpaperSetter:
  val name = "Windows"
  val detection = Detection(os = Set(Os.Windows))

  // SPI_SETDESKWALLPAPER = 20; SPIF_UPDATEINIFILE(1) | SPIF_SENDWININICHANGE(2) = 3
  def setWallpaper(absolutePath: String): IO[IOError, Unit] =
    val safePath = absolutePath.replace("'", "''")
    val script =
      s"""Add-Type @"
         |using System.Runtime.InteropServices;
         |public class GradisWp {
         |  [DllImport("user32.dll", CharSet = CharSet.Auto)]
         |  public static extern int SystemParametersInfo(int a, int b, string c, int d);
         |}
         |"@
         |[GradisWp]::SystemParametersInfo(20, 0, '$safePath', 3) | Out-Null
         |""".stripMargin

    for
      tmp <- SysIO.writeTempScript("gradis-wallpaper", ".ps1", script)
      _ <- SysIO
        .runProcess(
          Seq("powershell", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", tmp)
        )
        .ensuring(SysIO.deleteFile(tmp))
    yield ()

// GNOME and GNOME-based shells — set both light and dark background keys via gsettings.
object GnomeSetter extends WallpaperSetter:
  val name = "GNOME"

  private val gnomeShells = Set("gnome", "unity", "cinnamon", "ubuntu", "pop")

  val detection = Detection(
    os = Set(Os.Linux),
    envMatches = List(
      EnvMatch("XDG_CURRENT_DESKTOP", v => gnomeShells.exists(v.toLowerCase.contains))
    ),
    requiredCommands = List("gsettings")
  )

  def setWallpaper(absolutePath: String): IO[IOError, Unit] =
    val uri = s"file://$absolutePath"
    val schema = "org.gnome.desktop.background"
    SysIO.runProcess(Seq("gsettings", "set", schema, "picture-uri", uri)) *>
      SysIO.runProcess(Seq("gsettings", "set", schema, "picture-uri-dark", uri))

// KDE Plasma — use plasma-apply-wallpaperimage (Plasma 5.18+). An older-Plasma
// qdbus fallback could be added later as a second setter, no framework changes.
object KdeSetter extends WallpaperSetter:
  val name = "KDE Plasma"

  val detection = Detection(
    os = Set(Os.Linux),
    envMatches = List(EnvMatch("XDG_CURRENT_DESKTOP", _.toLowerCase.contains("kde"))),
    requiredCommands = List("plasma-apply-wallpaperimage")
  )

  def setWallpaper(absolutePath: String): IO[IOError, Unit] =
    SysIO.runProcess(Seq("plasma-apply-wallpaperimage", absolutePath))
