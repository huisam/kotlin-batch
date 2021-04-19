package me.huisam.batch.job

import me.huisam.batch.BaseBatchJobTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.boot.test.context.SpringBootTest
import java.util.*


@SpringBootTest(classes = [MemberJobPagingConfiguration::class])
internal class MemberJobPagingConfigurationTest : BaseBatchJobTest() {
    @Test
    fun `name을 성공적으로 변환한다`() {
        // given
        val jobParameters = JobParametersBuilder()
            .addString("name", "afterTest")
            .addDate("requestedAt", Date())
            .toJobParameters()

        // when
        val result = jobLauncherTestUtils.launchJob(jobParameters)

        // then
        assertThat(result.status).isEqualTo(BatchStatus.COMPLETED)
        val members = memberRepository.findAll()
        assertThat(members)
            .filteredOn { it.age >= 13 }
            .allSatisfy { assertThat(it.name).isEqualTo("afterTest") }
    }

}
