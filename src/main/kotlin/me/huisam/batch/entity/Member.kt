package me.huisam.batch.entity

import me.huisam.batch.entity.date.AuditableDate
import javax.persistence.*

@Entity
@Table
class Member(
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    val id: Long? = null,

    @Column
    val name: String,

    @Column
    val address: String,

    @Column
    val age: Long,

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

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Member(id=$id, name='$name', address='$address', age=$age, team=$team)"
    }
}
