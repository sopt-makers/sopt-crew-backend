package org.sopt.makers.crew.main.entity.post;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mumu_text")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MumuText extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "text")
	private String text;

	@Column(name = "category")
	private String category;

	@Column(name = "show_start_date")
	private LocalDateTime showStartDate;

	@Column(name = "show_end_date")
	private LocalDateTime showEndDate;

	public static MumuText create(String text, String category, LocalDateTime showStartDate,
		LocalDateTime showEndDate) {
		MumuText mumuText = new MumuText();
		mumuText.text = text;
		mumuText.category = category;
		mumuText.showStartDate = showStartDate;
		mumuText.showEndDate = showEndDate;
		return mumuText;
	}

	public void update(String text, String category, LocalDateTime showStartDate, LocalDateTime showEndDate) {
		this.text = text;
		this.category = category;
		this.showStartDate = showStartDate;
		this.showEndDate = showEndDate;
	}
}
