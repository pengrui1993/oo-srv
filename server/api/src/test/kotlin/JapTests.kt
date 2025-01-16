import com.oo.srv.Application
import com.oo.srv.SysUser
import com.oo.srv.SysUserRepo
import jakarta.annotation.Resource
import jakarta.persistence.*
import org.hibernate.Session
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Example
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime


@Entity
@Table(name = "Events")
class Event(
	val title: String? = null
	,@Column(name = "eventDate")
	val date: LocalDateTime? = null
){
	@Id
	@GeneratedValue
	val id: Long? = null
}

fun hibernateQuickstart(){
	val registry = StandardServiceRegistryBuilder()
		.applySetting("jakarta.persistence.jdbc.url","jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1")
		.applySetting("jakarta.persistence.jdbc.user","sa")
		.applySetting("jakarta.persistence.jdbc.password","")
		.applySetting("jakarta.persistence.schema-generation.database.action","create-drop")
		.applySetting("hibernate.show_sql","true")
		.applySetting("hibernate.format_sql","true")
		.applySetting("hibernate.highlight_sql","true")
		.build()
	runCatching {
		val sessionFactory = MetadataSources(registry)
			.addAnnotatedClass (Event::class.java)
			.buildMetadata()
			.buildSessionFactory()

		val now = {LocalDateTime.now()}
		sessionFactory.inTransaction { session: Session ->
			session.persist(Event("Our very first event!", now()))
			session.persist(Event("A follow up event", now()))
		}
		sessionFactory.inTransaction { session: Session ->
			session.createSelectionQuery("from Event",Event::class.java)
				.resultList.forEach { event: Event ->
					println("Event (" + event.date + ") : " + event.title)
				}
		}
		sessionFactory.close()
	}.onFailure {
		StandardServiceRegistryBuilder.destroy(registry)
	}
}


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
