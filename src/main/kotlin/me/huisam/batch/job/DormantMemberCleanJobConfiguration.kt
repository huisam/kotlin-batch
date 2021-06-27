package me.huisam.batch.job

import me.huisam.batch.entity.Member
import me.huisam.batch.entity.MemberStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.persistence.EntityManagerFactory

@Configuration
class DormantMemberCleanJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory,
) {
    companion object {
        const val JOB_NAME = "dormantMemberCleanJob"
        const val STEP_NAME = "dormantMemberCleanStep"
    }

    private val chunkSize = 10

    @Bean
    fun dormantMemberCleanJob(): Job = jobBuilderFactory.get(JOB_NAME)
        .start(dormantMemberCleanStep())
        .build()

    @Bean
    @JobScope
    fun dormantMemberCleanStep(
        @Value("#{jobParameters[requestedAt]}") requestedAt: Date? = null,
    ): Step = stepBuilderFactory.get(STEP_NAME)
        .chunk<Member, Member>(chunkSize)
        .reader(dormantMemberReader())
        .processor(dormantMemberProcessor())
        .writer(dormantMemberWriter())
        .build()

    @Bean
    @StepScope
    fun dormantMemberReader(
        @Value("#{jobParameters[dormantDate]}") dormantDate: String? = null
    ): JpaPagingItemReader<out Member> = JpaPagingItemReaderBuilder<Member>()
        .name(DormantMemberCleanJobConfiguration::dormantMemberReader.name)
        .entityManagerFactory(entityManagerFactory)
        .queryString(
            """
                SELECT m FROM Member m
                WHERE m.${Member::status.name} != :status and m.${Member::modifiedAt.name} <= :dormantDate
                ORDER BY m.${Member::id.name}
            """.trimIndent()
        )
        .parameterValues(
            mapOf(
                "status" to MemberStatus.DORMANT,
                "dormantDate" to LocalDate.parse(
                    dormantDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                ).atStartOfDay()
            )
        )
        .entityManagerFactory(entityManagerFactory)
        .maxItemCount(chunkSize)
        .build()

    @Bean
    fun dormantMemberProcessor() = ItemProcessor<Member, Member> {
        it.status = MemberStatus.DORMANT
        it
    }

    @Bean
    fun dormantMemberWriter() = JpaItemWriterBuilder<Member>()
        .usePersist(false)
        .entityManagerFactory(entityManagerFactory)
        .build()
}