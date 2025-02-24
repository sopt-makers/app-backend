package org.sopt.app.presentation.home.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.app.application.playground.dto.PlayGroundEmploymentResponse.EmploymentPost;
import org.sopt.app.application.playground.dto.PostWithMemberInfo;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentPostResponse implements PostWithMemberInfo {
    private Long id;
    private String categoryName;
    private String profileImage;
    private String name;
    private String title;
    private String content;
    private String link;
    private List<String> images;

    public static EmploymentPostResponse of(EmploymentPost employmentPost, String link) {
        return EmploymentPostResponse.builder()
                .id(employmentPost.id())
                .categoryName(employmentPost.categoryName())
                .link(link)
                .title(employmentPost.title())
                .content(employmentPost.content())
                .images(employmentPost.images())
                .build();
    }

    public EmploymentPostResponse withMemberDetail(String name, String profileImage) {
        return EmploymentPostResponse.builder()
                .id(this.id)
                .categoryName(this.categoryName)
                .name(name)
                .profileImage(profileImage)
                .title(this.title)
                .content(this.content)
                .images(this.images)
                .link(this.link)
                .build();
    }
}