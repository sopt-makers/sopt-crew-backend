package org.sopt.makers.crew.main.meeting.v2.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class MeetingCreatorDto {
    Integer id;
    String name;
    Integer orgId;

}
