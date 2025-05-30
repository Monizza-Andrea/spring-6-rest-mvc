package guru.springframework.spring6restmvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Version;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerDTO {

    private UUID id;
    private String customerName;
    private Integer version;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    @Column(length = 255)
    private String email;
}
