package org.sopt.makers.crew.main.entity.common;

import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "dev", "prod"})
public class RealTime implements Time {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
