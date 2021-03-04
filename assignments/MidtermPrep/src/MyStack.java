public class MyStack {

    public static final int SIZE = 5;

    private int stackPtr = 0, count = 0;

    private boolean full = false;

    private boolean empty = true;

    private Object[] myStack = new Object[SIZE];

    private synchronized void put(Object obj){

        //cant put in
        while(full){
            try{
                wait();
            } catch (InterruptedException e){
                return;
            }
        }

        myStack[stackPtr] = obj;
        empty = false;

        stackPtr = (stackPtr + 1) % SIZE;
        count++;
        if(count == SIZE){
            full = true;
        }

    }
}
