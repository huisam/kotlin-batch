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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Team) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Team(id=$id, name='$name', rank=$rank)"
    }
}