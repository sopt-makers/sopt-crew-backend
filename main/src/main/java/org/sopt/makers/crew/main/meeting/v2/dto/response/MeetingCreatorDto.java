package org.sopt.makers.crew.main.meeting.v2.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class MeetingCreatorDto {
    private final Integer id;
    private final String name;
    private final Integer orgId;
    private final String profileImage;

}
