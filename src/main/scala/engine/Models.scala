package engine

case class Hex(value: String)
case class SRGB(r: Double, g: Double, b: Double)
case class LinearSRGB(r: Double, g: Double, b: Double)
case class Oklab(L: Double, a: Double, b: Double)
case class Oklch(L: Double, C: Double, H: Double)

extension (hex: Hex)

  def toSRGB: SRGB =
    val clean = hex.value.stripPrefix("#")
    val r = Integer.parseInt(clean.substring(0, 2), 16) / 255.0
    val g = Integer.parseInt(clean.substring(2, 4), 16) / 255.0
    val b = Integer.parseInt(clean.substring(4, 6), 16) / 255.0
    SRGB(r, g, b)

extension (srgb: SRGB)

  def toHex: Hex =
    def format(c: Double): String =
      val clamped = Math.max(0.0, Math.min(1.0, c))
      f"${(clamped * 255).round.toInt}%02X"
    Hex(s"#${format(srgb.r)}${format(srgb.g)}${format(srgb.b)}")

extension (srgb: SRGB)

  def toLinear: LinearSRGB =
    def ungamma(c: Double): Double =
      if c <= 0.04045 then c / 12.92
      else Math.pow((c + 0.055) / 1.055, 2.4)
    LinearSRGB(ungamma(srgb.r), ungamma(srgb.g), ungamma(srgb.b))

extension (linear: LinearSRGB)

  def toSRGB: SRGB =
    def gamma(c: Double): Double =
      if c <= 0.0031308 then 12.92 * c
      else 1.055 * Math.pow(c, 1.0 / 2.4) - 0.055
    SRGB(gamma(linear.r), gamma(linear.g), gamma(linear.b))

extension (linear: LinearSRGB)

  def toOklab: Oklab =
    val l = 0.4122214708 * linear.r + 0.5363325363 * linear.g + 0.0514459929 * linear.b
    val m = 0.2119034982 * linear.r + 0.6806995451 * linear.g + 0.1073969566 * linear.b
    val s = 0.0883024619 * linear.r + 0.2817188376 * linear.g + 0.6299787005 * linear.b

    val l_ = Math.cbrt(l)
    val m_ = Math.cbrt(m)
    val s_ = Math.cbrt(s)

    val L = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_
    val a = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_
    val b = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_
    Oklab(L, a, b)

extension (oklab: Oklab)

  def toLinear: LinearSRGB =
    val l_ = oklab.L + 0.3963377774 * oklab.a + 0.2158037573 * oklab.b
    val m_ = oklab.L - 0.1055613458 * oklab.a - 0.0638541728 * oklab.b
    val s_ = oklab.L - 0.0894841775 * oklab.a - 1.2914855480 * oklab.b

    val l = l_ * l_ * l_
    val m = m_ * m_ * m_
    val s = s_ * s_ * s_

    val r = 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s
    val g = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s
    val b = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
    LinearSRGB(r, g, b)

extension (oklab: Oklab)

  def toOklch: Oklch =
    val C = Math.sqrt(oklab.a * oklab.a + oklab.b * oklab.b)
    val H_ = Math.toDegrees(Math.atan2(oklab.b, oklab.a))
    val H = if H_ < 0 then H_ + 360.0 else H_
    Oklch(oklab.L, C, H)

extension (oklch: Oklch)

  def toOklab: Oklab =
    val hRad = Math.toRadians(oklch.H)
    val a = oklch.C * Math.cos(hRad)
    val b = oklch.C * Math.sin(hRad)
    Oklab(oklch.L, a, b)

extension (start: Oklch)

  def interpolateTo(end: Oklch, steps: Int): List[Oklch] =
    if steps <= 1 then List(start)

    (0 until steps).map { i =>
      val t = i.toDouble / (steps - 1)

      Oklch(
        L = Lerp.lerp(start.L, end.L, t),
        C = Lerp.lerp(start.C, end.C, t),
        H = Lerp.lerpHue(start.H, end.H, t)
      )
    }.toList
