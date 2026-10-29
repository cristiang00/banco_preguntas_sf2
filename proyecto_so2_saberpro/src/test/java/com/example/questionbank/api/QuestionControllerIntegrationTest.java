package com.example.questionbank.api;

import com.example.questionbank.api.dto.AnswerOptionDto;
import com.example.questionbank.api.dto.ClassifyQuestionRequest;
import com.example.questionbank.api.dto.CreateMultipleChoiceQuestionRequest;
import com.example.questionbank.api.dto.CreateQuizRequest;
import com.example.questionbank.api.dto.CreateTrueFalseQuestionRequest;
import com.example.questionbank.api.dto.QuestionMetadataDto;
import com.example.questionbank.domain.model.CognitiveLevel;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the REST API Presentation layer (QuestionController & QuizController).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuestionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    private User author;
    private User teacher;
    private Topic topic;
    private Competency competency;

    @BeforeEach
    void setUp() {
        author = userRepository.save(new User("rest_author", "Rest Author", "author@unicauca.edu.co", Role.AUTHOR));
        teacher = userRepository.save(new User("rest_teacher", "Rest Teacher", "teacher@unicauca.edu.co", Role.TEACHER));
        topic = topicRepository.save(new Topic("REST APIs", "HTTP REST architectural style"));
        competency = competencyRepository.save(new Competency("COMP-WEB", "Web Services", "Building distributed systems"));
    }

    @Test
    @DisplayName("POST /api/questions/multiple-choice should create question with 201 Created")
    void testCreateMultipleChoiceEndpoint() throws Exception {
        CreateMultipleChoiceQuestionRequest request = new CreateMultipleChoiceQuestionRequest(
                "Which status code represents resource creation in REST?",
                Difficulty.EASY,
                author.getId(),
                List.of(
                        new AnswerOptionDto("201 Created", true),
                        new AnswerOptionDto("200 OK", false),
                        new AnswerOptionDto("204 No Content", false),
                        new AnswerOptionDto("400 Bad Request", false)
                ),
                new QuestionMetadataDto(CognitiveLevel.REMEMBER, 3, "HTTP standard codes.")
        );

        mockMvc.perform(post("/api/questions/multiple-choice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("MULTIPLE_CHOICE"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.options.length()").value(4));
    }

    @Test
    @DisplayName("POST /api/questions/true-false should create question with 201 Created")
    void testCreateTrueFalseEndpoint() throws Exception {
        CreateTrueFalseQuestionRequest request = new CreateTrueFalseQuestionRequest(
                "Idempotent HTTP methods produce the same result regardless of how many times they are called.",
                Difficulty.MEDIUM,
                author.getId(),
                true,
                new QuestionMetadataDto(CognitiveLevel.UNDERSTAND, 5, "REST idempotence definition.")
        );

        mockMvc.perform(post("/api/questions/true-false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("TRUE_FALSE"))
                .andExpect(jsonPath("$.expectedTruthValue").value(true));
    }

    @Test
    @DisplayName("POST /api/questions/{id}/classify should associate topic and competencies")
    void testClassifyEndpoint() throws Exception {
        // Create question first
        CreateTrueFalseQuestionRequest createReq = new CreateTrueFalseQuestionRequest("Question to classify", Difficulty.EASY, author.getId(), true, null);
        String responseContent = mockMvc.perform(post("/api/questions/true-false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long questionId = objectMapper.readTree(responseContent).get("id").asLong();

        ClassifyQuestionRequest classifyReq = new ClassifyQuestionRequest(topic.getId(), Set.of(competency.getId()));

        mockMvc.perform(post("/api/questions/" + questionId + "/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topicName").value("REST APIs"))
                .andExpect(jsonPath("$.competencyCodes[0]").value("COMP-WEB"));
    }

    @Test
    @DisplayName("POST /api/quizzes with unapproved question should return 400 Bad Request")
    void testCreateQuizWithDraftQuestionFails() throws Exception {
        CreateTrueFalseQuestionRequest createReq = new CreateTrueFalseQuestionRequest("Unapproved question", Difficulty.EASY, author.getId(), true, null);
        String responseContent = mockMvc.perform(post("/api/questions/true-false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn().getResponse().getContentAsString();
        Long questionId = objectMapper.readTree(responseContent).get("id").asLong();

        CreateQuizRequest quizReq = new CreateQuizRequest("Invalid Quiz", "Testing draft rejection", teacher.getId(), List.of(questionId), 100);

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quizReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"));
    }
}
