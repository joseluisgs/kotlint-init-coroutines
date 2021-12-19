import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue

@DisplayName("App Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Para el Beforeall
class AppTest {

    @BeforeAll
    fun setUp() {
        println("BeforeAll")
    }


    @Test
    @DisplayName("True is True")
    @Order(1)
    fun trueIsTrue() {
        // Estructura de un test
        // Arrange
        // Act
        // Assert
        assertTrue(true)
    }
}