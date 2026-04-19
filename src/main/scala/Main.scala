import engine.PaletteGenerator
import engine.PaletteStyle
import engine.SvgGenerator.generateSvgContent
import zio.Console.*
import zio.ZIO
import zio.cli.*
import zio.cli.HelpDoc.Span.text
import SysIO.*
import cli.BuildInfo
import cli.Cli
import cli.GenerationConfig
import cli.Messages.CLI

object Gradis extends ZIOCliDefault:
  val colorArg: Args[String] = Args.text("hex-color") ?? CLI.colorArgHelp
  val stepsOpt: Options[BigInt] = Options.integer("steps").alias("s").withDefault(BigInt(7)) ?? CLI.stepsOptHelp
  val darkThemeOpt: Options[Boolean] = Options.boolean("dark").alias("d") ?? CLI.darkOptHelp
  val skipBgOpt = Options.boolean("skip-bg") ?? CLI.skipBgOptHelp
  val skipCleanUpOpt = Options.boolean("skip-cl") ?? CLI.skipCleanUpOptHelp
  val outOpt: Options[Option[String]] = Options.text("out").alias("o").optional ?? CLI.outOptHelp

  val styleOpt = Options.enumeration("style")(
    "analogous" -> PaletteStyle.Analogous,
    "mono" -> PaletteStyle.Monochromatic,
    "comp" -> PaletteStyle.Complementary
  ).withDefault(PaletteStyle.Analogous) ?? CLI.styleOptHelp

  val cliOptions = stepsOpt ++ darkThemeOpt ++ skipBgOpt ++ outOpt ++ styleOpt ++ skipCleanUpOpt
  val generateCmd = Command("generate", cliOptions, colorArg)

  val rootCmd = Command("gradis").subcommands(generateCmd)

  val cliApp = CliApp.make(
    name = BuildInfo.name,
    version = BuildInfo.version,
    summary = text(CLI.summary),
    command = rootCmd
  ) { case ((stepsBigInt, isDark, skipBg, userOutPath, style, clean), rawHex) =>
    val config = GenerationConfig(
      rawHex = rawHex,
      steps = stepsBigInt.toInt,
      isDarkTheme = isDark,
      setAsWallpaper = !skipBg,
      skipCleanUp = !clean,
      style = style,
      customOutPath = userOutPath
    )

    Cli.generate(config).catchAll(err => zio.Console.printLineError(err.msg).orDie)
  }
