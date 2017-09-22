package no.qvidahl;

import java.util.concurrent.Semaphore;

/**
 * This is a solution to the Sleeping Barber Problem
 * Created by Thomas Qvidahl 19.09.2017
 * Problem wiki article: https://en.wikipedia.org/wiki/Sleeping_barber_problem
 * Code collected and adapted from various sources
 */
public class Sleepingbarber extends Thread {

    // Some modifiers
    private static final long BARBER_WORK_DELAY = 1000; // Modify worktime for barber
    private static final long CUSTOMER_DENSITY = 1000; // Modify how long between customers arrive

    // Set barbershop capacity, initialize empty seats.
    private final static int capacity = 5;
    private static int freeSeats = capacity;

    // Uses Semaphores to lock access to concurrent resources
    private static Semaphore customers = new Semaphore(0);
    private static Semaphore barber = new Semaphore(0);
    private static Semaphore chairs = new Semaphore(1);


    // Some counters
    private int servicedCustomers = 0;
    private int rejectedCustomers = 0;
    private int customerID = 1;
    private long totalWorkingTime = 0L;
    private static long startTime = System.currentTimeMillis();

    private static void msg(String msg) {
        // Use this to write to console
        System.out.println(System.currentTimeMillis() - startTime + " ms --- " + msg);
    }

    public static void main(String[] args) {

        // This is where we set up shop!
        Sleepingbarber shop = new Sleepingbarber();
        shop.start();

    }

    /**
     * Async run method. We instantiate the barber, and start him looking for customers.
     * Then we loop forever, instantiating new customers at random intervals.
     * The barber is looping and trying to find customers as they arrive.
     * When he is busy, they take a seat. If there are no seats, they leave.
     */
    @Override
    public void run() {

        Barber thomas = new Barber();
        thomas.start();

        while (true) {

            Customer customer = new Customer(customerID);
            customerID++;
            customer.start();
            msg("New customer arrives..");
            try {
                double waitFor = Math.random() * CUSTOMER_DENSITY; // Wait for x ms before next customer
                Thread.sleep((long) waitFor); // Gotta get double to long somehow.. :P

            } catch (InterruptedException ie) {
                msg("Whoa! Who interrupted the wait time??");
            }
        }
    }

    /**
     * Customer class. Each get an ID. They arrive and look for a chair. If no chairs are available, they leave.
     * Otherwise they take a seat.
     */
    class Customer extends Thread {

        // Threaded class for customers, they each get an ID:
        int id;

        // Indicates that customer is impatiently waiting to be serviced:
        boolean waitingForService = true;

        // Constructor, giving each customer their ID
        Customer(int i) {
            this.id = i;
        }

        // Method to get customer ID
        int getCustId() {
            return this.id;
        }

        // Async run method. Each customer loops and tries to get a lock on the chairs.
        // If they do, and there are free seats available, they take a seat, and decrement
        // the number of available free seats.
        @Override
        public void run() {
            while(waitingForService) { // Loop as long as customer is waiting
                try {
                    chairs.acquire();
                    if (freeSeats > 0 ) {
                        msg("Customer " + getCustId() + " took seat " + freeSeats);
                        freeSeats--;
                        customers.release(); // This signals the barber that a customer is ready
                        chairs.release(); // Release our lock on the chairs.
                        waitingForService = false; // Customer is leaving, never mind payment.. :P

                    }else { // freeSeats = 0 = No room for customer
                        waitingForService = false; // customer leaves
                        rejectedCustomers++; // Another disappointed customer
                        chairs.release(); // Let go of the chairs lock
                        System.out.println("No free seats available. Rejected " + rejectedCustomers + " customer(s)..");
                    }
                } catch (InterruptedException e) {
                    msg("Whoa! Who interrupted the customer thread??");
                }
            }
        }

    }

    /**
     * The barber looks for customers indefinitely.
     */
    class Barber extends Thread {

        Barber() {
        }

        @Override
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
                    msg("Whoa! Who interrupted the barber!?");
                }
            }

        }

        private void serviceCustomer() {
            try {
                double workFor = Math.random() * BARBER_WORK_DELAY; // Work for x number of ms
                msg("Barber is working for " + workFor + " ms..");
                Thread.sleep((long) workFor); //(long) workFor); // Gotta get double to long somehow.. :P
                servicedCustomers++; // When we're done, increment counter
                totalWorkingTime += workFor; // Add work time to total
                msg("Barber is done! Serviced " + servicedCustomers + " customer(s) so far..");
                msg("Total working time so far: " + totalWorkingTime + " ms");

            } catch (InterruptedException ie) {
                msg("Whoa! Who interrupted the working barber!?");
            }
        }

    }
}

