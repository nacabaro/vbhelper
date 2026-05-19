import android.util.Log

object AppLogger {
    private val logs = StringBuilder()

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        append("DEBUG", tag, message)
    }

    fun e(tag: String, message: String, tr: Throwable? = null) {
        Log.e(tag, message, tr)
        append("ERROR", tag, message + (tr?.stackTraceToString() ?: ""))
    }

    private fun append(level: String, tag: String, message: String) {
        logs.append("${System.currentTimeMillis()} [$level][$tag] $message\n")
    }

    fun getLogs(): String = logs.toString()

    fun clear() {
        logs.clear()
    }
}
