package org.sopt.app.common.utils;

import java.security.SecureRandom;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AnonymousNameGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final List<String> adjectives = List.of(
            "개운한", "걸쭉한", "고소한", "구수한", "그윽한", "기름진", "깔끔한", "끈적한", "녹는", "녹진한",
            "달달한", "달콤한", "달큰한", "담백한", "독한", "뜨거운", "로제맛", "마라맛", "매운", "매콤한",
            "맹맹한", "미묘한", "미지근한", "민초맛", "밍밍한", "바삭한", "부드러운", "뽀얀", "산뜻한", "새콤한",
            "색다른", "시원한", "시큼한", "신선한", "싱싱한", "쌉쌀한", "씁쓸한", "아삭한", "알싸한", "얼얼한",
            "얼큰한", "연한", "자극적인", "정갈한", "절인", "진한", "진득한", "짜장맛", "짭조름한", "짭짤한",
            "차가운", "찰진", "카레맛", "칼칼한", "텁텁한", "톡쏘는", "풍미있는", "풍성한", "향긋한", "향기로운"
    );
    private static final List<String> nouns = List.of(
            "가지볶음", "간장게장", "갈비찜", "갈비탕", "갈치조림", "감자전", "감자탕", "계란빵", "고추튀김", "국밥",
            "김밥", "김치전", "김치찌개", "꼬치", "낙지볶음", "냉면", "닭갈비", "닭강정", "닭발", "돈까스",
            "돼지불백", "된장찌개", "두루치기", "떡꼬치", "떡볶이", "라면", "마늘치킨", "만두", "메로나", "멸치볶음",
            "무말랭이", "물회", "미역국", "보쌈", "부대찌개", "부침개", "불고기", "불닭", "붕어빵", "비빔밥",
            "빈대떡", "새우튀김", "설렁탕", "소떡소떡", "수제비", "순대", "씨앗호떡", "아구찜", "야채곱창", "야채김밥",
            "약과", "약밥", "양념게장", "양념치킨", "어묵", "오뎅탕", "우동", "유과", "육개장", "잡채",
            "전복죽", "조개구이", "족발", "찐빵", "차돌박이", "참치김밥", "찹쌀떡", "칼국수", "콩나물국", "탕수육",
            "튀김우동", "파전", "팥빙수", "편육", "핫도그", "호두과자", "호떡", "호박죽", "화채", "후라이드"
    );


    public static String generateRandomString() {
        String firstWord = getRandomElement(adjectives);
        String secondWord = getRandomElement(nouns);
        return "익명의 " + firstWord + " " + secondWord;
    }

    private static String getRandomElement(List<String> list) {
        return list.get(random.nextInt(list.size()));
    }
}
