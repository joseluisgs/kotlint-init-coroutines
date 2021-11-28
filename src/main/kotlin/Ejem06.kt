import kotlinx.coroutines.*
import java.nio.charset.Charset
import kotlin.streams.toList

// Creamos un coroutina en el contexto Dfeault para no usar GlobalScope
suspend fun main() = coroutineScope {
    log("Ejemplo 6")
    val ping = async(Dispatchers.IO) { pingWithProccess("github.com") }
    log("Ping: ${ping.await()}") // Bloquea hasta que llegamos aquí, lo espera
    log("Fin ejemplo 6")
}

fun pingWithProccess(domain: String): String {
    val processBuilder = ProcessBuilder()
    // Debemos hacer ping 5 veces, por eso -c 5
    processBuilder.command("bash", "-c", "ping -c 5 $domain")
    val ping = processBuilder.start()
    return ping.inputStream
        .bufferedReader(Charset.defaultCharset())
        .lines().toList().last()
}

