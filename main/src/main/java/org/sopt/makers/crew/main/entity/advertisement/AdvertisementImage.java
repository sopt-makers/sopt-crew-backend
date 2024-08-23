package org.sopt.makers.crew.main.entity.advertisement;

import static jakarta.persistence.GenerationType.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "advertisement_image")
public class AdvertisementImage {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Integer id;

	@NotNull
	private String advertisementImageUrl;

	@NotNull
	private Integer imageOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "advertisementId")
	@NotNull
	private Advertisement advertisement;

}
