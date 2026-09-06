package com.chefai.security.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "history")
public class History {

    @Id
    private String id;
    private List<String> ingredients;
    private String htmlPage;


    @LastModifiedDate
    private LocalDateTime lastModified;


    @LastModifiedBy
    private String lastModifiedBy;
}
