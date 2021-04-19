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
import org.springframework.batch.item.database.JpaCursorItemReader
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.persistence.EntityManagerFactory

@Configuration
class MemberJobCursorConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory,
) {
    private val chunkSize = 10
    private val logger = LoggerFactory.getLogger("memberJob")

    @Bean
    fun addressChangeJob(): Job = jobBuilderFactory.get("addressChangeJob")
        .start(addressChangeStep())
        .build()

    @Bean
    @JobScope
    fun addressChangeStep(
        @Value("#{jobParameters[requestedAt]}") requestedAt: Date? = null,
    ): Step = stepBuilderFactory.get("addressChangeStep")
        .chunk<Member, Member>(chunkSize)
        .reader(jpaCursorMemberItemReader())
        .processor(jpaMemberProcessor())
        .writer(jpaCursorMemberItemWriter())
        .build()

    @Bean
    fun jpaCursorMemberItemWriter(): JpaItemWriter<in Member> = JpaItemWriterBuilder<Member>()
        .entityManagerFactory(entityManagerFactory)
        .usePersist(false)
        .build()

    @Bean
    @StepScope
    fun jpaMemberProcessor(
        @Value("#{jobParameters[address]}") address: String? = null,
    ): ItemProcessor<Member, Member> {
        return ItemProcessor {
            logger.info("before Processing, $it")
            Member(
                id = it.id,
                name = it.name,
                address = address!!,
                age = it.age,
                team = it.team
            ).also { member -> logger.info("After Processing = $member") }
        }
    }

    @Bean
    fun jpaCursorMemberItemReader(): JpaCursorItemReader<out Member> {
        return JpaCursorItemReaderBuilder<Member>()
            .name(this::jpaCursorMemberItemReader.name)
            .entityManagerFactory(entityManagerFactory)
            .queryString(
                """
                    SELECT m FROM Member m
                    WHERE m.${Member::age.name} >= 13
                """.trimIndent()
            )
            .maxItemCount(chunkSize)
            .build()
    }
}