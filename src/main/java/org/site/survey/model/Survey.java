package org.site.survey.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("surveys")
public class Survey {
    @Id
    private Integer id;
    private String title;
    private String description;
    private Integer createdBy;
    private LocalDateTime createdAt;
}