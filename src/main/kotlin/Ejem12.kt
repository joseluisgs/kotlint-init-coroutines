import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Productor Consuimidor usando Canales
/*
Un Channel es una estructura de datos que permite la comunicación entre coroutines. Si hacemos una comparación con el
ejemplo 11, un Channel sería análogo al Queue que usamos para almacenar los datos producidos por una coroutine
para ser posteriormente consumidos por otra coroutine.
La ventaja de usar un Channel es que el control de la lectura y escritura ya está controlado y soportado dentro de
su estructura, es decir, un Channel es thread-safe por lo tanto la implementación de nuestro programa será más simple.
Debes saber que la función send es una suspend function que opera en conjunto con la función receive que también es
una suspend function. Cuando no se le establece un tamaño al canal, la transmisión se da solamente hasta que se han
invocado ambas funciones. Esto quiere decir que si la función send es invocada, el hilo se suspenderá hasta que la función
receive sea invocada y viceversa. A esta dinámica se le conoce como rendezvous.
 */
fun main() {
    log("Start")

    val mutex = Mutex()

    val channel = Channel<Int>() // Canal de enteros de un solo elemento de tamaño
    // si queremos más tamaño debemos val channel = Channel<Int>(5) // 5

    var produced = 0
    var consumed = 0

    val amounts = IntArray(5)

    runBlocking {
        launch(Dispatchers.Default) {
            val producers = List(100_000) {
                launch {
                    val number = (1..100).random()
                    // Usamos un paso de mensajes. Es decir, se suspende hasta que se ejecuta recieve y viceversa.
                    channel.send(number)
                    mutex.withLock {
                        produced++
                    }
                }
            }

            producers.forEach { it.join() }
            channel.close()
            log("Producers finished!")
        }

        launch(Dispatchers.Default) {
            val consumers = List(amounts.size) {
                launch {
                    // No hace falta la espera activa, porque hasta que no haya un dato no podemos "sacarlo"
                    // dentro del for estamos haciendo el recieve implicito
                    // internamente tambien tenemos ya corrutinas con la parte d eproductor y consumidor hechos
                    // https://kotlinlang.org/docs/channels.html#pipelines
                    for(i in channel) {
                        mutex.withLock {
                            consumed++
                            amounts[it]++
                        }
                    }
                }
            }

            consumers.forEach { it.join() }
            log("Consumers finished!")
        }
    }

    log("Produced: $produced")
    log("Consumed: $consumed")

    var total = 0
    log("----- AMOUNTS ------")
    amounts.forEachIndexed { index, amount ->
        total += amount
        log("Index #$index: $amount")
    }
    log("--------------------")
    log("TOTAL = $total")
    log("--------------------")

    log("End")
}