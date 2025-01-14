package com.oo.srv

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


@NoRepositoryBean
interface ParentRepository<T, ID> : JpaRepository<T, ID>, PagingAndSortingRepository<T, ID>

@Repository
interface BizTranRepository:ParentRepository<BizTransaction,String>
@Repository
interface SysTokenRepository:ParentRepository<SysToken,String>
@Repository
interface SysRoleRepository:ParentRepository<SysRole,String>
@Repository
interface SysPowerRepository:ParentRepository<SysPower,Long>
@Repository
interface SysRolePowersRepository:ParentRepository<SysRolePowers,Long>
@Repository
interface BizApiCallRepository:ParentRepository<BizApiCall,Long>{
    //https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
    fun findByIdIn(ages: Collection<Long>): List<BizApiCall>
}
@Repository
interface WaitressRepo:ParentRepository<BizApiCall,Long>
@Repository
interface UploadFileInfoRepo:ParentRepository<UploadFileInfo,Long>
@Component
class WaitressRepositoryImpl(
    @Resource private val repo:WaitressRepo
): WaitressRepository {

}
//https://docs.spring.io/spring-data/jpa/reference/repositories/query-by-example.html
fun findByExample(repo:BizApiCallRepository){
    val match = ExampleMatcher.matching().withIgnoreNullValues()
    val ex = Example.of(BizApiCall().also{
        it.version++
    },match)
    val sort = Sort.by(Sort.Order.asc("cost"))
    val res = repo.findAll(ex,sort)
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
}