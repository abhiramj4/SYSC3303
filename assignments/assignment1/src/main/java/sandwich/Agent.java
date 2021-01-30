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


        while(this.table.getCount() < this.table.getMax()) {
            //get random ingredient

            Ingredient first = Ingredient.getRandomIngredient();
            Ingredient second = Ingredient.getRandomIngredient();

            while (first == second) {
                first = Ingredient.getRandomIngredient();
            }

            this.table.eatingTime(first, second);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

        }


    }


}
