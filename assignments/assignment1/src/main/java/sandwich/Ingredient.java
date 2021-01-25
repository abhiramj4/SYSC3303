package sandwich;

import java.util.Random;

/**
 * enum class for ingredient types
 */
public enum Ingredient {
    BREAD,JAM,PB;

    /**
     * Pick a random ingredient
     * @return a random ingredient
     */
    public static Ingredient getRandomIngredient(){
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
