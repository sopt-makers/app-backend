package org.sopt.app.application.poke;

import lombok.RequiredArgsConstructor;
import org.sopt.app.common.event.Events;
import org.sopt.app.common.exception.NotFoundException;
import org.sopt.app.common.response.ErrorCode;
import org.sopt.app.domain.entity.Friend;
import org.sopt.app.domain.entity.PokeHistory;
import org.sopt.app.domain.entity.User;
import org.sopt.app.interfaces.postgres.FriendRepository;
import org.sopt.app.interfaces.postgres.PokeHistoryRepository;
import org.sopt.app.interfaces.postgres.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PokeService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final PokeHistoryRepository historyRepository;

    @Transactional
    public void poke(Long pokerUserId, Long pokedUserId, String pokeMessage) {
        User pokedUser = userRepository.findUserById(pokedUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));

        applyRelationship(pokerUserId, pokedUserId);
        createPokeByApplyReply(pokerUserId, pokedUserId, pokeMessage);

        Events.raise(PokeEvent.of(pokedUser.getPlaygroundId()));
    }

    private void createPokeByApplyReply(Long pokerUserId, Long pokedUserId, String pokeMessage) {
        boolean currentPokeReply = false;
        Optional<PokeHistory> recentPokeFromPokedUser = historyRepository.findByPokerIdAndIsReplyIsFalse(pokedUserId);
        if (recentPokeFromPokedUser.isPresent()) {
            currentPokeReply = true;
            recentPokeFromPokedUser.get().activateReply();
        }
        PokeHistory createdPoke = PokeHistory.builder()
                .pokerId(pokerUserId)
                .pokedId(pokedUserId)
                .message(pokeMessage)
                .isReply(currentPokeReply)
                .build();
        historyRepository.save(createdPoke);
    }

    private void applyRelationship(Long pokerUserId, Long pokedUserId) {
        boolean isPokedUserPokeBefore = historyRepository.existsByPokerIdAndPokedId(pokedUserId, pokerUserId);
        boolean isPokerUserPokeBefore = historyRepository.existsByPokerIdAndPokedId(pokerUserId, pokedUserId);
        if (isPokedUserPokeBefore) {
            if (!isPokerUserPokeBefore) {
                Friend createdRelationUserToFriend = Friend.builder()
                        .userId(pokerUserId)
                        .friendUserId(pokedUserId)
                        .pokeCount(1)
                        .build();
                Friend createdRelationFriendToUser = Friend.builder()
                        .userId(pokedUserId)
                        .friendUserId(pokerUserId)
                        .pokeCount(1)
                        .build();
                friendRepository.save(createdRelationUserToFriend);
                friendRepository.save(createdRelationFriendToUser);
            } else {
                Optional<Friend> relationUserToFriend = friendRepository.findByUserIdAndAndFriendUserId(pokerUserId, pokedUserId);
//                Optional<Friend> relationFriendToUser = friendRepository.findByUserIdAndAndFriendUserId(pokedUserId, pokerUserId);
                relationUserToFriend.get().addCount();
//                relationFriendToUser.get().addCount();
            }
        }
    }

//    private void applyRelationship(Long pokerUserId, Long pokedUserId) {
//        Optional<Friend> relationUserToFriend = friendRepository.findByUserIdAndAndFriendUserId(pokerUserId, pokedUserId);
//        Optional<Friend> relationFriendToUser = friendRepository.findByUserIdAndAndFriendUserId(pokedUserId, pokerUserId);
//        if (relationFriendToUser.isPresent()) {
//            if (relationUserToFriend.isPresent()) {
//                relationUserToFriend.get().addCount();
//                relationFriendToUser.get().addCount();
//            } else {
//                Friend createdRelationUserToFriend = Friend.builder()
//                        .userId(pokerUserId)
//                        .friendUserId(pokedUserId)
//                        .pokeCount(1)
//                        .build();
//                Friend createdRelationFriendToUser = Friend.builder()
//                        .userId(pokedUserId)
//                        .friendUserId(pokerUserId)
//                        .pokeCount(1)
//                        .build();
//                friendRepository.save(createdRelationUserToFriend);
//                friendRepository.save(createdRelationFriendToUser);
//            }
//        }
//    }
}
