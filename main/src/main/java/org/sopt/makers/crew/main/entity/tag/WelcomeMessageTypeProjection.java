package org.sopt.makers.crew.main.entity.tag;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;

public interface WelcomeMessageTypeProjection {
	List<WelcomeMessageType> getWelcomeMessageTypes();
}
