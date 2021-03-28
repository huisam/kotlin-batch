package me.huisam.batch

import me.huisam.batch.entity.Member
import me.huisam.batch.entity.MemberRepository
import me.huisam.batch.entity.Team
import me.huisam.batch.entity.TeamRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(TestBatchConfig::class)
@SpringBatchTest
abstract class BaseBatchJobTest {
    @Autowired
    protected lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    protected lateinit var memberRepository: MemberRepository

    @Autowired
    protected lateinit var teamRepository: TeamRepository

    @BeforeEach
    fun setUp() {
        val member1 = Member(
            name = "member1",
            address = "Seoul",
            age = 11L
        )
        val member2 = Member(
            name = "member2",
            address = "Seoul2",
            age = 12L
        )
        val member3 = Member(
            name = "member3",
            address = "Seoul3",
            age = 13L
        )
        val member4 = Member(
            name = "member4",
            address = "Seoul4",
            age = 14L
        )

        val teamA = Team(
            name = "A",
            rank = 1L
        ).also {
            it.addMember(member1)
            it.addMember(member2)
        }
        val teamB = Team(
            name = "B",
            rank = 2L
        ).also {
            it.addMember(member3)
            it.addMember(member4)
        }

        teamRepository.save(teamA)
        teamRepository.save(teamB)
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)
        memberRepository.save(member4)
    }

    @AfterEach
    fun tearDown() {
        memberRepository.deleteAllInBatch()
        teamRepository.deleteAllInBatch()
    }
}