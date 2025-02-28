package org.sopt.app.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.sopt.app.common.fixtures.SoptampUserFixture.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.app.application.soptamp.SoptampPointInfo.Main;
import org.sopt.app.application.soptamp.SoptampPointInfo.PartRank;
import org.sopt.app.application.soptamp.SoptampUserFinder;
import org.sopt.app.domain.enums.Part;

@ExtendWith(MockitoExtension.class)
class RankFacadeTest {

    @Mock
    private SoptampUserFinder soptampUserFinder;

    @InjectMocks
    private RankFacade rankFacade;

    @Test
    @DisplayName("SUCCESS_현재 기수의 솝탬프 유저 랭킹 조회")
    void SUCCESS_findCurrentRanks() {
        //given
        given(soptampUserFinder.findAllOfCurrentGenerationOrderByTotalPoints()).willReturn(SOPTAMP_USER_INFO_LIST);

        // when
        List<Main> result = rankFacade.findCurrentRanks();
        List<Main> expected = List.of(
                Main.builder().rank(1)
                        .nickname(SOPTAMP_USER_6.getNickname())
                        .point(SOPTAMP_USER_6.getTotalPoints()).build(),
                Main.builder().rank(2)
                        .nickname(SOPTAMP_USER_5.getNickname())
                        .point(SOPTAMP_USER_5.getTotalPoints()).build(),
                Main.builder().rank(5)
                        .nickname(SOPTAMP_USER_4.getNickname())
                        .point(SOPTAMP_USER_4.getTotalPoints()).build(),
                Main.builder().rank(5)
                        .nickname(SOPTAMP_USER_3.getNickname())
                        .point(SOPTAMP_USER_3.getTotalPoints()).build(),
                Main.builder().rank(4)
                        .nickname(SOPTAMP_USER_2.getNickname())
                        .point(SOPTAMP_USER_2.getTotalPoints()).build(),
                Main.builder().rank(3)
                        .nickname(SOPTAMP_USER_1.getNickname())
                        .point(SOPTAMP_USER_1.getTotalPoints()).build()
        );

        // then
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getNickname(), result.get(i).getNickname(), i + "번째 index");
        }
    }

    @Test
    @DisplayName("SUCCESS 파트별 현재 기수의 솝탬프 유저 랭킹 조회")
    void findCurrentRanksByPart() {
        // given
        given(soptampUserFinder.findAllByPartAndCurrentGenerationOrderByTotalPoints(Part.SERVER)).willReturn(SERVER_PART_SOPTAMP_USER_INFO_LIST);
        // when
        List<Main> result = rankFacade.findCurrentRanksByPart(Part.SERVER);
        List<Main> expected = List.of(
                Main.builder().rank(1)
                        .nickname(SOPTAMP_USER_6.getNickname())
                        .point(SOPTAMP_USER_6.getTotalPoints()).build(),
                Main.builder().rank(2)
                        .nickname(SOPTAMP_USER_5.getNickname())
                        .point(SOPTAMP_USER_5.getTotalPoints()).build(),
                Main.builder().rank(3)
                        .nickname(SOPTAMP_USER_1.getNickname())
                        .point(SOPTAMP_USER_1.getTotalPoints()).build()
        );
        // then
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getNickname(), result.get(i).getNickname(), i + "번째 index");
        }
    }

    @Test
    @DisplayName("SUCCESS 파트끼리의 솝탬프 포인트 랭킹 조회")
    void findAllPartRanks() {
        // given
        given(soptampUserFinder.findAllOfCurrentGeneration()).willReturn(SOPTAMP_USER_INFO_LIST);
        // when
        List<PartRank> result = rankFacade.findAllPartRanks();
        List<PartRank> expected = List.of( // 기-디-웹-아-안-서 순서
                PartRank.builder().rank(5) // 동점이라면 rank도 같아야 함
                        .part(Part.PLAN.getPartName())
                        .points(0L).build(),
                PartRank.builder().rank(2)
                        .part(Part.DESIGN.getPartName())
                        .points(SOPTAMP_USER_4.getTotalPoints()).build(),
                PartRank.builder().rank(5)
                        .part(Part.WEB.getPartName())
                        .points(0L).build(),
                PartRank.builder().rank(2)
                        .part(Part.IOS.getPartName())
                        .points(SOPTAMP_USER_3.getTotalPoints()).build(),
                PartRank.builder().rank(4) // 2등, 2등 다음 rank는 3등이 아닌 4등이다.
                        .part(Part.ANDROID.getPartName())
                        .points(SOPTAMP_USER_2.getTotalPoints()).build(),
                PartRank.builder().rank(1)
                        .part(Part.SERVER.getPartName())
                        .points(SOPTAMP_USER_6.getTotalPoints() +
                                SOPTAMP_USER_5.getTotalPoints() +
                                SOPTAMP_USER_1.getTotalPoints()).build()
        );

        // then
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getPart(), result.get(i).getPart(), i + "번째 index");
            assertEquals(expected.get(i).getRank(), result.get(i).getRank(), i + "번째 index");
            assertEquals(expected.get(i).getPoints(), result.get(i).getPoints(), i + "번째 index");
        }
    }
}