package me.huisam.batch.job

import me.huisam.batch.BaseBatchJobTest
import me.huisam.batch.entity.Member
import me.huisam.batch.entity.MemberRepository
import me.huisam.batch.entity.MemberStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest(classes = [DormantMemberCleanJobConfiguration::class])
internal class DormantMemberCleanJobConfigurationTest : BaseBatchJobTest() {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @BeforeEach
    fun setUp() {
        val members = listOf(
            Member(
                name = "member1",
                address = "Seoul",
                age = 11L,
                status = MemberStatus.ACTIVE
            ).apply {
                createdAt = LocalDateTime.now().minusDays(5)
                modifiedAt = LocalDateTime.now().minusDays(5)
            },
            Member(
                name = "member2",
                address = "Seoul2",
                age = 12L,
                status = MemberStatus.ACTIVE
            ).apply {
                createdAt = LocalDateTime.now().minusDays(3)
                modifiedAt = LocalDateTime.now().minusDays(3)
            },
            Member(
                name = "member3",
                address = "Seoul3",
                age = 14L,
                status = MemberStatus.ACTIVE
            ).apply {
                createdAt = LocalDateTime.now().minusDays(1)
                modifiedAt = LocalDateTime.now().minusDays(1)
            }
        )
        saveAll(members)
    }

    @AfterEach
    fun tearDown() {
        deleteAll(Member::class.simpleName!!)
    }

    @Test
    fun `마지막 수정날짜가 2일전보다 이전 Member들의 status를 Dormant 처리한다`() {
        // given
        val jobParameters = JobParametersBuilder()
            .addString(
                "dormantDate", LocalDateTime.now()
                    .minusDays(2)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
            .addDate("requestedAt", Date())
            .toJobParameters()

        // when
        val result = jobLauncherTestUtils.launchJob(jobParameters)

        // then
        assertThat(result.status).isEqualTo(BatchStatus.COMPLETED)
        val members = memberRepository.findAll()
        assertThat(members).filteredOn { it.status === MemberStatus.DORMANT }.hasSize(2)
        assertThat(members).filteredOn { it.status !== MemberStatus.DORMANT }.hasSize(1)
    }
}