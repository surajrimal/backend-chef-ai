package com.chefai.security.history;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HistoryRequest {

    private String id;
    private String html;
}
