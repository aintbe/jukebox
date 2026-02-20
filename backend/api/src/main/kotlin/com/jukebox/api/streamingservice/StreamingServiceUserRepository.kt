package com.jukebox.api.streamingservice

import com.jukebox.api.streamingservice.entity.StreamingServiceUser
import com.jukebox.api.streamingservice.entity.StreamingServiceUserPK
import org.springframework.data.jpa.repository.JpaRepository

interface StreamingServiceUserRepository : JpaRepository<StreamingServiceUser, StreamingServiceUserPK>
