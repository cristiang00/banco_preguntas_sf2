package com.example.questionbank.domain.model;

import java.util.Objects;

/**
 * Domain entity representing an academic Topic within an evaluation area.
 *
 * <p>Strictly independent of persistence frameworks.</p>
 */
public class Topic {

    private Long id;
    private String name;
    private String description;

    public Topic() {
    }

    public Topic(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Topic(String name, String description) {
        this(null, name, description);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return Objects.equals(id, topic.id) || Objects.equals(name, topic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
