package org.sopt.makers.crew.main.entity.user;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;


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
	 *
	 * **/
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

	public boolean updateIfChanged(User playgroundUser) {
		boolean isUpdated = false;

		if (validateAndUpdateName(playgroundUser.getName())) {
			isUpdated = true;
		}

		if (validateAndUpdateOrgId(playgroundUser.getId())) {
			isUpdated = true;
		}

		if (validateAndUpdateActivities(playgroundUser.getActivities())) {
			isUpdated = true;
		}

		if (validateAndUpdateProfileImage(playgroundUser.getProfileImage())) {
			isUpdated = true;
		}

		if (validateAndUpdatePhone(playgroundUser.getPhone())) {
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

	private boolean validateAndUpdateOrgId(Integer newOrgId) {
		if (!Objects.equals(this.orgId, newOrgId)) {
			this.orgId = newOrgId;
			return true;
		}
		return false;
	}

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


	public List<UserActivityVO> getActivities() {
		return activities;
	}
}
