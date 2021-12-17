import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

// Definimos la interfaz con las acciones
interface BankAccount {
    val id: String
    val balance: Int

    fun transfer(amount: Int, to: BankAccount)
    fun deposit(amount: Int)
    fun retire(amount: Int)
}

// Clase monitor que implementa la interfaz
/*
Nótese la petición del cerrojo lock.withLock {...} en las 3 funciones.
 Ésto es necesario para que no se efectúe un depósito o un retiro mientras se realiza una transferencia,
 de lo contrario se podrían generar inconsistencias en el balance. Para realizar una transferencia es necesario que la cantidad
 a transferir no supere el balance de la cuenta. Si la condición se cumple entonces se procede a realizar la
transferencia que consta de las siguientes 3 acciones:
Se deposita la cantidad en la otra cuenta.
Se deduce del balance el monto que acaba de ser transferido.
Muestra en pantalla el mensaje “Transfer completed!”.
 */
class SavingsAccount(private val idField: String, initialBalance: Int): BankAccount {

    // Datos protegidos
    override val id: String
        get() = idField // cuando nos pregunten por id devuelve el idField

    override var balance: Int = initialBalance
        private set // Privado para que no se pueda modificar desde fuera

    // Cerrojo del monitor
    private val lock = ReentrantLock()

    // Funciones de la interfaz. accedemos con WithLock
    override fun transfer(amount: Int, to: BankAccount) = lock.withLock {
        log("[$id] Starting transfer to ${to.id} ...")
        Thread.sleep(1000) // Simulamos un proceso pesado, una operación que tardaría 1 segundo

        if(amount <= balance) {
            to.deposit(amount)
            retire(amount)
            log("[$id] Transfer completed!")
        } else {
            log("[$id] Couldn't make this transfer. Please check your balance.")
        }
    }

    override fun deposit(amount: Int) = lock.withLock {
        log("[$id] Making a deposit...")
        Thread.sleep(1000)// Simulamos un proceso pesado, una operación que tardaría 1 segundo
        balance += amount
        log("[$id] Deposit completed!")
    }

    override fun retire(amount: Int) = lock.withLock {
        log("[$id] Making a retirement...")
        Thread.sleep(1000)// Simulamos un proceso pesado, una operación que tardaría 1 segundo
        balance -= amount
        log("[$id] Retirement completed!")
    }
}

/**
 * Si deseas comprobar que el código del ejemplo anterior realmente funciona, puedes comentar alguna de las dos
 * coroutines y realizar la transferencia solamente desde una cuenta, o puedes retrasar la ejecución de alguna de las
 * dos coroutines con una llamada a la función delay. Solo procura que el retraso sea de más de 1 segundo.
 */
fun main() {
    log("Start Bank")

    val accountA = SavingsAccount("A-101", 100)
    val accountB = SavingsAccount("B-777", 86)

    log("[${accountA.id}] Balance = ${accountA.balance}")
    log("[${accountB.id}] Balance = ${accountB.balance}")

    // Ejecutamos paralelamente
    runBlocking {
        // Lanzamos una corrutina
        launch(Dispatchers.Default) {
            accountA.transfer(50, accountB)
        }
        // Lanzamos una corrutina
        launch(Dispatchers.Default) {
            //delay(1500) // PAra que no haya bloqueos la retrasamos
            accountB.transfer(43, accountA)
        }
    }

    log("[${accountA.id}] Balance = ${accountA.balance}")
    log("[${accountB.id}] Balance = ${accountB.balance}")

    log("End")
}