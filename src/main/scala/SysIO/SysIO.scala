package SysIO

import java.nio.file.{ Files, Paths }
import scala.sys.process.*

import zio.*

object SysIO {

  def writeString(content: String, path: String): IO[IOError, Unit] =
    ZIO.attemptBlocking {
      Files.writeString(Paths.get(path), content)
    }.catchAll(err => ZIO.fail(WriteError(err))).unit

  def convertSvgToPng(svgPath: String, pngPath: String, width: Int = 10000): IO[IOError, Unit] =
    ZIO.attemptBlocking {
      val cmd = Seq("rsvg-convert", "-w", width.toString, "-o", pngPath, svgPath)
      val exitCode = cmd.!
    }.catchAll(err => ZIO.fail(ConvertSvgToPngError(err))).unit

  def setMacWallpaper(pngPath: String): IO[IOError, Unit] =
    ZIO.attemptBlocking {
      val absolutePath = Paths.get(pngPath).toAbsolutePath.toString
      val script = s"""tell application "System Events" to set picture of every desktop to "$absolutePath""""

      val cmd = Seq("osascript", "-e", script)
      val exitCode = cmd.!
    }.catchAll(err => ZIO.fail(SetWallpaperError(err))).unit

  def deleteFile(path: String): UIO[Unit] =
    ZIO.attemptBlocking {
      Files.deleteIfExists(Paths.get(path))
    }.ignore

}
