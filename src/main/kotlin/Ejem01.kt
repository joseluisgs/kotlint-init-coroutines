import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

// Hilos vs Coroutines
fun main() {
    println("¡Bienvenido a \"Coroutines con Kotlin\"!")
    hilos();
    coroutines();
}

fun hilos() {
    println("¡Hilos!")
    val time = measureTimeMillis {
        // Una lista de 10_000 hilos
        val threads = List(10_000) {
            thread {
                // Los suspendemos 1 segundo
                Thread.sleep(1000)
                print('.')
            }
        }
        // Esperamos a que terminen
        threads.forEach {
            it.join()
        }
        print('\n')
    }
    // Medimos el tiempo
    println("Time: $time")
}

// El nombre de runBlocking significa que el hilo que lo ejecuta (en este caso, el hilo principal) se bloquea durante la duración de la llamada,
// hasta que todas las corrutinas dentro de runBlocking {...} completen su ejecución.
fun coroutines() = runBlocking {
    println("¡Corrutinas!")
    val time = measureTimeMillis {
        // Una lista de 10_000 coroutines
        val coroutines = List(10_000) {
            // un constructor de corrutinas. Lanza una nueva corrutina al mismo tiempo que el resto del código,
            // que continúa funcionando de forma independiente.
            launch {
                // es una función de suspensión especial. Suspende la corrutina durante un tiempo específico. La suspensión de una corrutina no bloquea el subproceso subyacente,
                // pero permite que otras corrutinas se ejecuten y utilicen el subproceso subyacente para su código.
                delay(1000)
                print('.')
            }
        }
        // Esperamos que terminen
        coroutines.forEach {
            it.join()
        }
        print('\n')
    }

    println("Time: $time")
}
