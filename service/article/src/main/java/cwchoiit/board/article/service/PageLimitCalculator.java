package cwchoiit.board.article.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PageLimitCalculator {

    /**
     * 유저가 특정 페이지에 존재할 때 필요한 최소 게시글 카운트를 구한다.
     * 예를 들어, 게시글이 100백만건이라도 100백만건 전체 개수를 가져올 필요는 없다.
     * 만약, 유저가 1페이지에 있고 한 페이지에 보여줄 게시글 개수가 30개고 최대 보여줄 페이지 이동 번호 버튼이 10개라면(1, 2, ..., 10)
     * 최대 301개를 가져오면 된다. 300개까지는 10페이지일 것이고 301개가 있다면 다음 버튼을 보여주면 된다.
     * 유저가 2페이지, 3페이지, ..., 10페이지까지는 301개까지만 가져오면 된다.
     * 11페이지부터는 601개를 가져오면 된다.
     * 그리고 가져온 개수를 토대로 계산해서 페이지 이동 번호 버튼을 앞단에서 보여주면 된다.
     * @param page 현재 페이지
     * @param pageSize 한 페이지 당 보여줄 게시글의 개수
     * @param movablePageCount 한 화면에서 이동할 수 있는 페이지 번호 개수 (예를 들어, 한 화면에 최대 보여줄 페이지 이동 번호 버튼이 10개면 10)
     * @return 게시글 개수
     */
    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount) {
        return (((page - 1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }
}
