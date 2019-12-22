/*
Seung Lee
 */

public class MinibusTerminalThread extends Thread{
    public static long time = System.currentTimeMillis();


    //Constructor
    public MinibusTerminalThread()
    {
        setName("MinibusTerminalThread");
    }


    public void run()
    {
        standbyForDeparture();
        departingBuses();
        closingStore();
        msg("Store Closed");
    }

    /*
    1st Instruction
    Waits for all customers to enter buses
     */
    public void standbyForDeparture()
    {
        int busNum = 1;
        while(true)
        {
            try
            {
                //Breaks once all customers are inside a bus
                if(MegaStore.customerInsideBuses == MegaStore.numCustomers)
                {
                    break;
                }
                msg("Bus " + busNum++ +" waiting for customers");
                sleep(20);
                //Blocked until the last customer of the group signals
                MegaStore.readyToGetOnBus.acquire();
                //Blocked until all the customers of the group enters the bus
                MegaStore.groupGotOnBus.acquire();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    /*
    2nd Instruction
    Departs all the customers inside the bus
     */
    public void departingBuses()
    {
        int busNum = 1;
        while(true)
        {
            //Break if there is no more customers in the queue
            if(MegaStore.waitingInsideBus.getQueueLength() == 0)
            {
                break;
            }

            msg("Bus " + busNum++ + " Departing");
            //Scenario 1
            //if the size of the queue is at least miniSize, signal miniSize customers
            if(MegaStore.waitingInsideBus.getQueueLength() >= MegaStore.miniSize)
            {
                for(int i=0; i<MegaStore.miniSize; i++)
                {
                    //Signal customer for departure
                    MegaStore.waitingInsideBus.release();
                }
            }
            //Scenario 2
            //if the size of the queue is lower than miniSize, signal the rest
            else
            {
                for(int i=0; i<MegaStore.waitingInsideBus.getQueueLength(); i++)
                {
                    //Signal customer for departure
                    MegaStore.waitingInsideBus.release();
                }
            }
            try
            {
                sleep(100);
            }
            catch(InterruptedException e)
            {

            }
        }
    }

    /*
    3rd Instruction
    Signals the rest of the Thread that are still alive
     */
    public void closingStore()
    {
        for(int i=0; i<MegaStore.closingTime.getQueueLength(); i++)
        {
            MegaStore.closingTime.release();
        }
    }



    public void msg(String m)
    {
        System.out.println("[" + (System.currentTimeMillis()-time) + "] " + getName() + ": " + m);
    }
}
