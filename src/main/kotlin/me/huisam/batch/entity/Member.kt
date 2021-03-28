package me.huisam.batch.entity

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
) {
    override fun toString(): String {
        return "Member(id=$id, name='$name', address='$address', age=$age, team=$team)"
    }
}
