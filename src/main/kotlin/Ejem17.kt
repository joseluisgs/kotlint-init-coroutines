import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Constructor producer

fun main() {
    withOutProducer()
    withProducer()
}

fun withOutProducer() {
    log("Start withou Producer")

    runBlocking {

        val channel = Channel<Int>()

        launch {
            (1..10).forEach {
                delay(500)
                log("Sending $it ")
                channel.send(it)
            }
            channel.close()
        }

        launch {
            for(i in channel) {
                log("Received: $i")
            }
        }

    }

    log("End")
}

fun withProducer() {
    log("Start with Producer")

    runBlocking {

        val channel = produce {
            (1..10).forEach {
                delay(500)
                log("Sending $it ")
                send(it)
            }
        }

        launch {
            for(i in channel) {
                log("Received: $i")
            }
        }

    }

    log("End")
}