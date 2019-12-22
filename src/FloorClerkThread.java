/*
Seung Lee
 */

import java.util.concurrent.Semaphore;

public class FloorClerkThread extends Thread{
    public static long time = System.currentTimeMillis();

    //Used for clerkTotalCustomerHelped Counter
    private static Semaphore mutex = new Semaphore(1, true);


    //Constructor
    public FloorClerkThread(int id)
    {
        setName("FloorClerk-" + id);
    }

    //RUN
    public void run()
    {
        waitingForCustomers();
        waitingForClosingTime();
        msg("Going Home");
    }


    /*
    1st Instruction
    Floor Clerk helping Customer with receiving item slip
     */
    private void waitingForCustomers()
    {
        try
        {
            msg("Waiting For Customers");
            while(true)
            {
                //Used mutex to synchronize int variable clerkTotalCustomerHelped
                mutex.acquire();
                //Checks if all customers were served
                if(MegaStore.clerkTotalCustomerHelped == MegaStore.numCustomers)
                {
                    mutex.release();
                    break;
                }

                //Blocked till customer arrives for service
                MegaStore.customerNeedsClerk.acquire();
                //Increments clerkTotalCustomerHelped when a customer is serviced
                ++MegaStore.clerkTotalCustomerHelped;
                mutex.release();

                //Signals the customer that is first in line for service
                MegaStore.waitingLineForClerk.release();
                msg("Helping Customer");
                sleep(2000);

                //Signals the customer to give the slip
                MegaStore.receivedClerkHelp.release();
                msg("Finished helping Customer");

            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }


    /*
    2nd Instruction
    Blocked until MinibusTerminalThread signals
     */
    public void waitingForClosingTime()
    {
        try
        {
            msg("Waiting for Closing Time");
            MegaStore.closingTime.acquire();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }




    public void msg(String m)
    {
        System.out.println("[" + (System.currentTimeMillis()-time) + "] " + getName() + ": " + m);
    }
}
