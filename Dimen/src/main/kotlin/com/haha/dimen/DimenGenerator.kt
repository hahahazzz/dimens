package com.haha.dimen

import android.annotation.SuppressLint
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// 305是小米MixFold折叠屏未展开
val dimenArr = arrayOf(
  305,
  320,
  360,
  363,
  384,
  392,
  393,
  400,
  410,
  411,
  432,
  480,
  533,
  592,
  600,
  610,
  640,
  662,
  674,
  720,
  768,
  838,
  1024,
  1280,
  1707
)
val decimalFormat = DecimalFormat("#.###")
const val dimenScale = 720.0 / 750 / 2 // dp值,这里的750和720都是px，所以要除以2
val dimenRange = -10..1280
fun main() {
  val saveDir = File("./Dimen/src/main/res/")
  if (saveDir.exists()) {
    saveDir.listFiles().filter { it.nameWithoutExtension.startsWith("values-sw") }.forEach { it.deleteRecursively() }
  } else {
    saveDir.mkdirs()
  }
  dimenArr.forEach {
    val dimenDir = File(saveDir, "values-sw${it}dp")
    if (dimenDir.exists()) {
      dimenDir.delete()
    }
    dimenDir.mkdirs()
    val dimenFile = File(dimenDir, "dimens.xml")
    if (dimenFile.exists()) {
      dimenFile.delete()
    }
    val res = genDimenFile(it)
    println("generate dimen --> w${it}dp")
    dimenFile.writeText(res)
  }
  val dp360 = File(saveDir, "values-sw360dp/dimens.xml")
  if (dp360.exists()) {
    val defaultDimen = File(saveDir, "values/dimens.xml")
    if (defaultDimen.exists()) {
      defaultDimen.delete()
    }
    dp360.copyTo(defaultDimen)
  }
  runCommand("./gradlew.cmd")
  /**
   * gradlew.cmd内容
   * ./gradlew dimen_lib:bundleReleaseAar
   */
}

@SuppressLint("NewApi")
fun runCommand(
  cmd: List<String>,
  workingDir: File = File("./"),
  timeoutAmount: Long = 60L,
  timeUnit: TimeUnit = TimeUnit.SECONDS
): String? = runCatching {
  ProcessBuilder(cmd)
    .directory(workingDir.also {
      println(it.absolutePath)
    })
    .redirectErrorStream(true)
    .start().also { it.waitFor(timeoutAmount, timeUnit) }
    .inputStream.bufferedReader().readText()
}.onFailure { it.printStackTrace() }.getOrNull()


/**
 * 调用系统命令行中的命令.以List<String>的方式输入命令的各个参数.
 * 命令执行完毕后会以String?传回结果,不会在终端显示
 * 在当前目录中执行,超时时间为60秒.
 * 若要更改目录和时间,请使用List<String>的方式传入
 */
fun runCommand(vararg cmd: String): String? = runCommand(listOf(*cmd))

fun genDimenFile(currentSize: Int): String {
  val dimenBuilder = StringBuilder()
  dimenBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
  dimenBuilder.append(System.lineSeparator())
  dimenBuilder.append("<resources>")

  dimenRange.forEach {
    if (it != 0) {
      dimenBuilder.append(System.lineSeparator())
      dimenBuilder.append("\t")
      val pxUnit = if (it < 0) {
        "px_${abs(it)}"
      } else {
        "px${it}"
      }
      val pxValue = "${
        decimalFormat.format(
          it * dimenScale * (currentSize.toFloat() / 360F)
        )
      }dp"
      dimenBuilder.append("<dimen name=\"${pxUnit}\">${pxValue}</dimen>")
      if (pxUnit == "px750") {
        println(pxValue)
      }
    }
  }
  dimenBuilder.append(System.lineSeparator())
  dimenBuilder.append("</resources>")
  return dimenBuilder.toString()
}