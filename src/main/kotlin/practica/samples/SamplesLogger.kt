package samples

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("Samples")

fun log(msg: Any?) {
    log.info(msg.toString())
}

fun log(message: String) {
    println("(${Thread.currentThread().name}) : $message")
}

fun log(character: Char) {
    print("$character")
}