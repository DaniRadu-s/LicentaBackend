package com.example.demo.testutil;

import com.example.demo.domain.entity.Exercise;
import com.example.demo.domain.entity.UserRestriction;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

public final class TestDataFactory {

    private static final AtomicLong ID_SEQ = new AtomicLong(1000);

    private TestDataFactory() {}

    public static Exercise createExercise(String name, String muscleGroup, String equipment, String difficulty, String type) {
        Exercise e = new Exercise();
        e.setName(name);
        e.setMuscleGroup(muscleGroup);
        e.setEquipment(equipment);
        e.setDifficulty(difficulty);
        e.setType(type);
        setId(e, ID_SEQ.getAndIncrement());
        return e;
    }

    public static UserRestriction createRestriction(String restrictionType) {
        UserRestriction r = new UserRestriction();
        r.setRestrictionType(restrictionType);
        setId(r, ID_SEQ.getAndIncrement());
        return r;
    }

    private static void setId(Object obj, long id) {
        try {
            Field f = obj.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(obj, id);
        } catch (Exception ignored) {}
    }
}
