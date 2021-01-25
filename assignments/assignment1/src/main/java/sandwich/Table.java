package sandwich;

import java.util.ArrayList;
import java.util.Arrays;

public class Table {

    private ArrayList<Ingredient> ingredients; //list of current ingredients, can only be a max size of 3
    private int count;
    private int MAX;

    /**
     * Table class that chefs and agents BOTH need to access
     */
    public Table(){
        this.ingredients = new ArrayList<Ingredient>();
        this.count = 0;
        this.MAX = 30;
    }

    /**
     * Time to eat !
     * @param ingredientOne first ingredient placed
     * @param ingredientTwo seccond ingredient placed
     */
    public synchronized void eatingTime(Ingredient ingredientOne, Ingredient ingredientTwo){


        while(ingredients.size() > 0){
            //wait while the table has some stuff on it
            try{
                wait();
            } catch (InterruptedException e){
                return;
            }
        }

        //add items to table
        for (Ingredient ingredient : Arrays.asList(ingredientOne, ingredientTwo)) addIngredient(ingredient);


        notifyAll();

    }

    public synchronized void eat(Chef chef){


        while(ingredients.size() == 0 || count >= MAX){
            //table is empty or contains the ingredient
            //just in case there are loose threads


            try{



                wait();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        System.out.println(chef.getName() + " has made a sandwich!");
        clearTable();
        count++;
        System.out.println("count is now " + count);

        notifyAll();
    }

    /**
     * get ingredients list
     * @return ingredients list
     */
    public synchronized ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }


    /**
     * add ingredient to arraylist of ingredients
     * @param ingredient
     */
    public void addIngredient(Ingredient ingredient){
       if(ingredients.size() < 3){
           this.ingredients.add(ingredient);
       } else {
           System.out.println("No more ingredients can be placed");
       }
    }

    /**
     * Clear table of any items
     */
    public synchronized void clearTable(){
        this.ingredients.clear();
    }

    public int getCount(){
        return count;
    }

    public int getMax(){
        return MAX;
    }

}
