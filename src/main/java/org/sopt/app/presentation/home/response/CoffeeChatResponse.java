package org.sopt.app.presentation.home.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.sopt.app.application.playground.dto.PlayGroundCoffeeChatResponse;

@Getter
@Builder
public class CoffeeChatResponse {
    private Long memberId;
    private String bio;
    private List<String> topicTypeList;
    private String profileImage;
    private String link;
    private String name;
    private String career;
    private String organization;
    private String companyJob;
    private List<String> soptActivities;
    private String currentSoptActivity;

    public static CoffeeChatResponse of(PlayGroundCoffeeChatResponse playGroundCoffeeChatResponse, String currentSoptActivity,String link) {
        List<String> updatedSoptActivities = new ArrayList<>(playGroundCoffeeChatResponse.soptActivities());
        if (currentSoptActivity != null) {
            updatedSoptActivities.remove(currentSoptActivity);
        }
        return CoffeeChatResponse.builder()
                .memberId(playGroundCoffeeChatResponse.memberId())
                .bio(playGroundCoffeeChatResponse.bio())
                .topicTypeList(playGroundCoffeeChatResponse.topicTypeList())
                .profileImage(playGroundCoffeeChatResponse.profileImage())
                .name(playGroundCoffeeChatResponse.name())
                .link(link)
                .career(playGroundCoffeeChatResponse.career().equals("아직 없어요") ? null : playGroundCoffeeChatResponse.career())
                .organization(playGroundCoffeeChatResponse.organization())
                .companyJob(playGroundCoffeeChatResponse.companyJob())
                .soptActivities(updatedSoptActivities)
                .currentSoptActivity(currentSoptActivity)
                .build();
    }
}