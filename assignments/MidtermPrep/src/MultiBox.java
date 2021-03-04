public class MultiBox {

    int count = 0; //count of how many times an object may be removed from the box
    private Object contents = null; // box contents
    private boolean empty = true; // is box empty?

    private synchronized void put(Object obj, int count){
        while(!empty){
            try {
                wait();
            } catch (InterruptedException e){
                return;
            }
        }

        contents = obj;
        empty = false;
        this.count = count;
        notifyAll();
    }

    private synchronized Object get(){
        //can't get anything if the box is empty or if the count is 0
        while (empty || count == 0) {
            try {

                if (count == 0){
                    return null;
                }

                wait();
            } catch (InterruptedException e) {
                return null;
            }
        }

        Object obj = contents;

        if (count == 0){
            contents = null;
            empty = true;
            notifyAll();
        }


        return obj;
    }
}
