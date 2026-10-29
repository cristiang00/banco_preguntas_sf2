package com.example.questionbank.domain.model;

import java.util.Objects;

/**
 * Domain entity representing an academic Competency evaluated in Saber Pro exams.
 *
 * <p>Strictly independent of persistence frameworks.</p>
 */
public class Competency {

    private Long id;
    private String code;
    private String name;
    private String description;

    public Competency() {
    }

    public Competency(Long id, String code, String name, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Competency(String code, String name, String description) {
        this(null, code, name, description);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
        Competency that = (Competency) o;
        return Objects.equals(id, that.id) || Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "Competency{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
