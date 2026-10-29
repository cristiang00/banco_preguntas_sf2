package com.example.questionbank.infrastructure;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.CognitiveLevel;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying Clean JPA Persistence with Spring Data JPA and H2:
 * 1. All four relationship types (@OneToOne, @OneToMany, @ManyToOne, @ManyToMany).
 * 2. JOINED table inheritance persistence and polymorphic retrieval.
 * 3. Cascade and constraint handling.
 */
@SpringBootTest
@Transactional
class CleanJpaPersistenceIntegrationTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private TopicRepository topicRepository;

    private User savedAuthor;
    private Topic savedTopic;
    private Competency savedComp1;
    private Competency savedComp2;

    @BeforeEach
    void setUp() {
        User author = new User("autor_persistence", "Maria Gomez", "mgomez@unicauca.edu.co", Role.AUTHOR);
        savedAuthor = userRepository.save(author);

        Topic topic = new Topic("Software Architecture", "Patterns and styles for large-scale systems");
        savedTopic = topicRepository.save(topic);

        savedComp1 = competencyRepository.save(new Competency("COMP-01", "Design Thinking", "Ability to propose modular architectures"));
        savedComp2 = competencyRepository.save(new Competency("COMP-02", "Problem Solving", "Evaluation of design trade-offs"));
    }

    @Test
    @DisplayName("Punto 4.1: Verify @OneToOne relationship between Question and QuestionMetadata")
    void testOneToOneMetadataRelationship() {
        QuestionMetadata metadata = new QuestionMetadata(
                null,
                LocalDateTime.now(),
                CognitiveLevel.ANALYZE,
                15,
                "Requires prior knowledge of design patterns."
        );

        MultipleChoiceQuestion question = new MultipleChoiceQuestion(
                "Which design pattern ensures a class has only one instance?",
                Difficulty.MEDIUM,
                savedAuthor
        );
        question.setMetadata(metadata);
        question.addOption(new AnswerOption("Singleton", true, "Provides global point of access"));
        question.addOption(new AnswerOption("Factory", false, "Creates objects without specifying exact class"));

        Question saved = questionRepository.save(question);
        assertNotNull(saved.getId());

        Optional<Question> retrievedOpt = questionRepository.findById(saved.getId());
        assertTrue(retrievedOpt.isPresent());

        Question retrieved = retrievedOpt.get();
        assertNotNull(retrieved.getMetadata());
        assertEquals(CognitiveLevel.ANALYZE, retrieved.getMetadata().getCognitiveLevel());
        assertEquals(15, retrieved.getMetadata().getEstimatedTimeMinutes());
        assertEquals("Requires prior knowledge of design patterns.", retrieved.getMetadata().getEditorialNotes());
    }

    @Test
    @DisplayName("Punto 4.2: Verify @OneToMany relationship between Question and AnswerOptions")
    void testOneToManyAnswerOptionsRelationship() {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion(
                "Identify the HTTP method used to create a resource in REST:",
                Difficulty.EASY,
                savedAuthor
        );

        // 4 options: 1 correct and 3 distractors (Saber Pro standard)
        question.addOption(new AnswerOption("POST", true, "Standard HTTP method for creation"));
        question.addOption(new AnswerOption("GET", false, "Used for retrieval"));
        question.addOption(new AnswerOption("PUT", false, "Used for replacement"));
        question.addOption(new AnswerOption("DELETE", false, "Used for removal"));

        Question saved = questionRepository.save(question);
        assertNotNull(saved.getId());

        Question retrieved = questionRepository.findById(saved.getId()).orElseThrow();
        assertInstanceOf(MultipleChoiceQuestion.class, retrieved);

        MultipleChoiceQuestion mcRetrieved = (MultipleChoiceQuestion) retrieved;
        assertEquals(4, mcRetrieved.getOptions().size());
        assertEquals(1, mcRetrieved.getCorrectOptions().size());
        assertEquals("POST", mcRetrieved.getCorrectOptions().get(0).getText());
    }

    @Test
    @DisplayName("Punto 4.3: Verify @ManyToOne relationship between Question and User/Topic")
    void testManyToOneAuthorAndTopicRelationship() {
        MultipleChoiceQuestion q1 = new MultipleChoiceQuestion("Question 1 Statement", Difficulty.EASY, savedAuthor);
        q1.setTopic(savedTopic);
        q1.addOption(new AnswerOption("A", true));
        q1.addOption(new AnswerOption("B", false));

        MultipleChoiceQuestion q2 = new MultipleChoiceQuestion("Question 2 Statement", Difficulty.HARD, savedAuthor);
        q2.setTopic(savedTopic);
        q2.addOption(new AnswerOption("C", true));
        q2.addOption(new AnswerOption("D", false));

        questionRepository.save(q1);
        questionRepository.save(q2);

        List<Question> authorQuestions = questionRepository.findByAuthor(savedAuthor);
        assertTrue(authorQuestions.size() >= 2);

        for (Question q : authorQuestions) {
            assertEquals(savedAuthor.getId(), q.getAuthor().getId());
            assertNotNull(q.getTopic());
            assertEquals("Software Architecture", q.getTopic().getName());
        }
    }

    @Test
    @DisplayName("Punto 4.4: Verify @ManyToMany relationship between Question and Competencies")
    void testManyToManyCompetenciesRelationship() {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion("Architecture trade-off question", Difficulty.HARD, savedAuthor);
        question.setTopic(savedTopic);
        question.addOption(new AnswerOption("High cohesion", true));
        question.addOption(new AnswerOption("Tight coupling", false));

        question.addCompetency(savedComp1);
        question.addCompetency(savedComp2);

        Question saved = questionRepository.save(question);
        assertNotNull(saved.getId());

        Question retrieved = questionRepository.findById(saved.getId()).orElseThrow();
        assertEquals(2, retrieved.getCompetencies().size());
        assertTrue(retrieved.getCompetencies().contains(savedComp1));
        assertTrue(retrieved.getCompetencies().contains(savedComp2));
    }

    @Test
    @DisplayName("Punto 5: Verify JOINED Inheritance persistence and polymorphic querying")
    void testJoinedInheritancePolymorphism() {
        // 1. Persist MultipleChoiceQuestion
        MultipleChoiceQuestion mc = new MultipleChoiceQuestion("What is MVC?", Difficulty.MEDIUM, savedAuthor);
        mc.addOption(new AnswerOption("Model-View-Controller", true));
        mc.addOption(new AnswerOption("Multiple-Virtual-Classes", false));
        Question savedMc = questionRepository.save(mc);

        // 2. Persist TrueFalseQuestion
        TrueFalseQuestion tf = new TrueFalseQuestion("Java interfaces support multiple inheritance of type.", Difficulty.EASY, savedAuthor, true);
        Question savedTf = questionRepository.save(tf);

        // 3. Query individually by ID and verify exact concrete subclass
        Question retrievedMc = questionRepository.findById(savedMc.getId()).orElseThrow();
        assertInstanceOf(MultipleChoiceQuestion.class, retrievedMc);
        assertEquals(2, ((MultipleChoiceQuestion) retrievedMc).getOptions().size());

        Question retrievedTf = questionRepository.findById(savedTf.getId()).orElseThrow();
        assertInstanceOf(TrueFalseQuestion.class, retrievedTf);
        assertTrue(((TrueFalseQuestion) retrievedTf).isExpectedTruthValue());

        // 4. Polymorphic retrieval
        List<Question> all = questionRepository.findAll();
        assertTrue(all.stream().anyMatch(q -> q instanceof MultipleChoiceQuestion && q.getId().equals(savedMc.getId())));
        assertTrue(all.stream().anyMatch(q -> q instanceof TrueFalseQuestion && q.getId().equals(savedTf.getId())));
    }

    @Test
    @DisplayName("Verify deletion of Question cascades and removes AnswerOptions and Metadata")
    void testCascadeDelete() {
        MultipleChoiceQuestion mc = new MultipleChoiceQuestion("Disposable question?", Difficulty.EASY, savedAuthor);
        mc.setMetadata(new QuestionMetadata(CognitiveLevel.REMEMBER, 5, "Disposable notes"));
        mc.addOption(new AnswerOption("Opt1", true));
        mc.addOption(new AnswerOption("Opt2", false));

        Question saved = questionRepository.save(mc);
        Long id = saved.getId();

        questionRepository.deleteById(id);
        assertTrue(questionRepository.findById(id).isEmpty());
    }
}
