package org.sopt.makers.crew.main.entity.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.like.Like;
import org.sopt.makers.crew.main.entity.like.Report;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {
    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * 사용자 이름
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * sopt org unique id
     */
    @Column(name = "orgId", nullable = false)
    private int orgId;

    /**
     * 활동 목록
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "activities")
    private UserActivityVO[] activities;

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
     * 내가 생성한 모임
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Meeting> meetings = new ArrayList<>();

    /**
     * 내가 지원한 내역
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Apply> applies = new ArrayList<>();

    /**
     * 작성한 게시글
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Post> posts = new ArrayList<>();

    /**
     * 좋아요
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

    /**
     * 신고 내역
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Report> reports = new ArrayList<>();

    @Builder
    public User(String name, int orgId, UserActivityVO[] activities, String profileImage, String phone) {
        this.name = name;
        this.orgId = orgId;
        this.activities = activities;
        this.profileImage = profileImage;
        this.phone = phone;
    }

    public void addMeeting(Meeting meeting) {
        meetings.add(meeting);
    }

    public void addApply(Apply apply) {
        applies.add(apply);
    }

    public void addLike(Like like) {
        likes.add(like);
    }

    public void addReport(Report report) {
        reports.add(report);
    }
}
