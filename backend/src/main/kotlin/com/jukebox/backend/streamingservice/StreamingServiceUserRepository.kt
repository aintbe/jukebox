package com.jukebox.backend.streamingservice

import com.jukebox.backend.streamingservice.entity.StreamingServiceUser
import com.jukebox.backend.streamingservice.entity.StreamingServiceUserPK
import org.springframework.data.jpa.repository.JpaRepository

interface StreamingServiceUserRepository : JpaRepository<StreamingServiceUser, StreamingServiceUserPK>
