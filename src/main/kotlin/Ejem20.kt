// Ejemplo de reactividad usando patrón observer
// https://refactoring.guru/es/design-patterns/observer

// Se crea una clase Observable que es la que va a ser observada
class Observable<T> {
    // Debemos tener una lista para nuestros observadores
    private val observers = mutableListOf<Observer<T>>()

    // Se crea un método para agregar observadores
    fun addObserver(observer: Observer<T>) {
        observers.add(observer)
    }

    // Se crea un método para eliminar observadores
    fun removeObserver(observer: Observer<T>) {
        observers.remove(observer)
    }

    // Se crea un método para notificar a los observadores
    fun notifyObservers(value: T) {
        println("Notificando a los observadores")
        observers.forEach { it.update(value) }
    }
}

// Class observer
class Observer<T>(val id: Int) {
    // Se crea un método para actualizar cuando nos llamen
    fun update(value: T) {
        println("Observer $id: $value")
    }
}

// main
fun main() {
    val observable = Observable<String>()
    val observer1 = Observer<String>(1)
    val observer2 = Observer<String>(2)
    observable.addObserver(observer1)
    observable.addObserver(observer2)
    observable.notifyObservers("Hello")
}