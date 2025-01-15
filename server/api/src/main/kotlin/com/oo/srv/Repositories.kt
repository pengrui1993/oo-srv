package com.oo.srv

import com.oo.srv.core.InvitingCodeRepository
import com.oo.srv.core.WaitressRepository
import jakarta.annotation.Resource
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.hibernate.Session
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
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
@Repository
interface SysTokenRepo:ParentRepository<SysToken,String>
@Repository
interface SysRoleRepo:ParentRepository<SysRole,String>
@Repository
interface SysPowerRepo:ParentRepository<SysPower,Long>
@Repository
interface SysRolePowersRepo:ParentRepository<SysRolePowers,Long>
@Repository
interface UploadFileInfoRepo:ParentRepository<UploadFileInfo,Long>
@Repository
interface WaitressRepo:ParentRepository<BizApiCall,Long>//TODO

@Component
class InvitingCodeRepositoryImpl: InvitingCodeRepository
@Component
class WaitressRepositoryImpl(
    @Resource private val repo:WaitressRepo
): WaitressRepository {

}
//https://docs.spring.io/spring-data/jpa/reference/repositories/query-by-example.html
fun findByExample(repo:BizApiCallRepository){
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
    session.doWork { conn->
        val ps = conn.prepareStatement("select * from sys_file as t where id = ?")
        ps.setLong(1,1L)
        val rs = ps.executeQuery()
        while(rs.next()){
            val id = rs.getString("id")
            val name = rs.getString("name")
        }
        rs.close()
        ps.close()
    }
    session.close()
    mgr.flush() //if change the entity state
}
class DemoService{
    @Resource
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    @Resource
    private lateinit var transactionTemplate: TransactionTemplate
}