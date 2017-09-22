package no.qvidahl;

import java.util.concurrent.Semaphore;

/**
 * This is a solution to the Sleeping Barber Problem
 * Created by Thomas Qvidahl 19.09.2017
 * Problem wiki article: https://en.wikipedia.org/wiki/Sleeping_barber_problem
 * Code collected and adapted from various sources
 */
public class Sleepingbarber extends Thread {

    public static Semaphore customers = new Semaphore(0);
    public static Semaphore barber = new Semaphore(0);
    public static Semaphore chairs = new Semaphore(1);

    private final static int capacity = 10;
    private static int freeSeats = capacity;

    private int servicedCustomers = 0;
    private int rejectedCustomers = 0;
    private int customerID = 1;
    private long totalWorkingTime = 0L;
    private long startTime = System.currentTimeMillis();


    private long runningTime() {
        long now = System.currentTimeMillis();
        return now - startTime;
    }

    private static void msg(String msg) {
        System.out.println(msg);
    }

    class Customer extends Thread {

        int id;

        boolean waitingForService = true;
        public Customer(int i) {
            this.id = i;
        }

        public int getCustId() {
            return this.id;
        }

        public void run() {
            while(waitingForService) { // Loop as long as customer is waiting
                try {
                    chairs.acquire();
                    if (freeSeats > 0 ) {
                        System.out.println("Customer " + getCustId() + " took seat " + freeSeats);
                        freeSeats--;
                        customers.release();
                        chairs.release();

                        try {
                            barber.acquire(); // Get a lock on the barber, if available
                            //this.getServiced(); // Service customer
                            // So if we managed to complete service without interruption,
                            // customer can leave, so:
                            waitingForService = false; // Customer is leaving, never mind payment.. :P

                        }catch (InterruptedException ie) {
                        }
                    }else { // freeSeats = 0 = No room for customer
                        waitingForService = false; // customer leaves
                        rejectedCustomers++; // Another disappointed customer
                        chairs.release();
                        System.out.println("No free seats available. Rejected " + rejectedCustomers + " customer(s)..");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void getServiced() {
            try{
                System.out.println("Servicing customer #" + servicedCustomers);
                Thread.sleep(1000); // Customer service takes x ms
            }catch (InterruptedException ie) {
                msg("Customer was interrupted during service");
            }

            servicedCustomers++;
            System.out.println("Servicing customer " + servicedCustomers);
        }

    }
    class Barber extends Thread {

        public Barber() {
        }

        public void run() {
            while(true) {

                try {
                    msg("Barber looking for customer");
                    customers.acquire(); // Try to get customer, if there are some
                    chairs.acquire(); // Barber woke up, try to modify number of free seats
                    freeSeats++;
                    barber.release(); // Ready to go to work
                    chairs.release(); // Done updating chairs
                    msg("Found customer, servicing! Now, " + freeSeats + " seats available.");
                    this.serviceCustomer();

                } catch (InterruptedException e) {
                }
            }

        }

        private void serviceCustomer() {
            try {
                double workFor = Math.random() * 5000; // Work for x number of ms
                msg("Barber is working for " + workFor + " ms..");
                Thread.sleep((long) workFor); //(long) workFor); // Gotta get double to long somehow.. :P
                servicedCustomers++;
                totalWorkingTime += workFor;
                msg("Barber is done! Serviced " + servicedCustomers + " customer(s) so far..");
                msg("Total working time so far: " + totalWorkingTime + " ms");
                msg("Total running time so far: " + runningTime() + " ms");

            }catch(InterruptedException ie) {
                msg("Barber was interrupted in his work! >:-O" + ie );
            }
        }

    }

    public static void main(String[] args) {

        Sleepingbarber shop = new Sleepingbarber();

        shop.start();

     }

    public void run() {

        Barber thomas = new Barber();
        thomas.start();

        while(true) {

            Customer customer = new Customer(customerID);
            customerID++;
            customer.start();
            msg("New customer arrives..");
            try{
                double waitFor = Math.random() * 3000; // Wait for x ms before next customer
                Thread.sleep((long) waitFor); // Gotta get double to long somehow.. :P

            }catch (InterruptedException ie) {
            }
        }
    }
}

