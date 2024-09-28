package org.sopt.makers.crew.main.entity.notice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notice")
public class Notice extends BaseTimeEntity {
    /**
     * primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 공지사항 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 공지사항 부제목
     */
    @Column(nullable = false)
    private String subTitle;

    /**
     * 공지사항 내용
     */
    @Column(nullable = false)
    private String contents;

    /**
     * 공지사항 작성일
     */
    @Column(name = "createdDate", nullable = false, columnDefinition = "TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdDate;

    /**
     * 공지사항 노출 시작일
     */
    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime exposeStartDate;

    /**
     * 공지사항 노출 종료일
     */
    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime exposeEndDate;

    @Builder
    public Notice(String title, String subTitle, String contents,
                  LocalDateTime exposeStartDate, LocalDateTime exposeEndDate) {
        this.title = title;
        this.subTitle = subTitle;
        this.contents = contents;
        this.exposeStartDate = exposeStartDate;
        this.exposeEndDate = exposeEndDate;
    }
}
