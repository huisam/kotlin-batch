package me.huisam.batch

import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import javax.persistence.EntityManagerFactory

@Import(TestBatchConfiguration::class)
@SpringBatchTest
abstract class BaseBatchJobTest {
    @Autowired
    protected lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    protected lateinit var entityManagerFactory: EntityManagerFactory

    private val entityManager by lazy { entityManagerFactory.createEntityManager() }

    protected fun <T> save(entity: T): T {
        entityManager.transaction.also {
            it.begin()
            entityManager.persist(entity)
            it.commit()
            entityManager.clear()
        }
        return entity
    }

    protected fun <T> saveAll(entities: List<T>): List<T> {
        entityManager.transaction.also {
            it.begin()
            entities.forEach { entity -> entityManager.persist(entity) }
            it.commit()
            entityManager.clear()
        }
        return entities
    }

    protected fun <T> mergeAll(entities: List<T>): List<T> {
        entityManager.transaction.also {
            it.begin()
            entities.forEach { entity -> entityManager.merge(entity) }
            it.commit()
            entityManager.clear()
        }
        return entities
    }

    protected fun deleteAll(tableName: String) {
        entityManager.transaction.also {
            it.begin()
            entityManager.createQuery(
                """
                DELETE FROM $tableName
                """.trimIndent()
            ).executeUpdate()
            it.commit()
        }
    }
}