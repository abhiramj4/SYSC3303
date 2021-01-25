package sandwich;

import java.util.Arrays;

public class Agent extends Thread{

    private Table table;

    /**
     * Agent has access to table
     * @param table
     */
    public Agent(Table table) {
        this.table = table;
    }

    public void run() {


        while(true) {
            //get random ingredient

            if(this.table.getCount() == this.table.getMax()){
                break;
            }

            Ingredient first = Ingredient.getRandomIngredient();
            Ingredient second = Ingredient.getRandomIngredient();

            while (first == second) {
                first = Ingredient.getRandomIngredient();
            }

            this.table.eatingTime(first, second);

        }


    }


}
