package com.chefai.security.book;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookRequest {

    private String id;
    private String author;
    private String isbn;
    private String content;
}
