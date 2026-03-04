package com.jukebox.api.jukebox.entity

import com.jukebox.api.common.entity.BaseEntity
import com.jukebox.api.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class Jukebox(
    @Column(nullable = false, unique = true, length = 20)
    var handle: String,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val host: User,
) : BaseEntity()
