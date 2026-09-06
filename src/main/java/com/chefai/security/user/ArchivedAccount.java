package com.chefai.security.user;

import com.chefai.security.book.Book;
import com.chefai.security.history.History;
import com.chefai.security.token.Token;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "archived_accounts")
public class ArchivedAccount {

    @Id
    private String id;
    private String userId;
    private LocalDateTime archivedAt;
    private User user;
    private List<Book> books;
    private List<History> histories;
    private List<Token> tokens;
}
