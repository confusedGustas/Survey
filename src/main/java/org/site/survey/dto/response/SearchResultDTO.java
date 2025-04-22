package org.site.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private String index;
    private Integer id;
    private String type;
    private Object content;
} 