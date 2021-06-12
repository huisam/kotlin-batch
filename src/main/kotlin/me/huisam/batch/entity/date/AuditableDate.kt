package me.huisam.batch.entity.date

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.sql.Timestamp
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AuditableDate {
    @CreatedDate
    @Column(name = "created_at")
    var createdAt: Timestamp? = null

    @LastModifiedDate
    @Column(name = "modified_at")
    var modifiedAt: Timestamp? = null
}