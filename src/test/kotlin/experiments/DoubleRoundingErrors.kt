import java.text.*
import kotlin.math.pow

private val plainStringFormat = DecimalFormat(
    "0.0",
    DecimalFormatSymbols.getInstance()) // Locale.ENGLISH
    .also {
        it.maximumFractionDigits = 18
    }

private fun Double.toPlainString(): String = plainStringFormat.format(this)

private fun Int.toGroupedString(): String =
    DecimalFormat(
        "0.0",
        DecimalFormatSymbols.getInstance()) // Locale.ENGLISH
        .also {
            it.maximumFractionDigits = 0
            it.groupingSize = 3
            it.isGroupingUsed = true

        }.format(this)


private fun compute(n: Int) {
    val summands = 10.0.pow(n).toInt()
    val ideal = summands * 0.1
    val error =  (1..summands).sumOf { 0.1 } - ideal
    println("${summands.toGroupedString()} | ${error.toPlainString()} | $error")
}


fun main() {
    (1..9).forEach { compute(it) }
}