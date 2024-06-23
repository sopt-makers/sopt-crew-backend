package org.sopt.makers.crew.main.user.v2.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.apply.Apply;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserV2GetApplyByUserDto {
    List<Apply> applies;
    Integer count;
}
