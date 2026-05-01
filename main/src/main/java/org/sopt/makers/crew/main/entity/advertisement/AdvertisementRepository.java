package org.sopt.makers.crew.main.entity.advertisement;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Integer> {

	default Advertisement findByIdOrThrow(Integer advertisementId) {
		return findById(advertisementId)
			.orElseThrow(() -> new BadRequestException("광고가 없습니다."));
	}

	@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Advertisement a " +
		"WHERE a.advertisementCategory = :category " +
		"AND a.isDisplay = true " +
		"AND a.id <> :advertisementId")
	boolean existsDisplayedAdvertisementByCategoryExcludingId(
		@Param("category") AdvertisementCategory category,
		@Param("advertisementId") Integer advertisementId);

	@Query("SELECT a FROM Advertisement a " +
		"WHERE a.isSponsoredContent = :isSponsored " +
		"AND a.advertisementCategory = :category " +
		"AND a.advertisementStartDate <= :now " +
		"AND a.advertisementEndDate >= :now " +
		"ORDER BY a.priority ASC")
	List<Advertisement> findAdvertisementsByDateAndType(
		@Param("category") AdvertisementCategory category,
		@Param("isSponsored") boolean isSponsored,
		@Param("now") LocalDateTime now,
		Pageable pageable);

	@Query("SELECT a FROM Advertisement a " +
		"WHERE a.isSponsoredContent = :isSponsored " +
		"AND a.advertisementCategory = :category " +
		"ORDER BY a.priority ASC")
	List<Advertisement> findAdvertisementsByCategory(
		@Param("category") AdvertisementCategory category,
		@Param("isSponsored") boolean isSponsored,
		Pageable pageable);

	@Query("SELECT a FROM Advertisement a " +
		"WHERE a.advertisementCategory = :category " +
		"AND a.isDisplay = true " +
		"AND a.advertisementStartDate <= :now " +
		"AND a.advertisementEndDate >= :now " +
		"ORDER BY a.priority ASC")
	List<Advertisement> findMeetingTopAdvertisements(
		@Param("category") AdvertisementCategory category,
		@Param("now") LocalDateTime now);
}
