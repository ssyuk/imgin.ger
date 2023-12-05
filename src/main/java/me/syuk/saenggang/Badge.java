package me.syuk.saenggang;

import java.util.Random;

public enum Badge {
    POOP(1, Grade.LEGENDARY),
    DOG(2, Grade.UNCOMMON),
    CAT(3, Grade.RARE),
    MOUSE(4, Grade.EPIC),
    HAMSTER(5, Grade.LEGENDARY),
    FOX(6, Grade.COMMON),
    BEAR(7, Grade.UNCOMMON),
    PANDA_FACE(8, Grade.RARE),
    KOALA(9, Grade.EPIC),
    TIGER(10, Grade.LEGENDARY),
    LION(11, Grade.COMMON),
    COW(12, Grade.UNCOMMON),
    PIG(13, Grade.RARE),
    FROG(14, Grade.EPIC),
    MONKEY_FACE(15, Grade.LEGENDARY),
    CHICKEN(16, Grade.COMMON),
    PENGUIN(17, Grade.UNCOMMON),
    BIRD(18, Grade.RARE),
    BABY_CHICK(19, Grade.EPIC),
    HATCHING_CHICK(20, Grade.LEGENDARY),
    HATCHED_CHICK(21, Grade.COMMON),
    WOLF(22, Grade.UNCOMMON),
    BOAR(23, Grade.RARE);

    private final int id;
    private final Grade grade;

    Badge(int id, Grade grade) {
        this.id = id;
        this.grade = grade;
    }

    public static Badge getRandomBadge() {
        Random random = new Random();
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;

        for (Badge badge : values()) {
            cumulativeProbability += badge.grade.getProbability();
            if (randomValue <= cumulativeProbability) {
                return badge;
            }
        }

        return values()[values().length - 1];
    }

    public static Badge getBadgeById(int id) {
        for (Badge badge : values()) if (badge.getId() == id) return badge;
        return null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return Utils.capitalize(name().toLowerCase());
    }

    public Grade getGrade() {
        return grade;
    }

    public String getEmoji() {
        return ":" + name().toLowerCase() + ":";
    }

    @Override
    public String toString() {
        return getEmoji();
    }

    public enum Grade {
        COMMON(0.4),
        UNCOMMON(0.3),
        RARE(0.2),
        EPIC(0.1),
        LEGENDARY(0.0);

        private final double probability;

        Grade(double probability) {
            this.probability = probability;
        }

        public double getProbability() {
            return probability;
        }
    }
}

