package engine

enum PaletteStyle:
  case Analogous, Monochromatic, Complementary

case class GeneratedPalette(
    colors: List[Hex],
    background: Hex
)

object PaletteGenerator:

  def generate(baseHex: String, steps: Int, style: PaletteStyle, isDarkTheme: Boolean = true): GeneratedPalette =
    val startOklch = Hex(baseHex).toSRGB.toLinear.toOklab.toOklch

    val endOklch = style match
      case PaletteStyle.Analogous =>
        startOklch.copy(H = (startOklch.H + 30.0) % 360.0, L = if startOklch.L > 0.5 then 0.3 else 0.8)
      case PaletteStyle.Monochromatic => startOklch.copy(L = if startOklch.L > 0.5 then 0.2 else 0.9)
      case PaletteStyle.Complementary =>
        startOklch.copy(H = (startOklch.H + 180.0) % 360.0, L = if startOklch.L > 0.5 then 0.3 else 0.8)

    val midH = Lerp.lerpHue(startOklch.H, endOklch.H, 0.5)
    val avgC = (startOklch.C + endOklch.C) / 2.0
    val bgC = Math.min(0.03, avgC * 0.15)

    val bgOklch = if isDarkTheme then
      Oklch(L = 0.16, C = bgC, H = midH)
    else
      Oklch(L = 0.97, C = bgC * 0.6, H = midH)

    val interpolatedOklch = startOklch.interpolateTo(endOklch, steps)

    val hexColors = interpolatedOklch.map(_.toOklab.toLinear.toSRGB.toHex)
    val hexBg = bgOklch.toOklab.toLinear.toSRGB.toHex

    GeneratedPalette(hexColors, hexBg)
