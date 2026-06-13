package com.example.demo.seed;

import com.example.demo.domain.entity.Exercise;
import com.example.demo.persistence.ExerciseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExerciseSeeder implements CommandLineRunner {

    private final ExerciseRepository repo;

    public ExerciseSeeder(ExerciseRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        repo.saveAll(List.of(
                ex("Push-ups", "CHEST", "HOME", "BEGINNER", "COMPOUND"),
                ex("Incline Push-ups", "CHEST", "HOME", "BEGINNER", "COMPOUND"),
                ex("Diamond Push-ups", "CHEST", "HOME", "INTERMEDIATE", "COMPOUND"),
                ex("Dumbbell Bench Press", "CHEST", "GYM", "BEGINNER", "COMPOUND"),
                ex("Incline Dumbbell Press", "CHEST", "GYM", "BEGINNER", "COMPOUND"),
                ex("Bench Press", "CHEST", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Incline Bench Press", "CHEST", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Cable Crossover", "CHEST", "GYM", "INTERMEDIATE", "ISOLATION"),
                ex("Dumbbell Flyes", "CHEST", "GYM", "BEGINNER", "ISOLATION"),
                ex("Chest Dips", "CHEST", "GYM", "INTERMEDIATE", "COMPOUND"),

                ex("Superman Hold", "BACK", "HOME", "BEGINNER", "ISOLATION"),
                ex("Inverted Rows", "BACK", "HOME", "BEGINNER", "COMPOUND"),
                ex("Barbell Row", "BACK", "GYM", "BEGINNER", "COMPOUND"),
                ex("One-arm DB Row", "BACK", "GYM", "BEGINNER", "COMPOUND"),
                ex("Pull-ups", "BACK", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Chin-ups", "BACK", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Lat Pulldown", "BACK", "GYM", "BEGINNER", "COMPOUND"),
                ex("Seated Cable Row", "BACK", "GYM", "BEGINNER", "COMPOUND"),
                ex("Deadlift", "BACK", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("T-Bar Row", "BACK", "GYM", "INTERMEDIATE", "COMPOUND"),

                ex("Pike Push-ups", "SHOULDERS", "HOME", "BEGINNER", "COMPOUND"),
                ex("Lateral Raises (Bottles)", "SHOULDERS", "HOME", "BEGINNER", "ISOLATION"),
                ex("Overhead Press", "SHOULDERS", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Dumbbell Shoulder Press", "SHOULDERS", "GYM", "BEGINNER", "COMPOUND"),
                ex("Lateral Raises", "SHOULDERS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Front Raises", "SHOULDERS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Face Pulls", "SHOULDERS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Arnold Press", "SHOULDERS", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Reverse Flyes", "SHOULDERS", "GYM", "BEGINNER", "ISOLATION"),

                ex("Bodyweight Squat", "LEGS", "HOME", "BEGINNER", "COMPOUND"),
                ex("Lunges", "LEGS", "HOME", "BEGINNER", "COMPOUND"),
                ex("Glute Bridge", "LEGS", "HOME", "BEGINNER", "ISOLATION"),
                ex("Wall Sit", "LEGS", "HOME", "BEGINNER", "ISOLATION"),
                ex("Step-ups", "LEGS", "HOME", "BEGINNER", "COMPOUND"),
                ex("Goblet Squat", "LEGS", "GYM", "BEGINNER", "COMPOUND"),
                ex("Back Squat", "LEGS", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Front Squat", "LEGS", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Leg Press", "LEGS", "GYM", "BEGINNER", "COMPOUND"),
                ex("Romanian Deadlift", "LEGS", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Leg Curl", "LEGS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Leg Extension", "LEGS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Calf Raises", "LEGS", "BOTH", "BEGINNER", "ISOLATION"),
                ex("Hip Thrust", "LEGS", "GYM", "BEGINNER", "COMPOUND"),
                ex("Bulgarian Split Squat", "LEGS", "GYM", "INTERMEDIATE", "COMPOUND"),

                ex("Chin-up Hold", "BICEPS", "HOME", "BEGINNER", "COMPOUND"),
                ex("Dumbbell Curl", "BICEPS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Barbell Curl", "BICEPS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Hammer Curl", "BICEPS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Preacher Curl", "BICEPS", "GYM", "INTERMEDIATE", "ISOLATION"),
                ex("Concentration Curl", "BICEPS", "GYM", "BEGINNER", "ISOLATION"),

                ex("Tricep Dips (Chair)", "TRICEPS", "HOME", "BEGINNER", "COMPOUND"),
                ex("Close-grip Push-ups", "TRICEPS", "HOME", "BEGINNER", "COMPOUND"),
                ex("Tricep Pushdown", "TRICEPS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Overhead Tricep Extension", "TRICEPS", "GYM", "BEGINNER", "ISOLATION"),
                ex("Skull Crushers", "TRICEPS", "GYM", "INTERMEDIATE", "ISOLATION"),
                ex("Close-grip Bench Press", "TRICEPS", "GYM", "INTERMEDIATE", "COMPOUND"),
                ex("Tricep Kickbacks", "TRICEPS", "GYM", "BEGINNER", "ISOLATION"),

                ex("Plank", "CORE", "BOTH", "BEGINNER", "ISOLATION"),
                ex("Side Plank", "CORE", "BOTH", "BEGINNER", "ISOLATION"),
                ex("Crunches", "CORE", "BOTH", "BEGINNER", "ISOLATION"),
                ex("Mountain Climbers", "CORE", "BOTH", "BEGINNER", "COMPOUND"),
                ex("Leg Raises", "CORE", "BOTH", "BEGINNER", "ISOLATION"),
                ex("Russian Twists", "CORE", "BOTH", "BEGINNER", "ISOLATION"),
                ex("Dead Bug", "CORE", "BOTH", "BEGINNER", "ISOLATION"),
                ex("Bird Dog", "CORE", "BOTH", "BEGINNER", "ISOLATION"),
                ex("Cable Crunch", "CORE", "GYM", "INTERMEDIATE", "ISOLATION"),
                ex("Hanging Leg Raise", "CORE", "GYM", "INTERMEDIATE", "ISOLATION"),
                ex("Ab Wheel Rollout", "CORE", "GYM", "INTERMEDIATE", "COMPOUND"),

                ex("Running", "CARDIO", "BOTH", "BEGINNER", "CARDIO"),
                ex("Jumping Jacks", "CARDIO", "HOME", "BEGINNER", "CARDIO"),
                ex("Burpees", "CARDIO", "BOTH", "INTERMEDIATE", "CARDIO"),
                ex("High Knees", "CARDIO", "HOME", "BEGINNER", "CARDIO"),
                ex("Jump Rope", "CARDIO", "BOTH", "BEGINNER", "CARDIO"),
                ex("Cycling", "CARDIO", "GYM", "BEGINNER", "CARDIO"),
                ex("Rowing Machine", "CARDIO", "GYM", "BEGINNER", "CARDIO"),
                ex("Stair Climber", "CARDIO", "GYM", "BEGINNER", "CARDIO"),
                ex("Elliptical", "CARDIO", "GYM", "BEGINNER", "CARDIO")
        ));
    }

    private Exercise ex(String name, String muscle, String equip, String diff, String type) {
        Exercise e = new Exercise();
        e.setName(name);
        e.setMuscleGroup(muscle);
        e.setEquipment(equip);
        e.setDifficulty(diff);
        e.setType(type);
        return e;
    }
}
