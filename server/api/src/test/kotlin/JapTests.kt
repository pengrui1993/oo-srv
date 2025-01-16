import com.oo.srv.*
import jakarta.annotation.Resource
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Example
import org.springframework.test.context.ActiveProfiles

/**
 * just use h2 database if that exists
 */
@ActiveProfiles("test")//dev test
@SpringBootTest(
	classes = [Application::class],
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JapTests {
	@Resource
	@PersistenceContext
	lateinit var entityManager: EntityManager
	@Resource
	lateinit var userRepo: SysUserRepo
	@AfterEach fun ae(){println("after each...")}
	@BeforeEach
	fun beforeEach(){ println("before each...")}
	@Test
	fun testSave() {
		val saved = userRepo.save(SysUser().also { it.name="tony";it.role="admin-token";it.uname="admin";it.upwd="111111" })
		val one = userRepo.findOne(Example.of(SysUser().clear().also {
			it.name = "tony";it.uname = "admin"
		}))
		Assertions.assertEquals(saved.id,one.get().id)
	}
	/*
	https://www.baeldung.com/hibernate-criteria-queries
	 */
	@Test
	fun testCriteria(){
		userRepo.save(SysUser().clear().also {
			it.name = "tony";it.uname = "admin";it.age=13
		})
		val cb = entityManager.criteriaBuilder
		val queryCreator = cb.createQuery(SysUser::class.java)
		val model = queryCreator.from(SysUser::class.java)
		val cond1 = cb.gt(model.get("age"),10)
		val cond2 = cb.like(model.get("uname"),"%min%")
		val combineCondition = cb.and(cond1,cond2)
		val criteriaQuery = queryCreator.select(model).where(combineCondition)
		val result = entityManager.createQuery(criteriaQuery)
		val list = result.resultList
		println("list result...")
		println(list)
	}

	companion object{
		@BeforeAll @JvmStatic
		fun before(){
			println("BeforeAll...")
		}
		@AfterAll @JvmStatic
		fun after(){
			println("AfterAll...")
		}
	}
}
