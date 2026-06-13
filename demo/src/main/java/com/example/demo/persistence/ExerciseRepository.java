package com.example.demo.persistence;

import com.example.demo.domain.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByEquipmentInAndDifficultyInAndMuscleGroup(String[] equipment, String[] difficulty, String muscleGroup);
    List<Exercise> findByEquipmentInAndDifficulty(List<String> equipment, String difficulty);

}
