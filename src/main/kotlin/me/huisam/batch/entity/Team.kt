package me.huisam.batch.entity

import javax.persistence.*

@Entity
@Table
class Team(
    @Id
    @GeneratedValue
    @Column(name = "team_id")
    val id: Long? = null,

    @Column
    val name: String,

    @Column
    val rank: Long,

    @OneToMany(mappedBy = "team")
    val members: MutableList<Member> = mutableListOf(),
) {
    fun addMember(member: Member) {
        this.members.add(member)
        member.team = this
    }
}