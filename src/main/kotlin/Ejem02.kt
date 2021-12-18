import kotlinx.coroutines.*
import kotlin.concurrent.thread

fun main() {
    //secuencialHilo();
    //concurrenteHilo()
    //concurrentCorutines()
    //concurrentCorutines2()
    //asyncAwait()
    //cancelation()
    //cancelJoin()
    cancelFinally()
}

fun secuencialHilo() {
    ("Inicio Secuencial Hilo")
    Thread.sleep(1000)
    ("Despues de Thread.sleep.")
    ("Fin")
}

fun concurrenteHilo() {
    ("Inicio Concurrente Hilo")

    // Lanzo un hilo y me paro... como vemos en la salida ya cada cosa va por su lado, es concurrente
    // No le da tiempo a terminar, para eso lee más abajo
    val hilo = thread {
        Thread.sleep(1000)
        ("Despues de Thread.sleep.")
    }
    // Deberíamos hacerle el join para que termine de ejecutarse antes que el programa y bloquear el hilo prinicipal hasta que
    // termine el que hemos abierto.
    // hilo.join()
    ("Fin")
}

fun concurrentCorutines() {
    ("Inicio Concurrente Hilo")
    // No deberíamos poner Global Scope, cuidado !!!
    //  se crea una coroutine que se ejecutará por defecto en un hilo trabajador o de bajo perfil,
    //  en Inglés se llama worker thread o background thread (hilo de fondo). Al igual que en el ejemplo anterior
    //  “Concurrente con hilos”, el hilo principal (main) continúa la ejecución pero no esperará a que la coroutine termine su ejecución,
    //  sino que al haber usado GlobalScope para su creación cualquier ejecución adentro de ésta finalizará tan pronto como el programa termine,
    //  es decir, tan pronto como el hilo principal acabe su ejecución.
    // // No le da tiempo a terminar, lee más abajo
    val coroutine = GlobalScope.launch {
        // Dado que Thread.sleep es una llamada a una función que bloquea el hilo, adentro de una coroutine
        // podemos hacer lo mismo con una llamada a una función que suspende el hilo en lugar de bloquearlo.
        // La función delay es la que nos sirve para rar nuestro objetivo. Con la modificación quedaría así:
        Thread.sleep(1000)
        ("Despues de Thread.sleep.")
    }
    // Deberíamos hacerle el join para que termine de ejecutarse antes que el programa y bloquear el hilo prinicipal hasta que
    // termine el que hemos abierto.
    Thread.sleep(2000) // Esto es muy cutre :)
    ("Fin")
}

fun concurrentCorutines2() = runBlocking {
    ("Inicio Concurrente Hilo")
    val coroutine = GlobalScope.launch {
        // Dado que Thread.sleep es una llamada a una función que bloquea el hilo, adentro de una coroutine
        // podemos hacer lo mismo con una llamada a una función que suspende el hilo en lugar de bloquearlo.
        // La función delay es la que nos sirve para rar nuestro objetivo. Con la modificación quedaría así:
        delay(1000)
        ("Despues de Thread.sleep.")
    }
    // Deberíamos hacerle el join para que termine de ejecutarse antes que el programa y bloquear el hilo prinicipal hasta que
    // termine el que hemos abierto.
    coroutine.join()
    // Thread.sleep(2000) // Esto es muy cutre :)
    ("Fin")
}

fun asyncAwait() = runBlocking {
    ("Inicio")
    val number = async {
        ("Antes del delay")
        delay(1000)
        ("Despues de delay.")
        (1..100).random()
    }
    ("Esto continua porque estamos en un contexto de RunBlocking...")
    // Se espera hasta tener el valor con el await
    ("Terminamos con el valor: ${number.await()}")
    ("Fin")
}

/*
En el contexto de concurrencia, bloquear un hilo significa que el hilo se mantendrá fuera de uso mientras éste encuentre algo que lo bloquee.
Ésto podría ser la espera de la liberación de un recurso, una llamada a la función Thread.sleep, una llamada a un servicio externo, etc.
Mientras se encuentre en ese estado, el hilo no podrá ser usado para realizar otras tareas.
Por el contrario, suspender un hilo significa que el hilo estará libre y listo para ser usado en la ejecución de otras tareas
mientras se encuentra a la espera de la liberación de un recurso, una llamada a la función delay, una llamada a un servicio externo, etc.
La naturaleza de las coroutines es suspender la ejecución evitando a toda costa llamadas a funciones que bloquean.
Ésta es la magia que le permite a las coroutines ser tan eficientes y de bajo consumo de recursos en comparación con los hilos regulares
de siempre.
Veamos la definición de la función Thread.sleep y hagamos una comparación con la definición de delay.

Para indicar que una función se supende debe tener el modificador suspend
Lo que sí hace el modificador suspend es restringir que esa función solo pueda ser llamada desde adentro del bloque de
una coroutine o dentro de otra suspend function.

 */

fun cancelation() = runBlocking {
    val job = launch {
        repeat(1000) { i ->
            log("job: I'm sleeping $i ...")
            delay(500L)
        }
    }
    // Espero y si no me cancelo
    delay(1300L) // delay a bit
    log("main: I'm tired of waiting!")
    job.cancel() // cancels the job
    job.join() // waits for job's completion
    log("main: Now I can quit.")
}

// Una vez cancelado puedo esperar a que termine
fun cancelJoin() = runBlocking {
    val startTime = System.currentTimeMillis()
    // Pôdemos indicarle el dispacher a la coroutine
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { // computation loop, just wastes CPU
            // print a message twice a second
            if (System.currentTimeMillis() >= nextPrintTime) {
                log("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // delay a bit
    log("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    log("main: Now I can quit.")
}

// Podemos hacer que si se cancela haga un finally para evitar dejar recursos abiertos
fun cancelFinally() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { i ->
                log("job: I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            log("job: I'm running finally")
        }
    }
    delay(1300L) // delay a bit
    log("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    log("main: Now I can quit.")
}

