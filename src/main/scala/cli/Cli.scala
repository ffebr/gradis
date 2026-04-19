package cli

import java.util.UUID

import zio.*
import engine.PaletteGenerator
import engine.PaletteStyle
import engine.SvgGenerator.generateSvgContent
import SysIO.*
import Messages.Logs

object Cli:

  def generate(config: GenerationConfig): IO[IOError, Unit] =
    val uniqueTag = UUID.randomUUID().toString.take(5)

    val baseHex = config.rawHex.replaceAll("['\"\\s]", "")
    val hexName = baseHex.replace("#", "")
    val pngPath = config.customOutPath.getOrElse(s"wallpaper_${hexName}_${config.steps}st_$uniqueTag.png")
    val svgPath = s"temp_${hexName}_$uniqueTag.svg"

    val generationEffect = for {
      _ <- ZIO.fail(InvalidHexFormatError(baseHex)).when(hexName.length != 6)
      _ <- Console.printLine(Logs.start(baseHex, config.steps, config.isDarkTheme)).orDie

      palette = PaletteGenerator.generate(baseHex, config.steps, config.style, config.isDarkTheme)
      _ <- Console.printLine(Logs.bgCalculated(palette.background.value)).orDie

      svgContent = generateSvgContent(palette.colors.map(_.value), palette.background.value)

      _ <- Console.printLine(Logs.writeSvg).orDie
      _ <- SysIO.writeString(svgContent, svgPath)

      _ <- Console.printLine(Logs.renderPng).orDie
      _ <- SysIO.convertSvgToPng(svgPath, pngPath)

      _ <- ZIO.when(config.setAsWallpaper) {
        Console.printLine(Logs.setWallpaper).orDie *>
          SysIO.setMacWallpaper(pngPath)
      }

      _ <- ZIO.when(config.setAsWallpaper) {
        Console.printLine(Logs.cleanup).orDie *>
          SysIO.deleteFile(svgPath)
      }

      _ <- Console.printLine(Logs.success).orDie
    } yield ()

    generationEffect.ensuring(
      ZIO.when(config.setAsWallpaper) {
        Console.printLine(Logs.cleanup).orDie *>
          SysIO.deleteFile(svgPath)
      }
    )
