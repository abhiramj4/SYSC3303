public class Box {

    private Object contents = null; // box contents
    private boolean empty = true; // is box empty?

    public synchronized void put(Object item) {
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {return;}
        }
        contents = item;
        empty = false;
        notifyAll();
    }

    public synchronized Object get() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                return null;
            }
        }
        Object obj = contents;
        empty = true;
        contents = null;
        notifyAll();
        return obj;
    }

    public synchronized void replaceContents(Object obj){
        contents = obj;

        if(empty){
            empty = false;
            notifyAll();
        }
    }

    public synchronized void replaceContentsWhenFull(Object obj){
        while(empty){
            try {
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
                return;
            }
        }

        contents = obj;
    }

    public synchronized boolean isFull(){
        return !empty;
    }
}
