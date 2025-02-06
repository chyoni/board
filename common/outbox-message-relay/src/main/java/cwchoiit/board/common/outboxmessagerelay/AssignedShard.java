package cwchoiit.board.common.outboxmessagerelay;

import lombok.Getter;

import java.util.List;
import java.util.stream.LongStream;

/**
 * 샤드를 균등하게 할당하기 위해 사용될 클래스
 * 예를 들어, 2개의 애플리케이션이 4개의 샤드가 있다면 1번 애플리케이션은 0,1번 샤드를, 2번 애플리케이션은 2,3번 샤드를 담당해서 Polling 해야 하므로.
 */
@Getter
public class AssignedShard {
    private List<Long> shards; // 애플리케이션에 할당된 샤드 번호들을 담고 있는 리스트

    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        AssignedShard assignedShard = new AssignedShard();
        assignedShard.shards = assign(appId, appIds, shardCount);
        return assignedShard;
    }

    /**
     * 현재 실행중인 애플리케이션에 할당될 샤드들의 번호를 리스트에 담아 반환한다.
     * @param appId 현재 실행중인 애플리케이션의 ID
     * @param appIds 실행중인 모든 애플리케이션의 정렬된 상태의 리스트
     * @param shardCount 샤드 개수 (우리는 4개로 고정)
     * @return 현재 애플리케이션이 담당할 샤드의 번호들을 담은 리스트
     */
    private static List<Long> assign(String appId, List<String> appIds, long shardCount) {
        int appIndex = findAppIndex(appId, appIds); // 현재 애플리케이션의 인덱스를 찾는다.
        if (appIndex == -1) { // 인덱스가 -1이라면 현재 애플리케이션이 할당할 샤드가 없다는 것을 의미한다.
            return List.of(); // 빈 리스트를 반환
        }
        long start = appIndex * shardCount / appIds.size(); // 할당할 샤드의 첫번째 번호
        long end = ((appIndex + 1) * shardCount / appIds.size()) - 1; // 할당할 샤드의 마지막 번호

        // 예를 들어, 애플리케이션이 2개가 띄워져 있고, 샤드 개수가 4라면 0번 애플리케이션은 0~1 샤드를, 1번 애플리케이션은 2~3 샤드를 할당받게 된다.

        return LongStream.rangeClosed(start, end).boxed().toList();
    }

    /**
     * 파라미터로 받는 {@code appIds} 라는 실행된 애플리케이션의 정렬된 상태에서,
     * 현재 실행중인 애플리케이션인 {@code appId}가 몇번째 인덱스인지를 찾는 메서드.
     * @param appId 현재 실행중인 애플리케이션 ID
     * @param appIds 실행중인 모든 애플리케이션의 정렬된 상태의 리스트
     * @return 현재 실행중인 애플리케이션의 index
     */
    private static int findAppIndex(String appId, List<String> appIds) {
        for (int i = 0; i < appIds.size(); i++) {
            if (appIds.get(i).equals(appId)) {
                return i;
            }
        }
        return -1;
    }
}
