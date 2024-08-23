package org.sopt.makers.crew.main.entity.advertisement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImage, Long> {
	List<AdvertisementImage> findAllByAdvertisementOrderByImageOrder(Advertisement advertisement);
}
