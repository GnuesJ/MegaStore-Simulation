/*
Seung Lee
 */

import java.util.Random;
import java.util.concurrent.Semaphore;

public class CustomerThread extends Thread{

    public static long time = System.currentTimeMillis();
    //Used for customerTotalFinished Counter
    private static Semaphore mutex = new Semaphore(1, true);
    //Used for customerInsideBuses Counter
    private static Semaphore mutex2 = new Semaphore(1, true);


    //Constructor
    public CustomerThread(int id)
    {
        setName("Customer-" + id);
    }


    //RUN
    public void run()
    {
        enterStoreAndBrowse();
        makingDecision();
        looksForFloorClerk();
        goesToCashier();
        groupingToGetOnBus();
        waitingInsideBus();
    }

    /*
    1st instruction
    Customer will enter the store at random order and browse the store
     */
    private void enterStoreAndBrowse()
    {
        try
        {
            //walking to store
            sleep((long) (Math.random() * 20000 * Math.random()));
            //arrives
            msg("Arrived at the store and browsing");
            //browsing
            sleep((long) (Math.random() * 10000 * Math.random()));

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /*
    2nd Instruction
    Customer will decide whether he/she will buy the item
     */
    private void makingDecision()
    {
        try
        {
            //deciding whether to buy or not
            msg("Deciding whether to buy it or not");
            yield();
            sleep((long) (Math.random() * 1000));
            yield();
            sleep((long) (Math.random() * 1000));

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /*
    3rd Instruction
    Customer will look for Floor Clerks
    if all the Floor Clerks are busy, Customer will be blocked
     */
    private void looksForFloorClerk()
    {
        try
        {
            msg("Looking for Floor Clerk");
            sleep(1000);
            msg("Found Floor Clerk");
            //Waking up FloorClerk to help the customer
            MegaStore.customerNeedsClerk.release();

            //Waits in Queue till it is customer's turn
            MegaStore.waitingLineForClerk.acquire();

            //Waits for FloorClerk to give Slip
            MegaStore.receivedClerkHelp.acquire();

            msg("Received Slip");
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /*
    4th Instruction
    Customer will go to the Cashier Line to pay for the item
     */
    private void goesToCashier()
    {
        try
        {
            msg("Walking to the Cashier");
            sleep(500);
            msg("Arrive at Cash Register");
            Random random = new Random();
            if(random.nextInt(2) == 0)
            {
                msg("Paying by CASH");
                //Wakes CashierThread for CASH Line
                MegaStore.customerNeedsCashierCASH.release();
                //Waits in Queue till it is customer's turn
                MegaStore.waitingLineForCashierCASH.acquire();

                //Waits for Cashier-CASH to finish processing payment
                MegaStore.receivedCashierHelpCASH.acquire();
            }
            else
            {
                msg("Paying by CREDIT");
                //Wakes CashierThread for CREDIT Line
                MegaStore.customerNeedsCashierCREDIT.release();
                //Waits in Queue till it is customer's turn
                MegaStore.waitingLineForCashierCREDIT.acquire();

                //Waits for Cashier-CREDIT to finish processing payment
                MegaStore.receivedCashierHelpCREDIT.acquire();
            }


        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /*
    5th Instruction
    Customer will be block till a group is formed
     */
    private void groupingToGetOnBus()
    {
        try
        {
            msg("Walking to the Bus Terminal");
            sleep(500);
            msg("Arrived at the Bus Terminal");
            //Used mutex to synchronize the incrementation of customerTotalFinished
            mutex.acquire();
            //Scenario 1
            //if current group-size is equal to Bus Capacity
            if(++MegaStore.customerTotalFinished%MegaStore.miniSize == 0)
            {
                mutex.release();
                //Signals Minibus that the group is size of miniSize
                MegaStore.readyToGetOnBus.release();
                for(int i=0; i<MegaStore.miniSize-1; i++)
                {
                    //Release customers that are part of the group
                    MegaStore.waitingLineForBus.release();
                }
            }
            //Scenario 2
            //If customer that joined the group is the LAST customer
            else if(MegaStore.customerTotalFinished == MegaStore.numCustomers)
            {
                mutex.release();
                //Signals Minibus that the group is size of miniSize
                MegaStore.readyToGetOnBus.release();
                for(int i=0; i<MegaStore.waitingLineForBus.getQueueLength(); i++)
                {
                    //Release customers that are part of the group
                    MegaStore.waitingLineForBus.release();
                }
            }
            //Scenario 3
            //Otherwise block the customer, not enough customers to form a group yet
            else
            {
                mutex.release();
                //Block customer till the last customer to join the group to signals
                MegaStore.waitingLineForBus.acquire();
            }


        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /*
    6th Instruction
    Customer goes into the bus
    and waits for MiniBusTerminalThread to signal for departure
     */
    private void waitingInsideBus()
    {
        try
        {
            msg("Got on the Bus");
            //Used mutex2 to synchronize the incrementation of customerInsideBuses
            mutex2.acquire();
            //Last customer to enter the bus will signal MinibusTerminalThread
            //the group has enter the bus
            if(++MegaStore.customerInsideBuses%MegaStore.miniSize == 0 || MegaStore.customerInsideBuses == MegaStore.numCustomers)
            {
                MegaStore.groupGotOnBus.release();
            }
            mutex2.release();
            //Blocks till MinibusTerminalThread signals to go home
            MegaStore.waitingInsideBus.acquire();
            msg("Driven Home by Bus");
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
