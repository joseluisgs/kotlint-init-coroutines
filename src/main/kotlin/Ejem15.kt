import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking

// Obtenemos los 10 primeros primos
fun main() = runBlocking {
    // Creamos un canal de recepcion de numeros desde el 2
    var cur = numbersFrom(2)
    repeat(10) {
        // Obtenemos el primer numero primo
        val prime = cur.receive()
        println(prime)
        // Creamos un nuevo canal con los números que no son divisibles por el candidato a primo
        cur = filter(cur, prime)
    }
    coroutineContext.cancelChildren() // cancel all children to let main finish
}

// Coroutine que devuelve un canal de numeros para recibir a partir del 2
fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
    var x = start
    while (true) send(x++) // infinite stream of integers from start
}

fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
    // Devolvemos si no es divisible por el candidato a primo
    // De esta manera quitamos los elementos
    for (x in numbers) if (x % prime != 0) send(x)
}