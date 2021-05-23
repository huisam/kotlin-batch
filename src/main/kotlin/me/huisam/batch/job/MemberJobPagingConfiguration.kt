package me.huisam.batch.job

import me.huisam.batch.entity.Member
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.persistence.EntityManagerFactory

@Configuration
class MemberJobPagingConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory,
) {
    private val chunkSize = 10
    private val logger = LoggerFactory.getLogger("memberJob")

    @Bean
    fun addressChangeJobPaging(): Job = jobBuilderFactory.get("addressChangeJobPaging")
        .start(addressChangeStepPaging())
        .build()

    @Bean
    @JobScope
    fun addressChangeStepPaging(
        @Value("#{jobParameters[requestedAt]}") requestedAt: Date? = null,
    ): Step = stepBuilderFactory.get("addressChangeStepPaging")
        .chunk<Member, Member>(chunkSize)
        .reader(jpaPagingMemberItemReader())
        .processor(jpaMemberProcessorPaging())
        .writer(jpaMemberItemWriterPaging())
        .build()

    @Bean
    fun jpaMemberItemWriterPaging(): JpaItemWriter<in Member> = JpaItemWriterBuilder<Member>()
        .entityManagerFactory(entityManagerFactory)
        .usePersist(false)
        .build()

    @Bean
    @StepScope
    fun jpaMemberProcessorPaging(
        @Value("#{jobParameters[name]}") name: String? = null,
    ): ItemProcessor<Member, Member> {
        return ItemProcessor {
            logger.info("before Processing, $it")
            Member(
                id = it.id,
                name = name!!,
                address = it.address,
                age = it.age,
                team = it.team
            ).also { member -> logger.info("After Processing = $member") }
        }
    }

    @Bean
    fun jpaPagingMemberItemReader(): JpaPagingItemReader<out Member> {
        return JpaPagingItemReaderBuilder<Member>()
            .name(this::jpaPagingMemberItemReader.name)
            .entityManagerFactory(entityManagerFactory)
            .queryString(
                """
                    SELECT m FROM Member m
                    WHERE m.${Member::age.name} >= 13
                    ORDER BY ${Member::id.name}
                """.trimIndent()
            )
            .maxItemCount(chunkSize)
            .build()
    }
}