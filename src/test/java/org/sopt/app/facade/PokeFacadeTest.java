package org.sopt.app.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.app.application.playground.dto.PlaygroundProfileInfo.*;
import org.sopt.app.application.playground.PlaygroundAuthService;
import org.sopt.app.application.friend.FriendService;
import org.sopt.app.application.playground.user_finder.PlaygroundUserIdsProvider;
import org.sopt.app.application.poke.*;
import org.sopt.app.application.poke.PokeInfo.*;
import org.sopt.app.application.user.UserInfo.UserProfile;
import org.sopt.app.application.user.UserService;
import org.sopt.app.common.fixtures.*;
import org.sopt.app.domain.entity.*;
import org.sopt.app.domain.entity.Friend;
import org.sopt.app.domain.entity.poke.*;
import org.sopt.app.domain.entity.poke.PokeMessage;
import org.sopt.app.domain.enums.*;
import org.sopt.app.presentation.poke.PokeResponse;
import org.sopt.app.presentation.poke.PokeResponse.*;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class PokeFacadeTest {

    private static final String MESSAGES_HEADER_FOR_POKE = "함께 보낼 메시지를 선택해주세요";
    private final Relationship relationship1 = Relationship.builder().pokeNum(1).build();
    private final Relationship relationship2 = Relationship.builder().pokeNum(3).build();
    private final User user = User.builder().id(1L).playgroundToken("token").build();
    private final UserProfile userProfile2 = UserProfile.builder().userId(2L).name("name2").playgroundId(2L).build();
    private final UserProfile userProfile3 = UserProfile.builder().userId(3L).name("name3").playgroundId(3L).build();
    private final List<UserProfile> userProfileList = List.of(userProfile2, userProfile3);
    private final ActivityCardinalInfo activityCardinalInfo = new ActivityCardinalInfo("34,서버");
    private final List<PlaygroundProfile> playgroundProfileList = List.of(
            new PlaygroundProfile(2L, "name2", "image", List.of(activityCardinalInfo)),
            new PlaygroundProfile(3L, "name3", "image", List.of(activityCardinalInfo))
    );
    private final List<PlaygroundProfile> playgroundProfileListWithoutImage = List.of(
            new PlaygroundProfile(2L, "name2", "", List.of(activityCardinalInfo)),
            new PlaygroundProfile(3L, "name3", "", List.of(activityCardinalInfo))
    );
    private final PokeHistory pokeHistory2 = PokeHistory.builder().id(2L).pokedId(1L).pokerId(2L).isReply(false)
            .isAnonymous(false).build();
    private final PokeHistoryInfo pokeHistoryInfo2 = PokeHistoryInfo.builder().id(2L).pokedId(1L).pokerId(2L)
            .isReply(false)
            .isAnonymous(false).build();
    private final PokeHistoryInfo pokeHistoryInfo2PokedIsNotReply = PokeHistoryInfo.builder().id(3L).pokedId(2L)
            .pokerId(1L)
            .isReply(false).isAnonymous(false).build();
    private final PokeDetail pokeDetail2 = PokeDetail.builder().id(2L).pokedId(1L).pokerId(2L).message("message")
            .build();
    private final Friend friend2 = Friend.builder().id(2L).userId(1L).friendUserId(2L).pokeCount(1).anonymousName("")
            .build();
    private final PokeMessage fixedMessage = PokeMessage.builder().id(0L).content("콕").type(PokeMessageType.POKE_ALL)
            .build();

    @Mock
    private PokeMessageService pokeMessageService;
    @Mock
    private PlaygroundAuthService playgroundAuthService;
    @Mock
    private UserService userService;
    @Mock
    private FriendService friendService;
    @Mock
    private PokeHistoryService pokeHistoryService;
    @Mock
    private PokeService pokeService;
    @Mock
    private PlaygroundUserIdsProvider playgroundUserIdsProvider;
    @InjectMocks
    private PokeFacade pokeFacade;

    @ParameterizedTest
    @ValueSource(strings = {"pokeSomeone", "pokeFriend", "replyNew"})
    @DisplayName("SUCCESS_찌르기 메세지 조회")
    void SUCCESS_getPokingMessage(String type) {
        PokeMessage pokeMessage = PokeMessage.builder().id(1L).content("content").build();
        ArrayList<PokeMessage> pokeMessageList = new ArrayList<>();
        pokeMessageList.add(pokeMessage);
        PokeResponse.PokeMessage pokeMessageResponse = new PokeResponse.PokeMessage(1L, "content");
        PokeResponse.PokeMessage fixedMessageResponse = new PokeResponse.PokeMessage(
                fixedMessage.getId(), fixedMessage.getContent());
        List<PokeResponse.PokeMessage> pokeMessageListResponse = List.of(pokeMessageResponse, fixedMessageResponse);

        when(pokeMessageService.pickRandomMessageByTypeOf(any())).thenReturn(pokeMessageList);
        when(pokeMessageService.getFixedMessage()).thenReturn(fixedMessage);

        List<PokeResponse.PokeMessage> result = pokeFacade.getPokingMessages(type);
        assertEquals(pokeMessageListResponse.size(), result.size());
        assertEquals(pokeMessageListResponse.get(0).getMessageId(), result.get(0).getMessageId());
        assertEquals(pokeMessageListResponse.get(0).getContent(), result.get(0).getContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"pokeSomeone", "pokeFriend", "replyNew"})
    @DisplayName("SUCCESS_찌르기 메세지 헤더 조회")
    void SUCCESS_getPokingMessageHeader(String type) {
        when(pokeMessageService.getMessagesHeaderComment(any())).thenReturn(MESSAGES_HEADER_FOR_POKE);

        String pokingMessageHeader = pokeFacade.getPokingMessageHeader(type);
        assertEquals(MESSAGES_HEADER_FOR_POKE, pokingMessageHeader);
    }

    @Test
    @DisplayName("SUCCESS_단일 누가 나를 찔렀어요 답장 X 조회")
    void SUCCESS_getMostRecentPokeMeHistoryIsNotReply() {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(
                2L, 2L, "image", "name2",
                "message", 34L, "서버", 1, null,
                "name3의 친구", true, false, false, "");

        when(pokeHistoryService.getPokeMeUserIds(1L)).thenReturn(List.of(2L));
        when(pokeHistoryService.getAllLatestPokeHistoryFromTo(2L, 1L)).thenReturn(List.of(pokeHistory2));
        when(pokeService.getPokeDetail(pokeHistory2.getId())).thenReturn(pokeDetail2);
        when(playgroundAuthService.getPlaygroundMemberProfiles(
                "token", List.of(2L))).thenReturn(playgroundProfileList);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of(3L));
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(userService.isUserExist(anyLong())).thenReturn(true);
        when(userService.getNamesByIds(List.of(3L))).thenReturn(List.of("name3"));
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship1);
        when(userService.getUserProfilesByPlaygroundIds(anyList())).thenReturn(userProfileList);

        SimplePokeProfile result = pokeFacade.getMostRecentPokeMeHistory(user);
        assertEquals(simplePokeProfile, result);
    }

    @Test
    @DisplayName("SUCCESS_단일 누가 나를 찔렀어요 답장 O 조회")
    void SUCCESS_getMostRecentPokeMeHistoryIsReply() {
        when(pokeHistoryService.getPokeMeUserIds(1L)).thenReturn(List.of(3L));

        SimplePokeProfile result = pokeFacade.getMostRecentPokeMeHistory(user);
        assertNull(result);
    }

    @Test
    @DisplayName("SUCCESS_리스트 누가 나를 찔렀어요 조회")
    void SUCCESS_getAllPokeMeHistory() {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(
                2L, 2L, "", "name2", "message", 34L,
                "서버", 3, null, "name3 외 1명과 친구",
                false, false, false, "");

        PokeToMeHistoryList pokeToMeHistoryList = PokeToMeHistoryList.builder()
                .history(List.of(simplePokeProfile))
                .totalPageSize(1)
                .pageSize(1)
                .pageNum(0)
                .build();
        Page<PokeHistory> pokeHistoryPage = new PageImpl<>(List.of(pokeHistory2));
        Pageable pageable = Pageable.ofSize(1);

        when(pokeHistoryService.getPokeMeUserIds(1L)).thenReturn(List.of(2L));
        when(pokeHistoryService.getAllLatestPokeHistoryFromTo(2L, 1L)).thenReturn(List.of(pokeHistory2));
        when(pokeHistoryService.getAllLatestPokeHistoryIn(List.of(2L), pageable)).thenReturn(pokeHistoryPage);

        when(pokeService.getPokeDetail(pokeHistory2.getId())).thenReturn(pokeDetail2);
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(
                playgroundProfileListWithoutImage);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of(3L, 4L));
        when(userService.isUserExist(anyLong())).thenReturn(true);
        when(userService.getNamesByIds(List.of(3L,4L))).thenReturn(List.of("name3", "name4"));
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship2);
        when(userService.getUserProfilesByPlaygroundIds(anyList())).thenReturn(userProfileList);

        PokeToMeHistoryList result = pokeFacade.getAllPokeMeHistory(user, pageable);
        assertEquals(pokeToMeHistoryList.getHistory().get(0).getUserId(), result.getHistory().get(0).getUserId());
        assertEquals(pokeToMeHistoryList.getTotalPageSize(), result.getTotalPageSize());
        assertEquals(pokeToMeHistoryList.getPageSize(), result.getPageSize());
        assertEquals(pokeToMeHistoryList.getPageNum(), result.getPageNum());
    }

    @Test
    @DisplayName("SUCCESS_친구 찌르기, 친구일 때")
    void SUCCESS_pokeFriendFriendEachOther() {
        when(pokeService.poke(1L, 2L, "message", false)).thenReturn(pokeHistory2);
        when(friendService.isFriendEachOther(1L, 2L)).thenReturn(true);

        Long result = pokeFacade.pokeFriend(1L, 2L, "message", false);
        assertEquals(2L, result);
    }

    @Test
    @DisplayName("SUCCESS_친구 찌르기, 친구 아닐 때, 찌른 적 없을 때")
    void SUCCESS_pokeFriendNotPokeBefore() {
        when(pokeService.poke(1L, 2L, "message", false)).thenReturn(pokeHistory2);
        when(friendService.isFriendEachOther(1L, 2L)).thenReturn(false);
        when(pokeHistoryService.getAllOfPokeBetween(1L, 2L)).thenReturn(List.of());

        Long result = pokeFacade.pokeFriend(1L, 2L, "message", false);
        assertEquals(2L, result);
    }

    @Test
    @DisplayName("SUCCESS_친구 찌르기, 친구 아닐 때, 찌른 적 있을 때")
    void SUCCESS_pokeFriendPokeBefore() {
        when(pokeService.poke(1L, 2L, "message", false)).thenReturn(pokeHistory2);
        when(friendService.isFriendEachOther(1L, 2L)).thenReturn(false);
        when(pokeHistoryService.getAllOfPokeBetween(1L, 2L)).thenReturn(List.of(pokeHistoryInfo2));

        Long result = pokeFacade.pokeFriend(1L, 2L, "message", false);
        assertEquals(2L, result);
    }

    @Test
    @DisplayName("SUCCESS_친구 가져오기, 이미지 있을 때, 친구 없을 때")
    void SUCCESS_getFriendNoMutualFriend() {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(2L, 2L, "image", "name2",
                "", 34L, "서버", 1, null, "새로운 친구", false, false, false, "");

        when(friendService.getPokeFriendIdRandomly(1L)).thenReturn(2L);
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(playgroundProfileList);
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship1);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of());

        List<SimplePokeProfile> result = pokeFacade.getFriend(user);
        assertEquals(List.of(simplePokeProfile), result);
    }

    @Test
    @DisplayName("SUCCESS_친구 가져오기, 이미지 있을 때, 친구 1명일 때")
    void SUCCESS_getFriendWithImage() {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(2L, 2L, "image", "name2",
                "", 34L, "서버", 1, null, "name3의 친구", false, false, false, "");

        when(friendService.getPokeFriendIdRandomly(1L)).thenReturn(2L);
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(playgroundProfileList);
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship1);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of(3L));
        when(userService.getNamesByIds(List.of(3L))).thenReturn(List.of("name3"));

        List<SimplePokeProfile> result = pokeFacade.getFriend(user);
        assertEquals(List.of(simplePokeProfile), result);
    }

    @Test
    @DisplayName("SUCCESS_친구 가져오기, 이미지 없을 때, 친구 1명일 때")
    void SUCCESS_getFriendWithoutImage() {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(2L, 2L, "", "name2",
                "", 34L, "서버", 1, null, "name3의 친구", false, false, false, "");

        when(friendService.getPokeFriendIdRandomly(1L)).thenReturn(2L);
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(
                playgroundProfileListWithoutImage);
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship1);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of(3L));
        when(userService.getNamesByIds(List.of(3L))).thenReturn(List.of("name3"));

        List<SimplePokeProfile> result = pokeFacade.getFriend(user);
        assertEquals(List.of(simplePokeProfile), result);
    }

    @Test
    @DisplayName("SUCCESS_친구 가져오기, 이미지 없을 때, 친구 2명 이상일 때")
    void SUCCESS_getFriendWithoutImageSomeFriends() {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(2L, 2L, "", "name2",
                "", 34L, "서버", 1, null, "name3 외 1명과 친구", false, false, false, "");

        when(friendService.getPokeFriendIdRandomly(1L)).thenReturn(2L);
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(
                playgroundProfileListWithoutImage);
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship1);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of(3L, 4L));
        when(userService.getNamesByIds(List.of(3L,4L))).thenReturn(List.of("name3", "name4"));

        List<SimplePokeProfile> result = pokeFacade.getFriend(user);
        assertEquals(List.of(simplePokeProfile), result);
    }

    @ParameterizedTest
    @EnumSource(value = Friendship.class)
    @DisplayName("SUCCESS_친구 관계로 두 친구 가져오기, 친구 아닐 때")
    void SUCCESS_getTwoFriendsByFriendshipNonFriend(Friendship friendship) {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(2L, 2L, "image", "name2",
                "message", 34L, "서버", 3, null, "name3의 친구", false, false, false, "");

        when(friendService.findAllFriendsByFriendship(any(), any(), any())).thenReturn(List.of(friend2));
        when(pokeHistoryService.getAllOfPokeBetween(1L, 2L)).thenReturn(List.of(pokeHistoryInfo2));
        when(pokeService.getPokeDetail(pokeHistory2.getId())).thenReturn(pokeDetail2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(playgroundProfileList);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of(3L));
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(userService.getNamesByIds(List.of(3L))).thenReturn(List.of("name3"));
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship2);
        when(userService.getUserProfilesByPlaygroundIds(anyList())).thenReturn(userProfileList);
        when(pokeHistoryService.getAllPokeHistoryByUsers(anyLong(), anyLong())).thenReturn(List.of(pokeHistoryInfo2));

        List<SimplePokeProfile> result = pokeFacade.getTwoFriendByFriendship(user, friendship);
        assertEquals(List.of(simplePokeProfile), result);
    }

    @ParameterizedTest
    @EnumSource(value = Friendship.class)
    @DisplayName("SUCCESS_친구 관계로 친구 사이즈 가져오기")
    void SUCCESS_getFriendSizeByFriendship(Friendship friendship) {
        when(friendService.findAllFriendsByFriendship(1L, friendship.getLowerLimit(),
                friendship.getUpperLimit())).thenReturn(List.of(friend2));

        int result = pokeFacade.getFriendSizeByFriendship(user.getId(), friendship);
        assertEquals(1, result);
    }

    @ParameterizedTest
    @EnumSource(value = Friendship.class)
    @DisplayName("SUCCESS_친구 관계로 모든 친구 가져오기")
    void SUCCESS_getAllFriendByFriendship(Friendship friendship) {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(2L, 2L, "image", "name2",
                "message", 34L, "서버", 3, null, "name3의 친구", false, false, false, "");
        Pageable pageable = Pageable.ofSize(1);
        Page<Friend> friendPage = new PageImpl<>(List.of(friend2));
        EachRelationFriendList eachRelationFriendList = EachRelationFriendList.builder()
                .friendList(List.of(simplePokeProfile))
                .pageNum(0)
                .pageSize(1)
                .totalPageSize(0)
                .totalSize(0)
                .build();

        when(friendService.findAllFriendsByFriendship(anyLong(), anyInt(), anyInt(), any())).thenReturn(friendPage);
        when(pokeService.getPokeDetail(pokeHistory2.getId())).thenReturn(pokeDetail2);
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(playgroundProfileList);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of(3L));
        when(userService.getNamesByIds(List.of(3L))).thenReturn(List.of("name3"));
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship2);
        when(pokeHistoryService.getAllOfPokeBetween(any(), any())).thenReturn(
                List.of(pokeHistoryInfo2, pokeHistoryInfo2PokedIsNotReply));
        when(userService.getUserProfilesByPlaygroundIds(anyList())).thenReturn(userProfileList);
        when(userService.isUserExist(anyLong())).thenReturn(true);

        EachRelationFriendList result = pokeFacade.getAllFriendByFriendship(user, friendship, pageable);
        assertEquals(eachRelationFriendList.getFriendList().get(0).getUserId(),
                result.getFriendList().get(0).getUserId());
        assertEquals(eachRelationFriendList.getPageSize(), result.getPageSize());
        assertEquals(eachRelationFriendList.getPageNum(), result.getPageNum());
    }

    @Test
    @DisplayName("SUCCESS_콕찌르기 히스토리 프로필 조회, 답장 없을 때, 친구 1명일 때")
    void SUCCESS_getPokeHistoryProfileReplyAMutualFriend() {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(2L, 2L, "image", "name2", "message",
                34L, "서버", 3, null, "name3의 친구", false, false, false, "");

        when(pokeService.getPokeDetail(pokeHistory2.getId())).thenReturn(pokeDetail2);
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(playgroundProfileList);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of(3L));
        when(userService.getNamesByIds(List.of(3L))).thenReturn(List.of("name3"));
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship2);
        when(userService.getUserProfilesByPlaygroundIds(anyList())).thenReturn(userProfileList);

        SimplePokeProfile result = pokeFacade.getPokeHistoryProfile(user, 2L, 2L);
        assertEquals(simplePokeProfile, result);
    }

    @Test
    @DisplayName("SUCCESS_콕찌르기 히스토리 프로필 조회, 답장 있을 때, 친구 2명 이상일 때")
    void SUCCESS_getPokeHistoryProfileNoReplySomeMutualFriends() {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(2L, 2L, "", "name2",
                "message", 34L, "서버", 3, null, "name3 외 1명과 친구", false, false, false, "");

        when(pokeService.getPokeDetail(pokeHistory2.getId())).thenReturn(pokeDetail2);
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(
                playgroundProfileListWithoutImage);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of(3L, 4L));
        when(userService.getNamesByIds(List.of(3L,4L))).thenReturn(List.of("name3", "name4"));
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship2);
        when(userService.getUserProfilesByPlaygroundIds(anyList())).thenReturn(userProfileList);

        SimplePokeProfile result = pokeFacade.getPokeHistoryProfile(user, 2L, 2L);
        assertEquals(simplePokeProfile, result);
    }

    @Test
    @DisplayName("SUCCESS_콕찌르기 히스토리 프로필 조회, 답장 없을 때, 친구 없을 때")
    void SUCCESS_getPokeHistoryProfileNoReplyNoMutualFriend() {
        SimplePokeProfile simplePokeProfile = SimplePokeProfile.of(2L, 2L, "", "name2", "message",
                34L, "서버", 3, null, "새로운 친구", false, false, false, "");

        when(pokeService.getPokeDetail(pokeHistory2.getId())).thenReturn(pokeDetail2);
        when(userService.getUserProfileOrElseThrow(2L)).thenReturn(userProfile2);
        when(playgroundAuthService.getPlaygroundMemberProfiles("token", List.of(2L))).thenReturn(
                playgroundProfileListWithoutImage);
        when(friendService.getMutualFriendIds(1L, 2L)).thenReturn(List.of());
        when(friendService.getRelationInfo(1L, 2L)).thenReturn(relationship2);
        when(userService.getUserProfilesByPlaygroundIds(anyList())).thenReturn(userProfileList);

        SimplePokeProfile result = pokeFacade.getPokeHistoryProfile(user, 2L, 2L);
        assertEquals(simplePokeProfile, result);
    }

    @Test
    @DisplayName("SUCCESS_신규 유저인지 조회")
    void SUCCESS_getIsNewUser() {
        when(friendService.getIsNewUser(1L)).thenReturn(true);

        boolean result = pokeFacade.getIsNewUser(user.getId());
        assertTrue(result);
    }

    @Test
    @DisplayName("SUCCESS_모든 유형의 추천 친구 조회")
    void SUCCESS_getRecommendedFriendsByAllType() {
        // given
        final Set<Long> sameMbtiPlaygroundIds = Set.of(1L);
        final List<Long> sameMbtiUserIds = List.of(11L);
        final Set<Long> sameUniversityPlaygroundIds = Set.of(2L);
        final List<Long> sameUniversityUserIds = List.of(22L);
        final Set<Long> sameGenerationPlaygroundIds = Set.of(3L);
        final List<Long> sameGenerationUserIds = List.of(33L);

        given(playgroundAuthService.getOwnPlaygroundProfile(anyString()))
                .willReturn(PokeFixture.createOwnPlaygroundProfile());
        given(friendService.findAllFriendIdsByUserId(anyLong())).willReturn(Set.of()); // 현재 친구인 유저 없음

        //== MBTI, UNIVERSITY, GENERATION별 playgroundIds를 찾는 로직 ==//
        given(playgroundUserIdsProvider.findPlaygroundIdsByType(any(), eq(FriendRecommendType.MBTI)))
                .willReturn(sameMbtiPlaygroundIds);
        given(playgroundUserIdsProvider.findPlaygroundIdsByType(any(), eq(FriendRecommendType.UNIVERSITY)))
                .willReturn(sameUniversityPlaygroundIds);
        given(playgroundUserIdsProvider.findPlaygroundIdsByType(any(), eq(FriendRecommendType.GENERATION)))
                .willReturn(sameGenerationPlaygroundIds);

        //== MBTI, UNIVERSITY, GENERATION별 playground profile을 찾는 로직 ==//
        given(playgroundAuthService.getPlaygroundMemberProfiles(anyString(), eq(List.copyOf(sameMbtiPlaygroundIds))))
                .willReturn(PokeFixture.createPlaygroundProfileList(List.copyOf(sameMbtiPlaygroundIds)));
        given(playgroundAuthService.getPlaygroundMemberProfiles(anyString(), eq(List.copyOf(sameUniversityPlaygroundIds))))
                .willReturn(PokeFixture.createPlaygroundProfileList(List.copyOf(sameUniversityPlaygroundIds)));
        given(playgroundAuthService.getPlaygroundMemberProfiles(anyString(), eq(List.copyOf(sameGenerationPlaygroundIds))))
                .willReturn(PokeFixture.createPlaygroundProfileList(List.copyOf(sameGenerationPlaygroundIds)));

        //== MBTI, UNIVERSITY, GENERATION별 user profile을 찾는 로직 ==//
        given(userService.getUserProfilesByPlaygroundIds(List.copyOf(sameMbtiPlaygroundIds)))
                .willReturn(PokeFixture.createUserProfileList(sameMbtiUserIds ,List.copyOf(sameMbtiPlaygroundIds)));
        given(userService.getUserProfilesByPlaygroundIds(List.copyOf(sameUniversityPlaygroundIds)))
                .willReturn(PokeFixture.createUserProfileList(sameUniversityUserIds ,List.copyOf(sameUniversityPlaygroundIds)));
        given(userService.getUserProfilesByPlaygroundIds(List.copyOf(sameGenerationPlaygroundIds)))
                .willReturn(PokeFixture.createUserProfileList(sameGenerationUserIds ,List.copyOf(sameGenerationPlaygroundIds)));

        // when
        RecommendedFriendsRequest result = pokeFacade.getRecommendedFriendsByTypeList(
                List.of(FriendRecommendType.ALL), 6, UserFixture.createMyAppUser());
        List<Long> resultSameMbtiPlaygroundIds = result.getRandomInfoList().stream()
                .filter(info -> info.getRandomType() == FriendRecommendType.MBTI)
                .findFirst().get().getUserInfoList().stream().map(SimplePokeProfile::getPlaygroundId).toList();
        List<Long> resultSameUniversityPlaygroundIds = result.getRandomInfoList().stream()
                .filter(info -> info.getRandomType() == FriendRecommendType.UNIVERSITY)
                .findFirst().get().getUserInfoList().stream().map(SimplePokeProfile::getPlaygroundId).toList();
        List<Long> resultSameGenerationPlaygroundIds = result.getRandomInfoList().stream()
                .filter(info -> info.getRandomType() == FriendRecommendType.GENERATION)
                .findFirst().get().getUserInfoList().stream().map(SimplePokeProfile::getPlaygroundId).toList();

        // then
        assertEquals(3, result.getRandomInfoList().size());
        assertEquals(List.copyOf(sameMbtiPlaygroundIds), resultSameMbtiPlaygroundIds);
        assertEquals(List.copyOf(sameUniversityPlaygroundIds), resultSameUniversityPlaygroundIds);
        assertEquals(List.copyOf(sameGenerationPlaygroundIds), resultSameGenerationPlaygroundIds);
    }

    @Test
    @DisplayName("SUCCESS_요구사항1_추천할 친구가 없다면 객체를 반환하지 않음")
    void SUCCESS_getRecommendedFriendsByAllType_Requirement1() {
        // given
        final List<Long> emptyPlaygroundIds = List.of();
        final List<Long> emptyUserIds = List.of();

        given(playgroundAuthService.getOwnPlaygroundProfile(anyString()))
                .willReturn(PokeFixture.createOwnPlaygroundProfile());
        given(friendService.findAllFriendIdsByUserId(anyLong())).willReturn(Set.of()); // 현재 친구인 유저 없음
        given(userService.getAllPlaygroundIds()).willReturn(emptyPlaygroundIds); // 추천된 playground id 없음
        given(userService.getUserProfilesByPlaygroundIds(emptyPlaygroundIds))
                .willReturn(PokeFixture.createUserProfileList(emptyUserIds ,List.copyOf(emptyPlaygroundIds)));

        // when
        RecommendedFriendsRequest result = pokeFacade.getRecommendedFriendsByTypeList(
                List.of(FriendRecommendType.ALL_USER), 6, UserFixture.createMyAppUser());

        // then
        assertTrue(result.getRandomInfoList().isEmpty());
    }

    @Test
    @DisplayName("SUCCESS_요구사항2_자기 자신은 추천 친구에서 제외되어야 함")
    void SUCCESS_getRecommendedFriendsByAllType_Requirement2() {
        // given
        final List<Long> playgroundIds = List.of(UserFixture.myPlaygroundId);
        final List<Long> userIds = List.of(UserFixture.myAppUserId);

        given(playgroundAuthService.getOwnPlaygroundProfile(anyString()))
                .willReturn(PokeFixture.createOwnPlaygroundProfile());
        given(friendService.findAllFriendIdsByUserId(anyLong())).willReturn(Set.of()); // 현재 친구인 유저 없음

        //== playgroundIds에 자신의 playgroundId만 포함됨 ==//
        given(userService.getAllPlaygroundIds()).willReturn(playgroundIds);
        given(userService.getUserProfilesByPlaygroundIds(playgroundIds)).willReturn(
                PokeFixture.createUserProfileList(userIds, playgroundIds));

        // when
        RecommendedFriendsRequest result = pokeFacade.getRecommendedFriendsByTypeList(
                List.of(FriendRecommendType.ALL_USER), 6, UserFixture.createMyAppUser());

        // then
        assertTrue(result.getRandomInfoList().isEmpty());
    }

    @Test
    @DisplayName("SUCCESS_요구사항3_플그 아이디는 있지만 앱 아이디가 없는 유저는 추천하지 않도록 필터링")
    void SUCCESS_getRecommendedFriendsByAllType_Requirement3() {
        // given
        final List<Long> playgroundIds = List.of(1L);

        given(playgroundAuthService.getOwnPlaygroundProfile(anyString()))
                .willReturn(PokeFixture.createOwnPlaygroundProfile());
        given(friendService.findAllFriendIdsByUserId(anyLong())).willReturn(Set.of()); // 현재 친구인 유저 없음
        given(userService.getAllPlaygroundIds()).willReturn(playgroundIds); // playgroundId 존재
        given(userService.getUserProfilesByPlaygroundIds(playgroundIds)).willReturn(List.of());// 앱 아이디가 없어 userProfile을 반환하지 않음

        // when
        RecommendedFriendsRequest result = pokeFacade.getRecommendedFriendsByTypeList(
                List.of(FriendRecommendType.ALL_USER), 6, UserFixture.createMyAppUser());

        // then
        assertTrue(result.getRandomInfoList().isEmpty());
    }

    /**
    @Test
    @DisplayName("SUCCESS_요구사항4_이미 친구인 유저는 추천 친구에서 제외함 ")
    void SUCCESS_getRecommendedFriendsByAllType_Requirement4() {
        // given
        given(playgroundAuthService.getOwnPlaygroundProfile(anyString()))
                .willReturn(PokeFixture.createOwnPlaygroundProfile());
        given(playgroundAuthService.getPlaygroundIdsForSameGeneration(List.of(GENERATION)))
                .willReturn(List.of(1L));
        given(playgroundAuthService.getPlaygroundIdsForSameMbti(GENERATION, MBTI))
                .willReturn(List.of(2L));
        given(playgroundAuthService.getPlaygroundIdsForSameUniversity(GENERATION, UNIVERSITY))
                .willReturn(List.of(3L));
        given(friendService.findUserIdsLinkedFriends(anyLong()))
                .willReturn(new ArrayList<>(Arrays.asList(11L, 22L, 33L)));

        given(userService.getUserProfilesByPlaygroundIds(anyList())).willReturn(
                PokeFixture.createUserProfileList(
                        List.of(11L, 22L, 33L),
                        List.of(1L, 2L, 3L)
                ));
        User myAppUser = UserFixture.createMyAppUser();

        // when
        RecommendedFriendsRequest result =
                pokeFacade.getRecommendedFriendsByTypeList(List.of(FriendRecommendType.ALL), 6, myAppUser);


        // then
        assertTrue(result.getRandomInfoList().isEmpty());
    }

    @Test
    @DisplayName("SUCCESS_요구사항5_자신의 유형 값이 null이면 객체를 반환하지 않음")
    void SUCCESS_getRecommendedFriendsByAllType_Requirement5() {
        // given
        final List<Long> sameGenerationPlaygroundIds = List.of(1L);
        final List<Long> sameMbtiPlaygroundIds = List.of(2L);

        given(playgroundAuthService.getOwnPlaygroundProfile(anyString()))
                .willReturn(PokeFixture.createMbtiNullPlaygroundProfile());
        given(playgroundAuthService.getPlaygroundIdsForSameGeneration(List.of(GENERATION)))
                .willReturn(List.of(1L));
        given(playgroundAuthService.getPlaygroundIdsForSameUniversity(GENERATION, UNIVERSITY))
                .willReturn(List.of(2L));
        given(friendService.findUserIdsLinkedFriends(anyLong()))
                .willReturn(new ArrayList<>(Arrays.asList(33L, 66L, 99L)));

        given(userService.getUserProfilesByPlaygroundIds(sameGenerationPlaygroundIds)).willReturn(
                PokeFixture.createUserProfileList(List.of(11L), sameGenerationPlaygroundIds));
        given(userService.getUserProfilesByPlaygroundIds(sameMbtiPlaygroundIds)).willReturn(
                PokeFixture.createUserProfileList(List.of(22L), sameMbtiPlaygroundIds));


        given(playgroundAuthService.getPlaygroundMemberProfiles(dummyPlaygroundToken, sameGenerationPlaygroundIds))
                .willReturn(PokeFixture.createPlaygroundProfileList(sameGenerationPlaygroundIds));
        given(playgroundAuthService.getPlaygroundMemberProfiles(dummyPlaygroundToken, sameMbtiPlaygroundIds))
                .willReturn(PokeFixture.createPlaygroundProfileList(sameMbtiPlaygroundIds));

        User myAppUser = UserFixture.createMyAppUser();

        // when
        RecommendedFriendsRequest result = pokeFacade.getRecommendedFriendsByTypeList(List.of(FriendRecommendType.ALL), 6, myAppUser);

        // then
        List<FriendRecommendType> recommendedFriendTypes = result.getRandomInfoList().stream()
                .map(RecommendedFriendsByType::getRandomType)
                .toList();
        assertTrue(recommendedFriendTypes.containsAll(List.of(FriendRecommendType.GENERATION, FriendRecommendType.UNIVERSITY)));
    }

    private List<Long> findPlaygroundIdsInRecommendedFriendsByAllTypeByType(
            RecommendedFriendsRequest recommendedFriendsRequest, FriendRecommendType type) {
        return recommendedFriendsRequest.getRandomInfoList().stream()
                .filter(randomInfo -> randomInfo.getRandomType() == type)
                .flatMap(randomInfo -> randomInfo.getUserInfoList().stream())
                .map(SimplePokeProfile::getPlaygroundId)
                .toList();
    }
     **/
}
