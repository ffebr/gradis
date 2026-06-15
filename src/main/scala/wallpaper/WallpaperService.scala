package wallpaper

import java.nio.file.Paths

import zio.*
import SysIO.{ IOError, NoWallpaperBackendError }

// Picks the right WallpaperSetter for the current environment and applies it.
// Add a setter to registry to support a new environment — selection is automatic.
object WallpaperService:

  // Built-in backends; order is irrelevant, resolve picks by specificity.
  val registry: List[WallpaperSetter] =
    List(MacOsSetter, WindowsSetter, KdeSetter, GnomeSetter)

  // Most specific setter matching the probe; pure, so it's unit-testable.
  def resolve(setters: List[WallpaperSetter], probe: EnvProbe): Option[WallpaperSetter] =
    setters
      .filter(_.detection.matches(probe))
      .sortBy(-_.detection.specificity)
      .headOption

  // Resolve a backend for the live environment and set path; fails if none match.
  def set(path: String): IO[IOError, Unit] =
    for
      absolute <- ZIO.succeed(Paths.get(path).toAbsolutePath.toString)
      probe <- EnvProbe.live
      setter <- ZIO
        .fromOption(resolve(registry, probe))
        .orElseFail(
          NoWallpaperBackendError(Os.render(probe.os), probe.env("XDG_CURRENT_DESKTOP"))
        )
      _ <- Console.printLine(s"Using wallpaper backend: ${setter.name}").orDie
      _ <- setter.setWallpaper(absolute)
    yield ()
