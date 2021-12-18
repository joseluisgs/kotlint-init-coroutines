import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

// Secuencialidad vs Asincronía
fun main() {
    secuencialidad()
    paralelismo()
    composicionFuncionesAsincronia()

}

suspend fun doSomethingUsefulOne(): Int {
    // Imaginemos un login o acceso a base de datos
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    // Imaginemos una consulta a api rest
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}

fun secuencialidad() = runBlocking {
    val time = measureTimeMillis {
        val one = doSomethingUsefulOne()
        val two = doSomethingUsefulTwo()
        log("The answer is ${one + two}")
    }
    log("Completed in $time ms")
}

fun paralelismo() = runBlocking {
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        log("The answer is ${one.await() + two.await()}")
    }
    log("Completed in $time ms")
}

fun composicionFuncionesAsincronia() = runBlocking {
    val time = measureTimeMillis {
        log("The answer is ${concurrentSum()}")
    }
    log("Completed in $time ms")
}

// Composicion de asincronia
suspend fun concurrentSum(): Int = coroutineScope {
    // Vamos a jugar con distintos Dispacher
    val one = async(Dispatchers.IO) { doSomethingUsefulOne() }
    val two = async(Dispatchers.Default) { doSomethingUsefulTwo() }
    one.await() + two.await()
}