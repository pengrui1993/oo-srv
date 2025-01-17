import com.oo.srv.api.Application
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test


@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KotlinTestingDemoApplicationIntegrationTest {

    @Test
    fun hello(){

    }
}