package samples

import kotlinx.coroutines.*

// Ejemplo asincronía
fun main() = runBlocking {
    // LA definimos como Deferred, es Diferida, asincronica
    val deferred: Deferred<Int> = async(Dispatchers.Default) {
        loadData()
    }
    log("waiting...")
    // Lo paso a String por la función que tengo hecha
    log(deferred.await().toString())
}

// Función asíncrona
suspend fun loadData(): Int {
    log("loading...")
    delay(1000L)
    log("loaded!")
    return 42
}