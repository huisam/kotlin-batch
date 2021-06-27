package me.huisam.batch.entity

import me.huisam.batch.entity.date.AuditableDate
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "member_table")
class Member(
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    val id: Long? = null,

    @Column(name = "name")
    val name: String,

    @Column(name = "address")
    val address: String,

    @Column(name = "age")
    val age: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "dormant")
    var status: MemberStatus = MemberStatus.ACTIVE,

    @ManyToOne
    @JoinColumn(name = "team_id")
    var team: Team? = null,
) : AuditableDate() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode() = Objects.hashCode(id)

    override fun toString(): String {
        return "Member(id=$id, name='$name', address='$address', age=$age, team=${team?.id})"
    }
}

enum class MemberStatus {
    ACTIVE, DORMANT
}
