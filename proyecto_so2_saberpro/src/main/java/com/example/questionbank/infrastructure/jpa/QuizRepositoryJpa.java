package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuizDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Concrete JPA implementation of domain QuizRepository contract.
 */
@Repository
@Transactional
public class QuizRepositoryJpa implements QuizRepository {

    private final SpringQuizDataRepository springQuizDataRepository;
    private final SpringQuestionDataRepository springQuestionDataRepository;
    private final SpringUserDataRepository springUserDataRepository;
    private final DomainMapper mapper;

    public QuizRepositoryJpa(SpringQuizDataRepository springQuizDataRepository,
                             SpringQuestionDataRepository springQuestionDataRepository,
                             SpringUserDataRepository springUserDataRepository,
                             DomainMapper mapper) {
        this.springQuizDataRepository = springQuizDataRepository;
        this.springQuestionDataRepository = springQuestionDataRepository;
        this.springUserDataRepository = springUserDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Quiz save(Quiz quiz) {
        QuizRecord record = mapper.toRecord(quiz);

        // Re-attach managed createdBy user
        if (quiz.getCreatedBy() != null && quiz.getCreatedBy().getId() != null) {
            Optional<UserRecord> userOpt = springUserDataRepository.findById(quiz.getCreatedBy().getId());
            userOpt.ifPresent(record::setCreatedBy);
        }

        // Re-attach managed question records
        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            List<Long> questionIds = quiz.getQuestions().stream()
                    .map(Question::getId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            List<QuestionRecord> managedQuestions = springQuestionDataRepository.findAllById(questionIds);
            record.setQuestions(managedQuestions);
        }

        QuizRecord saved = springQuizDataRepository.save(record);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> findById(Long id) {
        return springQuizDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findAll() {
        return springQuizDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springQuizDataRepository.deleteById(id);
    }
}
