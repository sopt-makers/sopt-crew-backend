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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    public User(Integer id, String name, Integer orgId, List<UserActivityVO> activities, String profileImage,
                String phone) {
        this.id = id;
        this.name = name;
        this.orgId = orgId;
        this.activities = activities;
        this.profileImage = profileImage;
        this.phone = phone;
    }

    public void setUserIdForTest(Integer userId) {
        this.id = userId;
    }

    public UserActivityVO getRecentActivityVO(){
        return activities.stream()
                .filter(userActivityVO -> userActivityVO.getPart() != null)
                .max(Comparator.comparingInt(UserActivityVO::getGeneration))
                .orElseThrow(() -> new ServerException(INTERNAL_SERVER_ERROR.getErrorCode()));
    }

    public void updateUser(String name, Integer orgId, List<UserActivityVO> activities, String profileImage,
        String phone){

        this.name = name;
        this.orgId = orgId;
        this.activities = activities;
        this.profileImage = profileImage;
        this.phone = phone;
    }

    public List<UserActivityVO> getActivities() {
        return activities;
    }
}
