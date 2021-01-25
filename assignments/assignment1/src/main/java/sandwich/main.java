package sandwich;

public class main {

    public static void main(String[] args){

        Thread agent, chefOne, chefTwo, chefThree;
        Table table = new Table();

        agent = new Thread(new Agent(table), "Agent");

        chefOne = new Thread(new Chef("chef one",table, Ingredient.BREAD));
        chefTwo = new Thread(new Chef("chef two",table, Ingredient.PB));
        chefThree = new Thread(new Chef("chef three",table, Ingredient.JAM));

        agent.start();
        chefOne.start();
        chefTwo.start();
        chefThree.start();

    }
}
