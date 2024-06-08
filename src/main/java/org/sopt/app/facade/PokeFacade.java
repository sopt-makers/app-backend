package org.sopt.app.facade;

import static org.sopt.app.application.poke.PokeInfo.NEW_FRIEND_MANY_MUTUAL;
import static org.sopt.app.application.poke.PokeInfo.NEW_FRIEND_NO_MUTUAL;
import static org.sopt.app.application.poke.PokeInfo.NEW_FRIEND_ONE_MUTUAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.application.auth.PlaygroundAuthInfo.PlaygroundProfile;
import org.sopt.app.application.auth.PlaygroundAuthService;
import org.sopt.app.application.poke.FriendService;
import org.sopt.app.application.poke.PokeHistoryService;
import org.sopt.app.application.poke.PokeInfo;
import org.sopt.app.application.poke.PokeInfo.PokeHistoryInfo;
import org.sopt.app.application.poke.PokeMessageService;
import org.sopt.app.application.poke.PokeService;
import org.sopt.app.application.user.UserInfo;
import org.sopt.app.application.user.UserInfo.UserProfile;
import org.sopt.app.application.user.UserService;
import org.sopt.app.domain.entity.PokeHistory;
import org.sopt.app.domain.entity.User;
import org.sopt.app.domain.enums.Friendship;
import org.sopt.app.presentation.poke.PokeResponse;
import org.sopt.app.presentation.poke.PokeResponse.EachRelationFriendList;
import org.sopt.app.presentation.poke.PokeResponse.PokeToMeHistoryList;
import org.sopt.app.presentation.poke.PokeResponse.SimplePokeProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PokeFacade {

    private final PlaygroundAuthService playgroundAuthService;
    private final UserService userService;
    private final FriendService friendService;
    private final PokeService pokeService;
    private final PokeHistoryService pokeHistoryService;
    private final PokeMessageService pokeMessageService;

    @Transactional(readOnly = true)
    public List<PokeResponse.PokeMessage> getPokingMessages(String type) {
        val messages = pokeMessageService.pickRandomMessageByTypeOf(type);
        return messages.stream()
                .map(messagesDetail -> PokeResponse.PokeMessage.of(
                                messagesDetail.getId(), messagesDetail.getContent()
                        )
                )
                .toList();
    }

    public String getPokingMessageHeader(String type) {
        return pokeMessageService.getMessagesHeaderComment(type);
    }

    @Transactional(readOnly = true)
    public List<SimplePokeProfile> getRecommendUserForNew(String playgroundToken, Long userPlaygroundId,
            Long userId
    ) {
        val playgroundUserIds = playgroundAuthService.getPlayGroundUserIds(playgroundToken);
        // TODO : 친구가 아닌 유저 아이디로만 Shuffle 하도록
        val notFriendUserPlaygroundIds = userService.getUserProfilesByPlaygroundIds(playgroundUserIds.getUserIds())
                .stream()
                .filter(userProfile -> !userId.equals(userProfile.getUserId()) && !friendService.isFriendEachOther(
                        userId, userProfile.getUserId()))
                .map(UserProfile::getPlaygroundId)
                .collect(Collectors.toList());
        int RECOMMEND_USER_NUM_FOR_NEW = 6;
        val recommendUserIds = pickRandomUserIds(notFriendUserPlaygroundIds, userPlaygroundId,
                RECOMMEND_USER_NUM_FOR_NEW
        );
        val playgroundProfiles = playgroundAuthService.getPlaygroundMemberProfiles(playgroundToken, recommendUserIds);
        val userProfiles = userService.getUserProfilesByPlaygroundIds(recommendUserIds);
        val pokeHistories = pokeHistoryService.getAllPokeHistoryMap(userId);
        return makeRandomSimplePokeProfile(userProfiles, playgroundProfiles, pokeHistories, userId);
    }

    private List<SimplePokeProfile> makeRandomSimplePokeProfile(
            List<UserProfile> userProfiles,
            List<PlaygroundProfile> playgroundProfiles,
            HashMap<Long, Boolean> pokeHistories,
            Long userId
    ) {
        return userProfiles.stream().map(
                userProfile -> {
                    val isAlreadyPoke = Objects.nonNull(pokeHistories.get(userProfile.getUserId()));
                    val pokeCount = friendService.getRelationInfo(userId, userProfile.getUserId()).getPokeNum();
                    val playgroundProfile = playgroundProfiles.stream()
                            .filter(profile -> profile.getMemberId().equals(userProfile.getPlaygroundId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("플레이그라운드 프로필이 없습니다."));
                    val generation = playgroundProfile.getActivities().get(0).getGeneration();
                    val part = playgroundProfile.getActivities().get(0).getPart();

                    return SimplePokeProfile.of(
                            userProfile.getUserId(),
                            playgroundProfile.getMemberId(),
                            playgroundProfile.getProfileImage() == null ? "" : playgroundProfile.getProfileImage(),
                            playgroundProfile.getName(),
                            "",
                            Integer.parseInt(generation),
                            part,
                            pokeCount,
                            Friendship.NON_FRIEND.getFriendshipName(),
                            NEW_FRIEND_NO_MUTUAL,
                            true,
                            isAlreadyPoke,
                            false,
                            ""
                    );
                }
        ).toList();
    }

    private List<Long> pickRandomUserIds(
            List<Long> playgroundUserIds, Long userPlaygroundId, int limitNum
    ) {
        List<Long> copiedList = new ArrayList<>(playgroundUserIds);
        copiedList.remove(userPlaygroundId);
        Collections.shuffle(copiedList, new Random());
        return copiedList.stream().limit(limitNum).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PokeResponse.Friend> getRecommendFriendsOfUsersFriend(User user) {
        val randomFriendsUserIds = friendService.findAllFriendIdsByUserIdRandomly(user.getId(), 2);

        val hasPokeMeBeforeUserIds = pokeHistoryService.getPokeFriendIds(user.getId());
        val friendUserIds = friendService.findAllFriendIdsByUserId(user.getId());

        val excludedUserIds = new ArrayList<>(hasPokeMeBeforeUserIds);
        excludedUserIds.addAll(friendUserIds);
        excludedUserIds.add(user.getId());

        return randomFriendsUserIds.stream().map(
                friendsUserId -> {
                    val friendUser = userService.getUserProfile(friendsUserId);
                    val friendProfile = playgroundAuthService.getPlaygroundMemberProfiles(
                            user.getPlaygroundToken(), List.of(friendUser.getPlaygroundId())
                    ).get(0);

                    val randomFriendsIds = friendService.findAllFriendIdsByUserIdRandomlyExcludeUserId(friendsUserId,
                            excludedUserIds, 2);

                    if (randomFriendsIds.isEmpty()) {
                        return PokeResponse.Friend.of(
                                friendsUserId,
                                friendProfile.getMemberId(),
                                friendProfile.getName(),
                                friendProfile.getProfileImage() == null ? "" : friendProfile.getProfileImage(),
                                List.of()
                        );
                    }

                    val randomFriendsOfFriends = userService.getUserProfilesByUserIds(randomFriendsIds);

                    val pokeHistories = pokeHistoryService.getAllPokeHistoryMap(user.getId());
                    val playgroundProfiles = playgroundAuthService.getPlaygroundMemberProfiles(
                            user.getPlaygroundToken(),
                            randomFriendsOfFriends.stream().map(UserProfile::getPlaygroundId).toList());
                    val simpleProfiles = makeRandomSimplePokeProfile(
                            randomFriendsOfFriends,
                            playgroundProfiles,
                            pokeHistories,
                            user.getId()
                    );

                    return PokeResponse.Friend.of(
                            friendsUserId,
                            friendProfile.getMemberId(),
                            friendProfile.getName(),
                            friendProfile.getProfileImage() == null ? "" : friendProfile.getProfileImage(),
                            simpleProfiles
                    );
                }
        ).toList();
    }

    @Transactional(readOnly = true)
    public SimplePokeProfile getMostRecentPokeMeHistory(User user) {
        List<Long> pokeMeUserIds = pokeHistoryService.getPokeMeUserIds(user.getId());
        Optional<PokeHistory> mostRecentPokeMeHistory = pokeMeUserIds.stream()
                .map(pokeMeUserId ->
                        pokeHistoryService.getAllLatestPokeHistoryFromTo(pokeMeUserId, user.getId()).get(0)
                )
                .sorted(Comparator.comparing(PokeHistory::getCreatedAt).reversed())
                .filter(pokeHistory -> !pokeHistory.getIsReply())
                .findFirst();
        return mostRecentPokeMeHistory
                .map(pokeHistory -> getPokeHistoryProfile(
                        user, pokeHistory.getPokerId(), pokeHistory.getId()))
                .orElse(null);

    }

    @Transactional(readOnly = true)
    public PokeToMeHistoryList getAllPokeMeHistory(User user, Pageable pageable) {
        List<Long> pokeMeUserIds = pokeHistoryService.getPokeMeUserIds(user.getId());
        List<Long> latestHistoryIds = pokeMeUserIds.stream()
                .map(pokeMeUserId ->
                        pokeHistoryService.getAllLatestPokeHistoryFromTo(pokeMeUserId, user.getId())
                                .get(0).getId()
                )
                .toList();
        Page<PokeHistory> pokedHistories = pokeHistoryService.getAllLatestPokeHistoryIn(latestHistoryIds, pageable);
        val size = pokedHistories.getSize();
        val totalPageSize = size / pageable.getPageSize();
        List<SimplePokeProfile> pokeToMeHistories = pokedHistories.stream()
                .map(pokeHistory -> getPokeHistoryProfile(user, pokeHistory.getPokerId(), pokeHistory.getId()))
                .distinct()
                .toList();
        return PokeToMeHistoryList.of(
                pokeToMeHistories,
                totalPageSize,
                pageable.getPageSize(),
                pokedHistories.getNumber()
        );
    }


    @Transactional
    public Long pokeFriend(Long pokerUserId, Long pokedUserId, String pokeMessage, Boolean isAnonymous) {
        pokeHistoryService.checkDuplicate(pokerUserId, pokedUserId);
        PokeHistory newPoke = pokeService.poke(pokerUserId, pokedUserId, pokeMessage, isAnonymous);

        applyFriendship(pokerUserId, pokedUserId);
        return newPoke.getId();
    }

    private void applyFriendship(Long pokerUserId, Long pokedUserId) {
        boolean friendEachOther = friendService.isFriendEachOther(pokerUserId, pokedUserId);
        if (friendEachOther) {
            friendService.applyPokeCount(pokerUserId, pokedUserId);
            return;
        }
        boolean userNotPokeBefore = pokeHistoryService.getAllOfPokeBetween(pokerUserId, pokedUserId).isEmpty();
        if (!userNotPokeBefore) {
            friendService.registerFriendshipOf(pokerUserId, pokedUserId);
        }
    }


    @Transactional(readOnly = true)
    public List<SimplePokeProfile> getFriend(User user) {
        Long userId = user.getId();
        val friendId = friendService.getPokeFriendIdRandomly(userId);

        val friendUserProfile = userService.getUserProfile(friendId);
        val friendProfile = playgroundAuthService.getPlaygroundMemberProfiles(
                user.getPlaygroundToken(), List.of(friendUserProfile.getPlaygroundId())).get(0);
        val friendRelationInfo = friendService.getRelationInfo(userId, friendId);

        val pokeHistoryList = pokeHistoryService.getAllOfPokeBetween(userId, friendId);

        return List.of(
                SimplePokeProfile.of(
                        friendUserProfile.getUserId(),
                        friendProfile.getMemberId(),
                        friendProfile.getProfileImage() == null ? "" : friendProfile.getProfileImage(),
                        friendProfile.getName(),
                        "",
                        Integer.parseInt(friendProfile.getActivities().get(0).getGeneration()),
                        friendProfile.getActivities().get(0).getPart(),
                        friendRelationInfo.getPokeNum(),
                        friendRelationInfo.getRelationName(),
                        createMutualFriendNames(user.getId(), friendId),
                        false,
                        getIsAlreadyPoke(pokeHistoryList, userId),
                        getIsAnonymous(pokeHistoryList, userId),
                        friendRelationInfo.getAnonymousName()
                )
        );
    }

    private boolean getIsAlreadyPoke(List<PokeHistoryInfo> pokeHistoryList, Long userId) {
        return pokeHistoryList.stream()
                .filter(pokeHistory -> pokeHistory.getPokerId().equals(userId))
                .anyMatch(pokeHistory -> !pokeHistory.getIsReply());
    }

    private boolean getIsAnonymous(List<PokeHistoryInfo> pokeHistoryList, Long userId) {
        return pokeHistoryList.stream()
                .filter(pokeHistory -> pokeHistory.getPokedId().equals(userId))
                .findFirst().map(PokeHistoryInfo::getIsAnonymous).orElse(false);
    }

    private String createMutualFriendNames(Long userId, Long friendId) {
        List<String> mutualFriendNames = friendService.getMutualFriendIds(userId, friendId).stream()
                .map(id -> userService.getUserProfile(id).getName())
                .toList();

        if (mutualFriendNames.isEmpty()) {
            return NEW_FRIEND_NO_MUTUAL;
        }
        if (mutualFriendNames.size() == 1) {
            return String.format(NEW_FRIEND_ONE_MUTUAL, mutualFriendNames.get(0));
        } else {
            return String.format(NEW_FRIEND_MANY_MUTUAL, mutualFriendNames.get(0), mutualFriendNames.size() - 1);
        }
    }

    @Transactional(readOnly = true)
    public List<SimplePokeProfile> getTwoFriendByFriendship(User user, Friendship friendship) {
        val friendsOfFriendship = friendService.findAllFriendsByFriendship(
                user.getId(), friendship.getLowerLimit(), friendship.getUpperLimit());

        return friendsOfFriendship.stream()
                .map(friend -> {
                    List<PokeHistoryInfo> allOfPokeFromTo = pokeHistoryService.getAllOfPokeBetween(friend.getUserId(),
                            friend.getFriendUserId());
                    return allOfPokeFromTo.stream()
                            .map(poke -> getPokeHistoryProfile(user, friend.getFriendUserId(), poke.getId()))
                            .findFirst().get();
                })
                .limit(2)
                .toList();
    }

    @Transactional(readOnly = true)
    public int getFriendSizeByFriendship(Long userId, Friendship friendship) {
        return friendService.findAllFriendsByFriendship(
                userId, friendship.getLowerLimit(), friendship.getUpperLimit()).size();
    }

    @Transactional(readOnly = true)
    public EachRelationFriendList getAllFriendByFriendship(User user, Friendship friendship, Pageable pageable) {
        val friends = friendService.findAllFriendsByFriendship(
                user.getId(), friendship.getLowerLimit(), friendship.getUpperLimit(), pageable);
        List<SimplePokeProfile> allOfPokeWithFriends = friends.getContent().stream()
                .map(friend -> {
                    List<PokeHistoryInfo> allOfPokeFromTo = pokeHistoryService.getAllOfPokeBetween(
                            friend.getUserId(),
                            friend.getFriendUserId());
                    return allOfPokeFromTo.stream()
                            .map(poke -> getPokeHistoryProfile(user, friend.getFriendUserId(), poke.getId()))
                            .findFirst().get();
                }).toList();
        val totalSize = friendService.findAllFriendSizeByFriendship(
                user.getId(), friendship.getLowerLimit(), friendship.getUpperLimit());
        val totalPageSize = totalSize / pageable.getPageSize();
        return EachRelationFriendList.of(
                allOfPokeWithFriends,
                totalSize,
                totalPageSize,
                pageable.getPageSize(),
                friends.getNumber()
        );
    }

    @Transactional(readOnly = true)
    public SimplePokeProfile getPokeHistoryProfile(User user, Long friendId, Long pokeId) {
        PokeInfo.PokeDetail pokeDetail = getPokeInfo(pokeId);
        PokeInfo.PokedUserInfo friendUserInfo = getFriendUserInfo(user, friendId);

        List<PokeHistoryInfo> pokeHistoryList = pokeHistoryService.getAllOfPokeBetween(
                pokeDetail.getPokerId(), pokeDetail.getPokedId());

        return SimplePokeProfile.from(
                friendUserInfo,
                pokeDetail,
                getIsAlreadyPoke(pokeHistoryList, user.getId()),
                getIsAnonymous(pokeHistoryList, user.getId())
        );
    }


    private PokeInfo.PokedUserInfo getFriendUserInfo(User user, Long friendUserId) {
        val pokedUser = userService.getUserProfile(friendUserId);
        val pokedUserProfile = userService.getUserProfile(pokedUser.getPlaygroundId());
        val pokedUserPlaygroundProfile = (PlaygroundProfile) playgroundAuthService.getPlaygroundMemberProfiles(
                user.getPlaygroundToken(), List.of(pokedUserProfile.getPlaygroundId())).get(0);
        val latestActivity = pokedUserPlaygroundProfile.getLatestActivity();
        val mutualFriendNames = friendService.getMutualFriendIds(user.getId(), friendUserId).stream()
                .map(id -> {
                    UserInfo.UserProfile friendProfile = userService.getUserProfile(id);
                    return friendProfile.getName();
                })
                .toList();
        val relationInfo = friendService.getRelationInfo(user.getId(), friendUserId);
        return PokeInfo.PokedUserInfo.builder()
                .userId(pokedUserProfile.getUserId())
                .playgroundId(pokedUserPlaygroundProfile.getMemberId())
                .name(pokedUserPlaygroundProfile.getName())
                .profileImage(pokedUserPlaygroundProfile.getProfileImage())
                .generation(Integer.parseInt(latestActivity.getGeneration()))
                .part(latestActivity.getPart())
                .relation(relationInfo)
                .mutualFriendNames(mutualFriendNames)
                .build();
    }

    private PokeInfo.PokeDetail getPokeInfo(Long pokeHistoryId) {
        return pokeService.getPokeDetail(pokeHistoryId);
    }

    public boolean getIsNewUser(Long userId) {
        return friendService.getIsNewUser(userId);
    }
}
