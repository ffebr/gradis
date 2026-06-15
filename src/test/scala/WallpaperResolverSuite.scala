import wallpaper.*

class WallpaperResolverSuite extends munit.FunSuite {

  // Build a synthetic probe. `env` defaults to empty; `commands` lists the
  // executables we pretend are installed.
  def probe(
      os: Os,
      env: Map[String, String] = Map.empty,
      commands: Set[String] = Set.empty
  ): EnvProbe =
    EnvProbe(os = os, env = env.get, hasCommand = commands.contains)

  def resolved(p: EnvProbe): Option[String] =
    WallpaperService.resolve(WallpaperService.registry, p).map(_.name)

  test("Windows always resolves to the Windows backend") {
    assertEquals(resolved(probe(Os.Windows)), Some("Windows"))
  }

  test("macOS resolves when osascript is present") {
    assertEquals(resolved(probe(Os.MacOS, commands = Set("osascript"))), Some("macOS"))
  }

  test("KDE session with plasma helper resolves to KDE") {
    val p = probe(
      Os.Linux,
      env = Map("XDG_CURRENT_DESKTOP" -> "KDE"),
      commands = Set("plasma-apply-wallpaperimage", "gsettings")
    )
    assertEquals(resolved(p), Some("KDE Plasma"))
  }

  test("GNOME session with gsettings resolves to GNOME") {
    val p = probe(
      Os.Linux,
      env = Map("XDG_CURRENT_DESKTOP" -> "ubuntu:GNOME"),
      commands = Set("gsettings")
    )
    assertEquals(resolved(p), Some("GNOME"))
  }

  test("GNOME session without gsettings has no backend") {
    val p = probe(Os.Linux, env = Map("XDG_CURRENT_DESKTOP" -> "GNOME"))
    assertEquals(resolved(p), None)
  }

  test("bare Linux with no known desktop has no backend") {
    assertEquals(resolved(probe(Os.Linux)), None)
  }

  test("detection is case-insensitive for desktop names") {
    val p = probe(Os.Linux, env = Map("XDG_CURRENT_DESKTOP" -> "kde"), commands = Set("plasma-apply-wallpaperimage"))
    assertEquals(resolved(p), Some("KDE Plasma"))
  }

}
