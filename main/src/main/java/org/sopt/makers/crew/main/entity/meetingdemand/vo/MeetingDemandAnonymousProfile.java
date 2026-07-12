package org.sopt.makers.crew.main.entity.meetingdemand.vo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MeetingDemandAnonymousProfile {

	private static final int MIN_IMAGE_NUMBER = 1;
	private static final int MAX_IMAGE_NUMBER = 5;
	private static final int DEFAULT_IMAGE_NUMBER = 1;

	private static final List<String> ADJECTIVES = List.of(
		"열정적인",
		"믿음직한",
		"용기 있는",
		"성장하는",
		"도전하는",
		"끈기 있는",
		"재치 있는",
		"친화력 좋은",
		"솔직한",
		"창의적인",
		"영감을 주는",
		"적극적인",
		"유연한",
		"모험적인",
		"의욕적인",
		"통찰력 있는",
		"센스 있는",
		"책임감 있는",
		"배려 깊은",
		"호기심 많은",
		"실행력 있는",
		"집중력 좋은",
		"섬세한",
		"든든한",
		"긍정적인",
		"활기찬",
		"따뜻한",
		"성실한",
		"차분한",
		"민첩한",
		"대담한",
		"반짝이는"
	);

	private static final List<String> NOUNS = List.of(
		"토마토",
		"참외",
		"브로콜리",
		"고구마",
		"딸기",
		"복숭아",
		"자두",
		"포도",
		"수박",
		"체리",
		"레몬",
		"망고",
		"당근",
		"감자",
		"호박",
		"완두콩",
		"오리",
		"너구리",
		"개구리",
		"병아리",
		"사슴",
		"참새",
		"수달",
		"토끼",
		"다람쥐",
		"펭귄",
		"해달",
		"판다",
		"마카롱",
		"케이크",
		"푸딩",
		"쿠키"
	);

	private static final Map<Integer, String> IMAGE_URLS = Map.of(
		1, "https://sopt-makers-mds.s3.ap-northeast-2.amazonaws.com/anonymousImage/avatar_m.png",
		2, "https://sopt-makers-mds.s3.ap-northeast-2.amazonaws.com/anonymousImage/avatar_o.png",
		3, "https://sopt-makers-mds.s3.ap-northeast-2.amazonaws.com/anonymousImage/avatar_p.png",
		4, "https://sopt-makers-mds.s3.ap-northeast-2.amazonaws.com/anonymousImage/avatar_s.png",
		5, "https://sopt-makers-mds.s3.ap-northeast-2.amazonaws.com/anonymousImage/avatar_t.png"
	);

	public static String generateNickname() {
		return getRandomValue(ADJECTIVES) + " " + getRandomValue(NOUNS);
	}

	public static int generateImageNumber() {
		return ThreadLocalRandom.current().nextInt(MIN_IMAGE_NUMBER, MAX_IMAGE_NUMBER + 1);
	}

	public static String getImageUrl(int imageNumber) {
		return IMAGE_URLS.getOrDefault(imageNumber, IMAGE_URLS.get(DEFAULT_IMAGE_NUMBER));
	}

	private static String getRandomValue(List<String> values) {
		return values.get(ThreadLocalRandom.current().nextInt(values.size()));
	}
}
