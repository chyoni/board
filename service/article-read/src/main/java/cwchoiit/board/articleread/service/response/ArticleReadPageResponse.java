package cwchoiit.board.articleread.service.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArticleReadPageResponse {
    private List<ArticleReadResponse> articles;
    private Long articleCount;

    public static ArticleReadPageResponse of(List<ArticleReadResponse> articles, Long articleCount) {
        ArticleReadPageResponse articleReadPageResponse = new ArticleReadPageResponse();
        articleReadPageResponse.articles = articles;
        articleReadPageResponse.articleCount = articleCount;
        return articleReadPageResponse;
    }
}
