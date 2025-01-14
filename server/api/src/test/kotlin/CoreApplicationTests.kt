import com.oo.srv.Application
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
	classes = [Application::class],
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CoreApplicationTests {

	@Test
	fun contextLoads() {
	}

}
