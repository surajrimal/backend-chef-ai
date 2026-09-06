package com.chefai.security.history;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class HistoryRequest {

    private String id;
    private String html;
    private List<String> ingredients;
}
