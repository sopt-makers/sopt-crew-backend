package org.sopt.makers.crew.main.entity.user;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.exception.ServerException;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User extends BaseTimeEntity {

	/**
	 * Primary Key
	 */
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 사용자 이름
	 */
	@Column(name = "name", nullable = false)
	private String name;

	/**
	 * sopt org unique id
	 */
	@Column(name = "orgId", nullable = false)
	private Integer orgId;

	/**
	 * 활동 목록
	 */
	@Column(name = "activities", columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	private List<UserActivityVO> activities;

	/**
	 * 프로필 이미지
	 */
	@Column(name = "profileImage")
	private String profileImage;

	/**
	 * 핸드폰 번호
	 */
	@Column(name = "phone")
	private String phone;

	/**
	 * 유저 선호 키워드 타입
	 */
	@Column(name = "interestedKeywords", columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	@Size(min = 1, max = 2)
	private List<MeetingKeywordType> interestedKeywords;

	@Column(name = "isAlarmed")
	@ColumnDefault("false")
	private Boolean isAlarmed;

	@Builder
	public User(String name, Integer orgId, List<UserActivityVO> activities, String profileImage,
		String phone) {
		this.name = name;
		this.orgId = orgId;
		this.activities = activities;
		this.profileImage = profileImage;
		this.phone = phone;
	}

	public void setUserIdForTest(Integer userId) {
		this.id = userId;
	}

	/**
	 * @implSpec : redis 에서 조회한 데이터를 엔티티로 변환할 때 사용하는 메서드
	 **/
	public User withUserIdForRedis(Integer id) {
		this.id = id;
		return this;
	}

	public UserActivityVO getRecentActivityVO() {
		return activities.stream()
			.filter(userActivityVO -> userActivityVO.getPart() != null)
			.max(Comparator.comparingInt(UserActivityVO::getGeneration))
			.orElseThrow(() -> new ServerException(INTERNAL_SERVER_ERROR.getErrorCode()));
	}

	public void updateKeywords(List<MeetingKeywordType> keywords) {
		this.interestedKeywords = keywords;
	}

	public boolean updateIfChanged(User authUser) {
		boolean isUpdated = false;

		if (validateAndUpdateName(authUser.getName())) {
			isUpdated = true;
		}

		// if (validateAndUpdateOrgId(authUser.getOrgId())) {
		// 	isUpdated = true;
		// }

		if (validateAndUpdateActivities(authUser.getActivities())) {
			isUpdated = true;
		}

		if (validateAndUpdateProfileImage(authUser.getProfileImage())) {
			isUpdated = true;
		}

		if (validateAndUpdatePhone(authUser.getPhone())) {
			isUpdated = true;
		}

		return isUpdated;
	}

	private boolean validateAndUpdateName(String newName) {
		if (!Objects.equals(this.name, newName)) {
			this.name = newName;
			return true;
		}
		return false;
	}

	// private boolean validateAndUpdateOrgId(Integer newOrgId) {
	// 	if (!Objects.equals(this.orgId, newOrgId)) {
	// 		this.orgId = newOrgId;
	// 		return true;
	// 	}
	// 	return false;
	// }

	private boolean validateAndUpdateActivities(List<UserActivityVO> newActivities) {
		if (!Objects.equals(this.activities, newActivities)) {
			this.activities = newActivities;
			return true;
		}
		return false;
	}

	private boolean validateAndUpdateProfileImage(String newProfileImage) {
		if (!Objects.equals(this.profileImage, newProfileImage)) {
			this.profileImage = newProfileImage;
			return true;
		}
		return false;
	}

	private boolean validateAndUpdatePhone(String newPhone) {
		if (!Objects.equals(this.phone, newPhone)) {
			this.phone = newPhone;
			return true;
		}
		return false;
	}

	public List<MeetingKeywordType> getInterestedKeywords() {
		if (this.interestedKeywords == null) {
			return List.of();
		}
		return interestedKeywords;
	}

	public List<UserActivityVO> getActivities() {
		return activities;
	}
}
