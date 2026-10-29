package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Technology-agnostic domain repository contract for Question entities.
 *
 * <p>Defines core query and persistence operations independently of JPA or database technology.</p>
 */
public interface QuestionRepository {

    /**
     * Persists or updates a question.
     *
     * @param question the question entity to save
     * @return the saved question with generated identifier
     */
    Question save(Question question);

    /**
     * Finds a question by its unique identifier.
     *
     * @param id question identifier
     * @return an Optional containing the question if found
     */
    Optional<Question> findById(Long id);

    /**
     * Lists all questions in the bank.
     *
     * @return list of questions
     */
    List<Question> findAll();

    /**
     * Finds questions created by a specific author.
     *
     * @param author author user
     * @return list of questions created by author
     */
    List<Question> findByAuthor(User author);

    /**
     * Finds questions by their lifecycle status.
     *
     * @param status lifecycle status
     * @return list of matching questions
     */
    List<Question> findByStatus(QuestionStatus status);

    /**
     * Finds published questions associated with a specific academic competency.
     *
     * @param competencyId competency identifier
     * @return list of published questions
     */
    List<Question> findPublishedByCompetency(Long competencyId);

    /**
     * Deletes a question by its identifier.
     *
     * @param id question identifier
     */
    void deleteById(Long id);
}
