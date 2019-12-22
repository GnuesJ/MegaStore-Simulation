/*
Seung Lee
 */

import java.util.concurrent.Semaphore;

public class MegaStore {
    public static CustomerThread[] customerThreads;
    public static FloorClerkThread[] floorClerkThreads;
    public static CashierThread[] cashierThreads;
    public static MinibusTerminalThread minibusTerminalThread;

    //Default size of each Threads
    public static int numCustomers = 15;
    public static int numFloorClerks = 3;
    public static int numCashiers = 2;
    public static int miniSize = 4;


    /*
    Semaphore
     */
    //Blocks FloorClerkThread until a customer arrives
    public static Semaphore customerNeedsClerk = new Semaphore(0, true);
    //Put CustomerThread into the queue if all the FloorClerk is busy
    public static Semaphore waitingLineForClerk = new Semaphore(0, true);
    //Blocks CustomerThread when the FloorClerk is currently helping the customer
    public static Semaphore receivedClerkHelp = new Semaphore(0, true);

    //Blocks the CashierThread for CASH until a customer arrives
    public static Semaphore customerNeedsCashierCASH = new Semaphore(0, true);
    //Put CustomerThread into the queue if the CashierThread for CASH is busy
    public static Semaphore waitingLineForCashierCASH = new Semaphore(0, true);
    //Blocks CustomerThread when the Cashier for CASH is currently helping the customer
    public static Semaphore receivedCashierHelpCASH = new Semaphore(0, true);

    //Blocks the CashierThread for CREDIT until a customer arrives
    public static Semaphore customerNeedsCashierCREDIT = new Semaphore(0, true);
    //Put CustomerThread into the queue if the CashierThread for CREDIT is busy
    public static Semaphore waitingLineForCashierCREDIT = new Semaphore(0, true);
    //Blocks CustomerThread when the Cashier for CREDIT is currently helping the customer
    public static Semaphore receivedCashierHelpCREDIT = new Semaphore(0, true);

    //Signals MinibusTerminalThread once the customers forms a group
    public static Semaphore readyToGetOnBus = new Semaphore(0, true);
    //Blocks CustomerThread until the customers forms a group
    public static Semaphore waitingLineForBus = new Semaphore(0, true);
    //Signals MinibusTerminalThread once all the customers of the group gets on the bus
    public static Semaphore groupGotOnBus = new Semaphore(0, true);
    //Blocks CustomerThread until bus departs
    public static Semaphore waitingInsideBus = new Semaphore(0, true);

    //Blocks FloorClerkThread and CashierThread until closing time
    //MinibusTerminalThread Signals after all the buses departed
    public static Semaphore closingTime = new Semaphore(0, true);


    /*
    Counters
     */
    //Used by FloorClerkThread to break out of while-loop after helping all customers
    public static int clerkTotalCustomerHelped = 0;
    //Used by CashierThread to break out of while-loop after helping all customers
    public static int cashierTotalCustomerHelped = 0;
    //Used by CustomerThread to form groups before entering the bus
    public static int customerTotalFinished = 0;
    //Used by CustomerThread to enter the bus as a group
    //Also used by MinibusTerminal to break out of while-loop
    public static int customerInsideBuses = 0;



    //MAIN
    public static void main(String[] args)
    {
        if(args.length > 0 && Integer.parseInt(args[0]) > 0)
        {
                numCustomers = Integer.parseInt(args[0]);
        }

        //Initializing the array
        customerThreads = new CustomerThread[numCustomers];
        floorClerkThreads = new FloorClerkThread[numFloorClerks];
        cashierThreads = new CashierThread[numCashiers];


        //Creating the Threads
        minibusTerminalThread = new MinibusTerminalThread();

        for(int i=0; i<numFloorClerks; i++)
        {
            floorClerkThreads[i] = new FloorClerkThread(i+1);
        }

        for(int i=0; i<numCashiers; i++)
        {
            cashierThreads[i] = new CashierThread(i);
        }

        for(int i=0; i<numCustomers; i++)
        {
            customerThreads[i] = new CustomerThread(i+1);
        }


        //Starting the Threads
        minibusTerminalThread.start();

        for(int i=0; i<numFloorClerks; i++)
        {
            floorClerkThreads[i].start();
        }

        for(int i=0; i<numCashiers; i++)
        {
            cashierThreads[i].start();
        }

        for(int i=0; i<numCustomers; i++)
        {
            customerThreads[i].start();
        }

    }
}
