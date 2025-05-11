package org.sopt.makers.crew.main.entity.property;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
	Optional<Property> findByKey(String key);

	void deleteByKey(String propertyKey);
}
