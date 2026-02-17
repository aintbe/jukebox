import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class OAuth2Id(
    @Column(length = 10) val provider: String,
    @Column(length = 100) val providerId: String,
) : Serializable
