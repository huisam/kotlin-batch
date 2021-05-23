package me.huisam.batch.job

import me.huisam.batch.BaseBatchJobTest
import me.huisam.batch.entity.Member
import me.huisam.batch.entity.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*


@SpringBootTest(classes = [MemberJobCursorConfiguration::class])
internal class MemberJobCursorConfigurationTest : BaseBatchJobTest() {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @BeforeEach
    fun setUp() {
        val members = listOf(
            Member(
                name = "member1",
                address = "Seoul",
                age = 11L
            ),
            Member(
                name = "member2",
                address = "Seoul2",
                age = 12L
            ),
            Member(
                name = "member3",
                address = "Seoul3",
                age = 13L
            ),
            Member(
                name = "member4",
                address = "Seoul4",
                age = 14L
            )
        )
        saveAll(members)
    }

    @AfterEach
    fun tearDown() {
        deleteAll(Member::class.simpleName!!)
    }

    @Test
    fun `address를 성공적으로 변환한다`() {
        // given
        val jobParameters = JobParametersBuilder()
            .addString("address", "Busan")
            .addDate("requestedAt", Date())
            .toJobParameters()

        // when
        val result = jobLauncherTestUtils.launchJob(jobParameters)

        // then
        assertThat(result.status).isEqualTo(BatchStatus.COMPLETED)
        val members = memberRepository.findAll()
        assertThat(members)
            .filteredOn { it.age >= 13 }
            .allSatisfy { assertThat(it.address).isEqualTo("Busan") }
    }
}
