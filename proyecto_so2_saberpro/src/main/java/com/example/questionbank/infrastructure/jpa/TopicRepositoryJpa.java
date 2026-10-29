package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Concrete JPA implementation of domain TopicRepository contract.
 */
@Repository
@Transactional
public class TopicRepositoryJpa implements TopicRepository {

    private final SpringTopicDataRepository springTopicDataRepository;
    private final DomainMapper mapper;

    public TopicRepositoryJpa(SpringTopicDataRepository springTopicDataRepository, DomainMapper mapper) {
        this.springTopicDataRepository = springTopicDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Topic save(Topic topic) {
        TopicRecord record = mapper.toRecord(topic);
        TopicRecord saved = springTopicDataRepository.save(record);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Topic> findById(Long id) {
        return springTopicDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> findAll() {
        return springTopicDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springTopicDataRepository.deleteById(id);
    }
}
