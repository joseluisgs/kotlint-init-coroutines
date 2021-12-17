import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor

sealed class Message2
object IncCounterMessage2: Message2()
class GetCounterMessage2(val counterValue: CompletableDeferred<Int>): Message2()

fun CoroutineScope.getSendChannel2(): SendChannel<Message2> = actor(newSingleThreadContext("My Thread")) {
    var counter = 0
    // Nos ahorramos crear lounch y el canal
    for (message in channel) {
        when (message) {
            is IncCounterMessage2 -> counter++
            is GetCounterMessage2 -> message.counterValue.complete(counter)
        }
    }
}

fun main() {
    log("Start")

    val coroutinesAmount = 100_000
    var counter = 0

    log("Initial Value: $counter")

    runBlocking {

        val channel = getSendChannel2()

        val coroutines = List(coroutinesAmount) {
            launch(Dispatchers.Default) {
                channel.send(IncCounterMessage2)
            }
        }

        coroutines.forEach {
            it.join()
        }

        val result = CompletableDeferred<Int>()
        channel.send(GetCounterMessage2(result))
        counter = result.await()
        channel.close()
    }

    log("Final Value: $counter")
    log("---------------")
    if(counter == coroutinesAmount) {
        log("Result: SUCCESS")
    } else {
        log("Result: FAIL")
    }
    log("---------------")

    log("End")
}