/*
Seung Lee
 */

import java.util.concurrent.Semaphore;

public class CashierThread extends Thread{
    public static long time = System.currentTimeMillis();

    //Used for cashierTotalCustomerHelped Counter
    private static Semaphore mutex = new Semaphore(1, true);


    //Constructor
    public CashierThread(int i)
    {
        if(i==0)
        {
            setName("Cashier-Cash");
        }
        else
        {
            setName("Cashier-Credit");
        }
    }


    public void run()
    {
        waitingForCustomers();
        waitingForClosingTime();
        msg("Going Home");
    }


    /*
    1st Instruction
    Cashier helping Customer to pay for item
     */
    private void waitingForCustomers()
    {
        msg("Waiting For Customers");
        while(true)
        {
            try
            {
                //CASH
                if(getName().equals("Cashier-Cash")) {
                    //Blocked till customer arrives for service
                    MegaStore.customerNeedsCashierCASH.acquire();
                    //Break if all customers were serviced
                    if(MegaStore.cashierTotalCustomerHelped == MegaStore.numCustomers)
                    {
                        break;
                    }
                    //Signals the customer that is first in line for service
                    MegaStore.waitingLineForCashierCASH.release();

                    msg("Helping Customer with Cash Payment");
                    sleep(2000);

                    //Signals the customer to indicate payment is done
                    MegaStore.receivedCashierHelpCASH.release();
                    msg("Finished Helping Customer");
                }

                //CREDIT
                else
                {
                    //Blocked till customer arrives for service
                    MegaStore.customerNeedsCashierCREDIT.acquire();
                    //Break if all customers were serviced
                    if(MegaStore.cashierTotalCustomerHelped == MegaStore.numCustomers)
                    {
                        break;
                    }
                    //Signals the customer that is first in line for service
                    MegaStore.waitingLineForCashierCREDIT.release();

                    msg("Helping Customer with Credit Payment");
                    sleep(2000);

                    //Signals the customer to indicate payment is done
                    MegaStore.receivedCashierHelpCREDIT.release();
                    msg("Finished Helping Customer");
                }
                //Used mutex to synchronize incrementation of cashierTotalCustomerHelped and the if statement
                mutex.acquire();
                if(++MegaStore.cashierTotalCustomerHelped == MegaStore.numCustomers)
                {
                    mutex.release();
                    //Signal the other CashierThread that is currently blocked
                    if(MegaStore.customerNeedsCashierCASH.hasQueuedThreads())
                    {
                        MegaStore.customerNeedsCashierCASH.release();
                    }
                    if(MegaStore.customerNeedsCashierCREDIT.hasQueuedThreads())
                    {
                        MegaStore.customerNeedsCashierCREDIT.release();
                    }
                    break;
                }
                mutex.release();

            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
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
