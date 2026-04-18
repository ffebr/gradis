import engine.PaletteGenerator
import engine.PaletteStyle
import engine.SvgGenerator.generateSvgContent
import zio.ZIO
import zio.cli.*
import zio.cli.HelpDoc.Span.text
import SysIO.*

/*@main def hello(): Unit =
  val inputHex = "#9B5DE5"
  val result = PaletteGenerator.generate(inputHex, 8, PaletteStyle.Analogous, isDarkTheme = true)
  val svgString = generateSvgContent(
    colors = result.colors.map(_.value),
    bgHex = result.background.value
  )
  println(svgString)*/

object Gradis extends ZIOCliDefault:
  val colorArg: Args[String] = Args.text("hex-color")
  val stepsOpt: Options[BigInt] = Options.integer("steps").alias("s").withDefault(BigInt(7))
  val darkThemeOpt: Options[Boolean] = Options.boolean("dark").alias("d")
  val cliOptions = stepsOpt ++ darkThemeOpt
  val generateCmd = Command("generate", cliOptions, colorArg)

  val rootCmd = Command("gradis").subcommands(generateCmd)

  val cliApp = CliApp.make(
    name = "Mac Wallpaper Generator",
    version = "1.0.0",
    summary = text("Генерирует 10K обои на основе OKLCH палитры и ставит их на рабочий стол"),
    command = rootCmd
  ) { case ((stepsBigInt, isDark), rawHex) =>
    val steps = stepsBigInt.toInt
    val svgPath = "temp_palette.svg"
    val pngPath = "wallpaper_10k.png"

    val baseHex = rawHex.replaceAll("['\"\\s]", "")

    val generationEffect = for {
      _ <- ZIO.logInfo(s"Начинаем генерацию. Базовый цвет: $baseHex, Шагов: $steps, Темная тема: $isDark")

      palette = PaletteGenerator.generate(baseHex, steps, PaletteStyle.Analogous, isDark)
      _ <- ZIO.logInfo(s"Вычислен фон: ${palette.background.value}")

      svgContent = generateSvgContent(palette.colors.map(_.value), palette.background.value)

      _ <- ZIO.logInfo("Записываем SVG...")
      _ <- SysIO.writeString(svgContent, svgPath)

      _ <- ZIO.logInfo("Рендерим 10K PNG (это может занять секунду)...")
      _ <- SysIO.convertSvgToPng(svgPath, pngPath)

      _ <- ZIO.logInfo("Устанавливаем на рабочий стол...")
      _ <- SysIO.setMacWallpaper(pngPath)

      _ <- ZIO.logInfo("Убираем за собой...")
      _ <- SysIO.deleteFile(svgPath)

      _ <- ZIO.logInfo("Готово! Посмотри на свой рабочий стол.")
    } yield ()

    val withCleanup = generationEffect.ensuring(
      ZIO.logInfo("Убираем за собой...") *> SysIO.deleteFile(svgPath)
    )

    withCleanup.catchAll { (domainError: IOError) =>
      ZIO.logError(s"Ошибка: ${domainError.msg}")
    }
  }
