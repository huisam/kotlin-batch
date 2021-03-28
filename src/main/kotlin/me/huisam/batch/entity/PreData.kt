package me.huisam.batch.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct


interface TeamRepository : JpaRepository<Team, Long>
interface MemberRepository : JpaRepository<Member, Long>

@Component
class PreData(
    private val teamRepository: TeamRepository,
    private val memberRepository: MemberRepository,
) {

    @PostConstruct
    @Transactional
    fun init() {
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
}