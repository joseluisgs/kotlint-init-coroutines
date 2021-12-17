import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

/*
Foreach vs ConsumeEach
Según la documentación oficial, los datos transmitidos a través del canal pueden ser consumidos de manera continua de
dos maneras: con la función consumeEach y con un ciclo for valiéndose del hecho de que un canal implementa iteradores.
Cuando se consumen los datos aplicando el patrón Fan-Out, si uno de los consumidores falla, podría repercutir en todos los
involucrados, es decir, tanto en los emisores como en los demás receptores. A continuación vamos a desarrollar un ejemplo
comparativo simple.
El siguiente ejemplo constará de dos clases. La clase Sender estará encargada de enviar los datos y la clase Receiver
se encargará de recibir los datos. Crearemos una instancia de la clase Sender que enviará un dato cada 500 millisegundos.
Crearemos dos instancias de la clase Receiver y una de ellas fallará cuando reciba un dato por tercera vez. Los datos serán
recibidos de dos maneras diferentes, primero con la función consumeEach y luego con un ciclo for.

Si observas ambos códigos sin ejecutarlos podrías llegar a la conclusión de que ambos hacen lo mismo y que usar uno o
el otro es indiferente, pero si los ejecutas te darías cuenta de la falencia que tiene la función consumeEach. Si alguno
 de los receptores falla mientras está obteniendo los datos con la función consumeEach, provocará que el canal se cierre
 y que tanto el emisor como los demás receptores se detengan. Contrariamente cuando recibes los datos usando un ciclo for,
  como en el segundo caso, el fallo de un receptor no tiene repercusión sobre el canal, por lo tanto los datos podrán seguir
  siendo recibidos por lo demás receptores que se encuentran en ejecución. Está en tus manos seleccionar uno u otro según
  el caso de uso que estés resolviendo, a fin de cuentas, la función consumeEach podría ser exactamente lo que necesitas.

 */

fun main() {
    consumeEach()
    consumeFor()
}

class Sender2: CoroutineScope {

    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    fun processData(): ReceiveChannel<Int> = produce {
        (1..10).forEach {
            delay(500)
            log("Sender: Sending $it...")
            send(it)
        }
    }

}

class Receiver2(private val id: Int): CoroutineScope {

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        log("Receiver #$id Exception Caught: [ $exception ]")
    }
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default + exceptionHandler

    fun processData(channel: ReceiveChannel<Int>, fail: Boolean) = launch {
        var i = 0
        // Usamos el consumeEach
        channel.consumeEach {
            i++
            if(fail && i == 3)
                throw ArithmeticException("Receiver #$id: Playing with exceptions.")

            log("Receiver #$id: $it received!")
        }
    }

}

class Receiver3(private val id: Int): CoroutineScope {

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        log("Receiver #$id Exception Caught: [ $exception ]")
    }
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default + exceptionHandler

    fun processData(channel: ReceiveChannel<Int>, fail: Boolean) = launch {
        var i = 0
        // Usamos el ciclo for
        for(number in channel) {
            i++
            if(fail && i == 3)
                throw ArithmeticException("Receiver #$id: Playing with exceptions.")

            log("Receiver #$id: $number received!")
        }
    }

}

fun consumeEach() {
    log("Start ConsumeEach")

    val sender = Sender2()
    val receiver1 = Receiver2(1)
    val receiver2 = Receiver2(2)

    runBlocking {
        val channel = sender.processData()
        val jobReceiver1 = receiver1.processData(channel, false)
        val jobReceiver2 = receiver2.processData(channel, true)

        joinAll(jobReceiver1, jobReceiver2)
    }

    log("End")
}

fun consumeFor() {
    log("Start Consume con For")

    val sender = Sender2()
    val receiver1 = Receiver3(1)
    val receiver2 = Receiver3(2)

    runBlocking {
        val channel = sender.processData()
        val jobReceiver1 = receiver1.processData(channel, false)
        val jobReceiver2 = receiver2.processData(channel, true)

        joinAll(jobReceiver1, jobReceiver2)
    }

    log("End")
}