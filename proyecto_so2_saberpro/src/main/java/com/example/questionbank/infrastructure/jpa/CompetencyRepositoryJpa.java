package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Concrete JPA implementation of domain CompetencyRepository contract.
 */
@Repository
@Transactional
public class CompetencyRepositoryJpa implements CompetencyRepository {

    private final SpringCompetencyDataRepository springCompetencyDataRepository;
    private final DomainMapper mapper;

    public CompetencyRepositoryJpa(SpringCompetencyDataRepository springCompetencyDataRepository, DomainMapper mapper) {
        this.springCompetencyDataRepository = springCompetencyDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Competency save(Competency competency) {
        CompetencyRecord record = mapper.toRecord(competency);
        CompetencyRecord saved = springCompetencyDataRepository.save(record);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Competency> findById(Long id) {
        return springCompetencyDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Competency> findByCode(String code) {
        return springCompetencyDataRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Competency> findAll() {
        return springCompetencyDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springCompetencyDataRepository.deleteById(id);
    }
}
