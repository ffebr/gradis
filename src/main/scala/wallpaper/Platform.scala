package wallpaper

import zio.*
import SysIO.SysIO

// OS family the binary runs on.
enum Os:
  case Windows, MacOS, Linux, Unknown

object Os:

  // Current OS, from the `os.name` system property.
  def current: Os =
    val name = sys.props.getOrElse("os.name", "").toLowerCase
    if name.contains("win") then Windows
    else if name.contains("mac") || name.contains("darwin") then MacOS
    else if name.contains("nix") || name.contains("nux") || name.contains("aix") then Linux
    else Unknown

  def render(os: Os): String = os match
    case Windows => "Windows"
    case MacOS   => "macOS"
    case Linux   => "Linux"
    case Unknown => "unknown OS"

// What a WallpaperSetter may inspect to decide if it applies. Plain functions, so
// detection stays pure and unit-testable with synthetic probes.
final case class EnvProbe(
    os: Os,
    env: String => Option[String],
    hasCommand: String => Boolean
)

object EnvProbe:

  // The real environment: OS, env vars, and PATH lookup.
  val live: UIO[EnvProbe] =
    ZIO.succeed(
      EnvProbe(
        os = Os.current,
        env = key => sys.env.get(key),
        hasCommand = cmd => SysIO.commandExistsBlocking(cmd)
      )
    )
