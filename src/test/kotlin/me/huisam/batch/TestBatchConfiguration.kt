package me.huisam.batch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@TestConfiguration
@EnableAutoConfiguration
@EnableJpaAuditing
@EnableBatchProcessing
class TestBatchConfiguration