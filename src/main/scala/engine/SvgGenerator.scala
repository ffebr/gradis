package engine

object SvgGenerator:

  def generateSvgContent(colors: List[String], bgHex: String): String = {
    val width = 10000
    val height = 5000

    val pillWidth = 1000
    val pillHeight = 3000
    val rx = pillWidth / 2
    val overlap = 450

    val step = pillWidth - overlap

    val totalWidth = pillWidth + (colors.size - 1) * step

    val startX = (width - totalWidth) / 2
    val startY = (height - pillHeight) / 2

    val bg = s"""<rect width="$width" height="$height" fill="$bgHex" />"""

    val rects = colors.zipWithIndex.map { case (hex, i) =>
      val x = startX + i * step
      s"""<rect x="$x" y="$startY" width="$pillWidth" height="$pillHeight" rx="$rx" fill="$hex" />"""
    }

    val pills = rects.reverse.mkString("\n  ")

    s"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height">
       |  $bg
       |  $pills
       |</svg>""".stripMargin
  }
