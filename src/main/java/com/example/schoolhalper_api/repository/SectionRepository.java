package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.Section;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SectionRepository extends CrudRepository<Section,Long> {
    Optional<Section> getSectionById(Long id);
}
