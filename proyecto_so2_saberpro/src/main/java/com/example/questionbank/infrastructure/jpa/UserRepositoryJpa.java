package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.UserRepository;
import com.example.questionbank.infrastructure.jpa.mapper.DomainMapper;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Concrete JPA implementation of domain UserRepository contract.
 */
@Repository
@Transactional
public class UserRepositoryJpa implements UserRepository {

    private final SpringUserDataRepository springUserDataRepository;
    private final DomainMapper mapper;

    public UserRepositoryJpa(SpringUserDataRepository springUserDataRepository, DomainMapper mapper) {
        this.springUserDataRepository = springUserDataRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserRecord record = mapper.toRecord(user);
        UserRecord saved = springUserDataRepository.save(record);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return springUserDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return springUserDataRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return springUserDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        springUserDataRepository.deleteById(id);
    }
}
