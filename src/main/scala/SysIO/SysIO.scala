package SysIO

import java.nio.file.{ Files, Paths }
import scala.sys.process.*
import scala.util.Try

import zio.*

object SysIO {

  def writeString(content: String, path: String): IO[IOError, Unit] =
    ZIO.attemptBlocking {
      Files.writeString(Paths.get(path), content)
    }.catchAll(err => ZIO.fail(WriteError(err))).unit

  def convertSvgToPng(svgPath: String, pngPath: String, width: Int = 10000): IO[IOError, Unit] =
    ZIO.attemptBlocking {
      val cmd = Seq("rsvg-convert", "-w", width.toString, "-o", pngPath, svgPath)
      cmd.!
    }.catchAll(err => ZIO.fail(ConvertSvgToPngError(err))).unit

  // Runs a command, failing with SetWallpaperError on a process error or non-zero exit.
  def runProcess(cmd: Seq[String]): IO[IOError, Unit] =
    for
      result <- ZIO.attemptBlocking {
        val err = new StringBuilder
        val code = cmd.!(ProcessLogger(_ => (), err append _ append '\n'))
        (code, err.toString.trim)
      }.mapError(e => SetWallpaperError(e.getMessage))
      (code, stderr) = result
      _ <- ZIO
        .fail(SetWallpaperError(s"${cmd.headOption.getOrElse("command")} exited with $code: $stderr"))
        .when(code != 0)
    yield ()

  // True if cmd is on PATH; blocking, false on any failure.
  def commandExistsBlocking(cmd: String): Boolean =
    val isWindows = sys.props.getOrElse("os.name", "").toLowerCase.contains("win")
    val probe = if isWindows then Seq("where", cmd) else Seq("sh", "-c", s"command -v $cmd")
    Try(probe.!(ProcessLogger(_ => (), _ => ())) == 0).getOrElse(false)

  // Writes content to a temp file and returns its path, so multi-line scripts can be
  // passed to interpreters via -File instead of fragile command-line quoting.
  def writeTempScript(prefix: String, suffix: String, content: String): IO[IOError, String] =
    ZIO.attemptBlocking {
      val p = Files.createTempFile(prefix, suffix)
      Files.writeString(p, content)
      p.toAbsolutePath.toString
    }.catchAll(err => ZIO.fail(WriteError(err)))

  def deleteFile(path: String): UIO[Unit] =
    ZIO.attemptBlocking {
      Files.deleteIfExists(Paths.get(path))
    }.ignore

}
