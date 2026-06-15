package wallpaper

import zio.*
import SysIO.IOError

// One env-var condition, e.g. XDG_CURRENT_DESKTOP contains "KDE".
final case class EnvMatch(key: String, pred: String => Boolean)

// Declarative criteria for when a setter applies; every field must hold.
// os: allowed families (empty = any); envMatches: env vars + predicate;
// requiredCommands: executables on PATH.
final case class Detection(
    os: Set[Os] = Set.empty,
    envMatches: List[EnvMatch] = Nil,
    requiredCommands: List[String] = Nil
):

  // Whether this setter applies to the given environment.
  def matches(probe: EnvProbe): Boolean =
    val osOk = os.isEmpty || os.contains(probe.os)
    val envOk = envMatches.forall(m => probe.env(m.key).exists(m.pred))
    val cmdOk = requiredCommands.forall(probe.hasCommand)
    osOk && envOk && cmdOk

  // Higher = more specific; the resolver prefers the highest match.
  def specificity: Int = os.size + envMatches.size + requiredCommands.size

// The framework's only extension point: implement it and add the object to
// WallpaperService.registry — no other wiring needed.
trait WallpaperSetter:
  def name: String // shown in logs
  def detection: Detection // when this backend is selected
  def setWallpaper(absolutePath: String): IO[IOError, Unit]
