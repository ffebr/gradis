package engine

object Lerp:

  def lerp(a: Double, b: Double, t: Double) = a + (b - a) * t

  def lerpHue(h1: Double, h2: Double, t: Double): Double =
    val delta = (h2 - h1) match
      case d if d > 180.0  => d - 360.0
      case d if d < -180.0 => d + 360.0
      case d               => d
    val res = (h1 + delta * t) match
      case r if r < 0      => r + 360.0
      case r if r >= 360.0 => r - 360.0
      case r               => r
    res
