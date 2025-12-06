package app.revanced.extension.dudulauncher.info

import android.annotation.SuppressLint
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class CpuStat(
    val user: Long,
    val nice: Long,
    val system: Long,
    val idle: Long,
    val iowait: Long,
    val irq: Long,
    val softirq: Long
)

class CpuInfo {
    companion object {
        private var prevState: CpuStat? = null
        private val executor = Executors.newScheduledThreadPool(1)

        @SuppressLint("PrivateApi")
        @JvmStatic
        fun start() {

            executor.schedule({
                try {

                    val clazz = Class.forName("android.os.SystemProperties")
                    val setMethod = clazz.getDeclaredMethod("set", String::class.java, String::class.java)
                    val usage = "%.0f".format(getCpuUsages())
                    setMethod.invoke(null, "debug.info.cpu", usage)
                    println(usage)
                } catch (e: Exception) {
                }
            }, 1000, TimeUnit.MILLISECONDS)
        }

        @JvmStatic
        fun stop() {
            executor.shutdown()
        }

        fun getCpuUsages(): Float {
            val currState: CpuStat = readCpuStat()
            val usage = calculateCpuPercent(prevState, currState)
            prevState = currState
            return usage
        }

        fun readCpuStat(): CpuStat {
            val line = File("/proc/stat").readLines().first()  // "cpu  123 456 ..."
            val parts = line.split(Regex("\\s+")).drop(1)      // skip "cpu"

            return CpuStat(
                user = parts[0].toLong(),
                nice = parts[1].toLong(),
                system = parts[2].toLong(),
                idle = parts[3].toLong(),
                iowait = parts[4].toLong(),
                irq = parts[5].toLong(),
                softirq = parts[6].toLong()
            )
        }

        fun calculateCpuPercent(prev: CpuStat?, curr: CpuStat?): Float {
            if (prev == null || curr == null) return -1.0F
            val prevIdle = prev.idle + prev.iowait
            val idle = curr.idle + curr.iowait

            val prevNonIdle = prev.user + prev.nice + prev.system + prev.irq + prev.softirq
            val nonIdle = curr.user + curr.nice + curr.system + curr.irq + curr.softirq

            val prevTotal = prevIdle + prevNonIdle
            val total = idle + nonIdle

            val totalDiff = total - prevTotal
            val idleDiff = idle - prevIdle

            return ((totalDiff - idleDiff).toFloat() / totalDiff.toFloat()) * 100f
        }
    }
}
