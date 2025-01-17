package com.oo.srv

import com.oo.srv.core.InvitingCodeRepository
import com.oo.srv.core.OrderRepository
import com.oo.srv.core.WaitressRepository
import jakarta.annotation.Resource
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.Expression
import org.hibernate.Session
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime


@NoRepositoryBean
interface ParentRepository<T, ID> : JpaRepository<T, ID>, PagingAndSortingRepository<T, ID>
@Repository
interface BizApiCallRepository:ParentRepository<BizApiCall,Long>{
    //https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
    fun findByIdIn(ages: Collection<Long>): List<BizApiCall>
    fun findByStartTimeGreaterThanAndEndTimeLessThan(t1:LocalDateTime,t2:LocalDateTime):List<BizApiCall>
}
@Repository
interface SysApiCallRepo:ParentRepository<SysApiCall,Long>
@Repository
interface BizTranRepo:ParentRepository<BizTransaction,String>
//@Repository
//interface SysTokenRepo:ParentRepository<SysToken,String>
@Repository
interface SysUserRepo:ParentRepository<SysUser,String>

//@Repository
//interface SysRoleRepo:ParentRepository<SysRole,String>
//@Repository
//interface SysPowerRepo:ParentRepository<SysPower,Long>
//@Repository
//interface SysRolePowersRepo:ParentRepository<SysRolePowers,Long>
@Repository
interface UploadFileInfoRepo:ParentRepository<UploadFileInfo,FileInfoId>
@Repository
interface WaitressRepo:ParentRepository<ActressUser,Long>//TODO



@Component
class OrderRepositoryImpl: OrderRepository
@Component
class InvitingCodeRepositoryImpl: InvitingCodeRepository
@Component
class WaitressRepositoryImpl(
    @Resource private val repo:WaitressRepo
): WaitressRepository {

}
//https://docs.spring.io/spring-data/jpa/reference/repositories/query-by-example.html
fun useExample(repo:BizApiCallRepository){
    val bean = BizApiCall().also{it.version++}
    val match = ExampleMatcher.matching().withIgnoreNullValues()
    val ex = Example.of(bean,match)
    val sort = Sort.by(Sort.Order.asc("cost"),Sort.Order.desc("createTime"))
    val res = repo.findAll(ex,sort)
    repo.findBy(ex){
    }
}
fun useJdbc(mgr: EntityManager){
    val session: Session = mgr.unwrap(Session::class.java)
    val t =session.beginTransaction()
    try{
        session.doWork { conn->
            val ps = conn.prepareStatement("select * from sys_file as t where id = ?")
            ps.setLong(1, 1L)
            val rs = ps.executeQuery()
            while (rs.next()) {
                val id = rs.getString("id")
                val name = rs.getString("name")
            }
            rs.close()
            ps.close()
        }
        t.commit()
    }catch (ignore:Throwable){
        t.rollback()
    }
    session.close()
    //如果要将很多对象持久化，你必须通过经常的调用 flush() 以及稍后调用 clear() 来控制第一级缓存的大小。
    mgr.flush() //if change the entity state
    mgr.clear() //clear cache
}
private fun useCriteriaPage(manager: EntityManager,namePattern:String):Pair<Long,List<SysUser>>{
    val builder = manager.criteriaBuilder

    val cq = builder.createQuery(Long::class.java)
    val model = cq.from(SysUser::class.java)
    cq.where(builder.like(model["name"],"%$namePattern%"))
    cq.select(builder.count(model))

    val count = manager.createQuery(cq).singleResult


    val query = builder.createQuery(SysUser::class.java).select(model).where(builder.like(model["name"],"%$namePattern%"))
    val result = manager.createQuery(query).also { it.firstResult = 0;it.maxResults =5 }

    return count to result.resultList
}
fun useCriteria(entityManager: EntityManager){
    val builder = entityManager.criteriaBuilder
    val query = builder.createQuery(SysUser::class.java)
    val model = query.from(SysUser::class.java)

    val name:Expression<String> = model["uname"]
    val age:Expression<Int> = model["age"]
    val cond1 = builder.gt(age,10)
    val cond2 = builder.like(name,"%min%")
    val combineCondition = builder.and(cond1,cond2)

    val typedQuery = query.select(model)
        .where(combineCondition)
        .orderBy(builder.asc(age),builder.desc(name))
        .let { entityManager.createQuery(it) }//sql no execution

    val pageNumber = 1
    val pageSize = 5
    typedQuery.firstResult = (pageNumber-1)*pageSize
    typedQuery.maxResults = pageSize
//        val record = result.singleResult
    val records = typedQuery.resultList //execution is lazy
    println(records)
    println("list result...")

    val conditions = listOf<Any>(
        builder.equal(name,"admin")
        ,builder.le(age,3)
        ,builder.lessThan(age,3)
        ,builder.lessThanOrEqualTo(age,3)
        ,builder.like(name,"%min%")
        ,builder.between(age,10,20)
        ,builder.isNull(name)
        ,builder.isNotNull(name)
    )
}
@Service
class DemoService(
    @Resource
    @PersistenceContext
    private val entityManager: EntityManager
){
    init{
        val session = entityManager.unwrap(Session::class.java)
        val ss = session.sessionFactory.openStatelessSession()
        ss.close()
        session.close()
    }
    @Resource
    private lateinit var userRepo:SysUserRepo
    @Resource
    private lateinit var transactionTemplate: TransactionTemplate

}