package org.sopt.makers.crew.main.entity.advertisement;

import java.time.LocalDateTime;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Integer> {
	Optional<Advertisement> findFirstByAdvertisementCategoryAndAdvertisementEndDateBeforeAndAdvertisementStartDateAfterOrderByPriority(
		AdvertisementCategory advertisementCategory, LocalDateTime now1, LocalDateTime now2);
}
