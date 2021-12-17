import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/*
Estados compartidos, secciones críticas y sección de bloqueo
Empecemos estableciendo un problema sencillo. Vamos a crear un ejemplo en el que se incrementa una variable en 1 desde
cada coroutine creada. Para que se pueda apreciar una inconsistencia vamos a crear 100 mil coroutines, el valor final
 de la variable inicializada en 0 debería ser 100 mil. Veamos:
 */

fun main() {
    nada()
    semaforo()
    monitor()
    atomicidad()
    mutex()
}

/*
Si ejecutas el ejemplo anterior te darás cuenta que la variable finaliza con una cantidad menor a la cantidad de coroutines
creadas. La razón por la que se dan inconsistencias se debe a un problema conocido como “condición de carrera” — ‘race condition’
en Inglés — y es un problema a resolver que se da ineludiblemente cuando se trabaja de manera concurrente sobre una variable
u objeto mutable compartido. Básicamente muchos subprocesos acceden a la misma variable en un momento determinado,
 consultan su valor y lo modifican basado en esta consulta. Debido a que hay varios subprocesos leyendo y escribiendo
 sobre una misma variable a la vez, entre la acción de consulta y la acción de modificación de cada subproceso, pueden
 haber otras modificaciones intermedias hechas por los otros subprocesos.
 */

fun nada() {
    log("Start Ejemplo 1")

    val coroutinesAmount = 100_000
    var counter = 0

    log("Initial Value: $counter")

    runBlocking {
        val coroutines = List(coroutinesAmount) {
            launch (Dispatchers.Default) {
                counter++
            }
        }

        coroutines.forEach {
            it.join()
        }
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

/*
Para resolver este problema, vamos a utilizar una sección crítica. Protegida por un semáforo binario
Podemos usar acquire() para solicitar un permiso de acceso y release() para liberarlo.
O usar withPermit() para solicitar un permiso de acceso y liberarlo automáticamente al finalizar la función.
 */
fun semaforo() {
    log("Start Semaforos")

    val coroutinesAmount = 100_000
    var counter = 0

    log("Initial Value: $counter")

    runBlocking {
        val semaphore = Semaphore(1)

        val coroutines = List(coroutinesAmount) {
            launch (Dispatchers.Default) {
                semaphore.withPermit {
                    counter++
                }
            }
        }

        coroutines.forEach {
            it.join()
        }
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

/**
 * Monitor es una clase que implementa una sección crítica. Accedemos y la modificamos a través de los métodos de la clase.
 * En este ejemplo: Si el id y el valor del contador no coinciden, el subproceso deberá ser interrumpido temporalmente hasta que sea notificado
 * y así reanudar su ejecución para volverlo a intentar.
 * ReentrantLock que resulta mejor que un objeto de tipo Object dado que está especialmente adaptado para lidiar con
 * concurrencia y sincronización
 * Condition. Al usar un ReentrantLock puedes crear tantas Conditions como desees ganando así la posibilidad de
 * interrumpir subprocesos separadamente a partir de un mismo cerrojo. Ésto te permite clasificar mejor los subprocesos
 * pudiendo generar subgrupos sin necesidad de crear varios cerrojos para ello.
 */
class Monitor {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    var counter = 0
        private set

    fun performAction(id: Int) {
        lock.withLock {
            while (id != counter) {
                condition.await()
            }

            this.counter++
            condition.signalAll()
        }
    }
}

fun monitor() {
    log("Start Monitor")

    val coroutinesAmount = 100_000

    val monitor = Monitor()

    log("Initial Value: ${monitor.counter}")

    runBlocking {

        val coroutines = List(coroutinesAmount) {
            launch (Dispatchers.Default) {
                monitor.performAction(it)
            }
        }

        coroutines.forEach {
            it.join()
        }
    }

    log("Final Value: ${monitor.counter}")
    log("---------------")
    if(monitor.counter == coroutinesAmount) {
        log("Result: SUCCESS")
    } else {
        log("Result: FAIL")
    }
    log("---------------")

    log("End")
}

/**
 * Usamos variables atómicas para asegurarnos que que podemos leer y escribir concurrentemente
 * Tenemos AtomicInteger, Long, Reference
 *  BlockingQueue y ConcurrentHashMap
 *  un Collection común en un Collection sincronizado por medio de un “envoltorio” llamando a la función
 *  Collections.synchronizedCollection o alguna de sus funciones más específicas como Collections.synchronizedSet,
 *  Collections.synchronizedList, Collections.synchronizedMap, etc. La diferencia entre los Collections del paquete
 *  java.util.concurrent y los Collections sincronizados radica en el desempeño. Los Collections del paquete java.util.concurrent
 *  siempre serán más eficientes o más rápidos debido a su naturaleza de soportar el acceso desde varios subprocesos dividiendo
 *  sus datos en segmentos, mientras que los Collections sincronizados se valen del uso de cerrojos limitando el acceso
 *  a solamente un subproceso a la vez.
 */
fun atomicidad() {
    log("Start Atomicidad")

    val coroutinesAmount = 100_000
    val counter = AtomicInteger(0)

    log("Initial Value: $counter")

    runBlocking {
        val coroutines = List(coroutinesAmount) {
            launch (Dispatchers.Default) {
                counter.incrementAndGet()
            }
        }

        coroutines.forEach {
            it.join()
        }
    }

    log("Final Value: ${counter.get()}")
    log("---------------")
    if(counter.get() == coroutinesAmount) {
        log("Result: SUCCESS")
    } else {
        log("Result: FAIL")
    }
    log("---------------")

    log("End")
}

fun mutex() {
    log("Start Mutex")

    val coroutinesAmount = 100_000
    var counter = 0

    log("Initial Value: $counter")

    runBlocking {
        val mutex = Mutex()

        val coroutines = List(coroutinesAmount) {
            launch (Dispatchers.Default) {
                mutex.withLock {
                    counter++
                }
            }
        }

        coroutines.forEach {
            it.join()
        }
    }

    log("Final Value: ${counter}")
    log("---------------")
    if(counter == coroutinesAmount) {
        log("Result: SUCCESS")
    } else {
        log("Result: FAIL")
    }
    log("---------------")

    log("End")
}