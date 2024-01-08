package org.sopt.app.application.poke;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

public class PokeInfo {



    @Getter
    @Builder
    @ToString
    public static class Relationship {
        private final int pokeNum;
        private final String relationName;
    }

    @Getter
    @Builder
    @ToString
    public static class PokedUserInfo {
        private final Long userId;
        private final Long playgroundId;
        private final String name;
        private final String profileImage;
        private final Integer generation;
        private final String part;
        private final Relationship relation;
        private final List<String> mutualFriendNames;

    }

    @Getter
    @Builder
    @ToString
    public static class PokeDetail {
        private final Long id;
        private final Long pokerId;
        private final Long pokedId;
        private final String message;

    }

    @Getter
    @Builder
    @ToString
    public static class PokeMessageDetail {
        private final Long id;
        private final String content;

    }

}
