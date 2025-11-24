package castlesofburgundy.player;

import castlesofburgundy.view.PlayerConsoleView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalBoardRegionTest {

    private PersonalBoard boardWithCastle19;

    @BeforeEach
    void setUp() {
        PersonalLayout layout = new PersonalLayout();
        boardWithCastle19 = new PersonalBoard(layout);
        // 시작 성: 19번 칸 (이미 프로젝트에서 이렇게 쓰고 있었음)
        boardWithCastle19.setupInitialCastle(19);
    }

    @Test
    @DisplayName("같은 타입의 연결된 구역을 찾는다 - 중앙 성(19)은 단독 구역이다")
    void sameTypeRegion_singleCastle() {
        Set<Integer> region = boardWithCastle19.sameTypeRegion(19);

        // 19 주변에는 같은 타입(CASTLE) 이웃이 없으므로, 크기 1짜리 구역이어야 한다.
        assertThat(region)
                .containsExactly(19); // 순서까지 신경 안 쓰려면 containsOnly(19) 로 바꿔도 됨
    }

    @Test
    @DisplayName("같은 타입의 연결된 구역을 찾는다 - 2, 3, 7번 성은 한 구역이다")
    void sameTypeRegion_castleCluster() {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());

        // 2번 칸은 CASTLE이며, 레이아웃/격자 상에서 3, 7번과 연결된 CASTLE 구역을 이룬다.
        Set<Integer> region = board.sameTypeRegion(2);

        assertThat(region)
                .containsExactlyInAnyOrder(2, 3, 7);
    }

    @Test
    @DisplayName("이미 차 있는 칸으로만 이루어진 구역은 완성으로 간주한다")
    void regionCompleted_trueForFilledRegion() {
        // setupInitialCastle(19) 에 의해 19번 CASTLE 칸은 이미 배치된 상태.
        // 19번이 포함된 CASTLE 구역은 {19} 이므로, 이 구역은 이미 완성된 상태여야 한다.
        assertThat(boardWithCastle19.isRegionCompleted(19)).isTrue();
        assertThat(boardWithCastle19.completedRegionSizeIfAny(19)).isEqualTo(1);
    }

    @Test
    @DisplayName("아직 모든 칸이 채워지지 않은 구역은 미완성이다")
    void regionCompleted_falseForIncompleteRegion() {
        // 2,3,7 세 칸은 모두 CASTLE 타입 구역이지만, 아직 어느 칸에도 타일이 놓이지 않았으므로
        // 이 구역은 미완성 상태여야 한다.
        boolean completed = boardWithCastle19.isRegionCompleted(2);
        int sizeIfCompleted = boardWithCastle19.completedRegionSizeIfAny(2);

        assertThat(completed).isFalse();
        assertThat(sizeIfCompleted).isEqualTo(0);
    }
}