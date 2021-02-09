package sandwich;

public class Chef extends Thread {

    private Table table; //chef has access to table
    private Ingredient ingredient; //current ingredient of this chef

    public Chef(String name, Table table, Ingredient ingredient) {
        super(name);
        this.table = table;
        this.ingredient = ingredient;

    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    /**
     * chef needs to access the table and make a sandwich based on what is available
     */
    public void run() {


        while (this.table.getCount() <= this.table.getMax()) {

            this.table.eat(this);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


    }

}
