import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun main() = runBlocking {
    val numbers = produceNumbers() //produce enteros en un stream
    val squares = square(numbers) // realiza el cuadrado de los elemtnos que hay en el stream

    // Solo cinco veces
    repeat(5) {
        println(squares.receive()) // imprimimos los 5 primeros
    }
    println("Done!") // we are done
    coroutineContext.cancelChildren() // cancelamos las corrutinas hijas
}

// produceNumbers produce enteros en un stream y devuelve un channel de recepcion
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) send(x++) // infinite stream of integers starting from 1
}

// Lee de un stream realiza el cuadrado y devuelve un channel de recepcion
fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
    // Mandamos los cuadrados de los enteros que hay en el stream
    for (x in numbers) send(x * x)
}