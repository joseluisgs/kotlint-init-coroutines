import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

/*
El enfoque que debemos darle al uso de canales para aplicarlos correctamente bajo el concepto de confinamiento de hilo
sería implementando el patrón Fan-In con un solo hilo dedicado a consumir los datos del canal para posteriormente actualizar el estado.
La idea entonces será enviar dos tipos de mensajes diferentes a través del canal. El primer mensaje será el
 correspondiente a la solicitud de incremento en 1 unidad de la variable. El segundo mensajes será el correspondiente a
 la solicitud de la obtención del valor actual de la variable. Para que esto sea posible vamos a valernos de la potencia
  de las clases selladas para clasificar los mensajes por tipo y poder usarlos dentro de un bloque when. La clase sellada y
  sus subclases quedarían de la siguiente manera:
 */

// Clase mensaje sellada
sealed class Message

//  lo declaramos como un object para que solo exista una instancia que sea reutilizable (Singleton)
object IncCounterMessage : Message()

/*
El mensaje correspondiente a la solicitud del valor actual de la variable lo llamamos GetCounterMessage,
lo declaramos como una clase que deberá ser instanciada cada vez que se desee obtener el valor actual y contará con una
propiedad de tipo CompletableDeferred<Int> que usará el receptor del mensaje para almacenar el valor actual de la variable.
El emisor del mensaje podrá consultar el valor almacenado llamando a la función await del objeto CompletableDeferred
provocando que se suspenda, para luego reanudarse tan pronto el receptor responda el mensaje.
 */
class GetCounterMessage(val counterValue: CompletableDeferred<Int>) : Message()

fun CoroutineScope.getSendChannel(): SendChannel<Message> {
    val channel = Channel<Message>()

    launch(newSingleThreadContext("My Thread")) {
        var counter = 0
        for (message in channel) {
            when (message) {
                is IncCounterMessage -> counter++
                is GetCounterMessage -> message.counterValue.complete(counter)
            }
        }
    }

    return channel
}

fun main() {
    log("Start Channel")

    val coroutinesAmount = 100_000
    var counter = 0

    log("Initial Value: $counter")

    runBlocking {

        // Obtenemos el canal
        val channel = getSendChannel()

        val coroutines = List(coroutinesAmount) {
            launch(Dispatchers.Default) {
                // Mandamos el mensaje de incrementar la variable
                channel.send(IncCounterMessage)
            }
        }

        coroutines.forEach {
            it.join()
        }

        // Esperamos el resultado
        val result = CompletableDeferred<Int>()
        // Mandamos el mensaje de obtener el valor actual de la variable
        channel.send(GetCounterMessage(result))
        // Obtenemos el valor actual esperamos, porque es asíncrono
        counter = result.await()
        channel.close()
    }

    log("Final Value: $counter")
    log("---------------")
    if (counter == coroutinesAmount) {
        log("Result: SUCCESS")
    } else {
        log("Result: FAIL")
    }
    log("---------------")

    log("End")
}