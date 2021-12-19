package samples

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.*

// Ejemplo de uso de canales
fun main() = runBlocking<Unit> {
    println("Ejemplo de uso de canales")
    // Crear un canal de datos String pra enviar y recibir de tanmaño 1
    val channel = Channel<String>()
    // Lanzar una corutina que envia datos al canal
    launch {
        // Enviar datos al canal
        channel.send("A1")
        channel.send("A2")
        log("A done")

    }
    // Lanzar una corutina que envía datos del canal
    launch {
        channel.send("B1")
        log("B done")
    }
    // Lanzar una corutina que recibe datos del canal
    launch {
        repeat(3) {
            val x = channel.receive()
            log(x)
        }
    }
}
