package com.jukebox.backend.user.entity

import com.jukebox.backend.common.entity.BaseEntity
import com.jukebox.backend.streamingservice.entity.StreamingServiceUser
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "app_user")
class User(
    @Column(nullable = false, unique = true, length = 100)
    var username: String,
    // TODO: add password for regular users
    // @Column()
    // var password: String? = null
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    val streamingServiceUser: StreamingServiceUser? = null,
) : BaseEntity()
