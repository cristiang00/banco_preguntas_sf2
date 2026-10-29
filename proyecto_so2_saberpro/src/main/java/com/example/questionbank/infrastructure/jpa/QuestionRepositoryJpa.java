package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Concrete JPA implementation of the domain QuestionRepository contract.
 *
 * <p>Translates between pure domain models and JPA entity records via {@link DomainMapper},
 * delegating persistence operations to {@link SpringQuestionDataRepository}.</p>
 */
@Repository
@Transactional
public class QuestionRepositoryJpa implements QuestionRepository {

    private final SpringQuestionDataRepository springQuestionDataRepository;
    private final SpringUserDataRepository springUserDataRepository;
    private final SpringTopicDataRepository springTopicDataRepository;
    private final SpringCompetencyDataRepository springCompetencyDataRepository;
    private final DomainMapper mapper;

    public QuestionRepositoryJpa(SpringQuestionDataRepository springQuestionDataRepository,
                                 SpringUserDataRepository springUserDataRepository,
                                 SpringTopicDataRepository springTopicDataRepository,
                                 SpringCompetencyDataRepository springCompetencyDataRepository,
                                 DomainMapper mapper) {
        this.springQuestionDataRepository = springQuestionDataRepository;
        this.springUserDataRepository = springUserDataRepository;
        this.springTopicDataRepository = springTopicDataRepository;
        this.springCompetencyDataRepository = springCompetencyDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Question save(Question question) {
        QuestionRecord record = mapper.toRecord(question);

        // Re-attach managed author if exists
        if (question.getAuthor() != null && question.getAuthor().getId() != null) {
            Optional<UserRecord> userOpt = springUserDataRepository.findById(question.getAuthor().getId());
            userOpt.ifPresent(record::setAuthor);
        }

        // Re-attach managed topic if exists
        if (question.getTopic() != null && question.getTopic().getId() != null) {
            Optional<TopicRecord> topicOpt = springTopicDataRepository.findById(question.getTopic().getId());
            topicOpt.ifPresent(record::setTopic);
        }

        // Re-attach managed competencies
        if (question.getCompetencies() != null && !question.getCompetencies().isEmpty()) {
            Set<CompetencyRecord> managedCompetencies = new HashSet<>();
            for (var comp : question.getCompetencies()) {
                if (comp.getId() != null) {
                    springCompetencyDataRepository.findById(comp.getId()).ifPresent(managedCompetencies::add);
                }
            }
            record.setCompetencies(managedCompetencies);
        }

        QuestionRecord savedRecord = springQuestionDataRepository.save(record);
        return mapper.toDomain(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Question> findById(Long id) {
        return springQuestionDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findAll() {
        return springQuestionDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findByAuthor(User author) {
        if (author == null || author.getId() == null) {
            return List.of();
        }
        return springQuestionDataRepository.findByAuthorId(author.getId()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findByStatus(QuestionStatus status) {
        return springQuestionDataRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findPublishedByCompetency(Long competencyId) {
        return springQuestionDataRepository.findByCompetencyIdAndStatus(competencyId, QuestionStatus.PUBLISHED).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springQuestionDataRepository.deleteById(id);
    }
}
