package no.qvidahl;

/**
 * This is my solution to the Sleeping Barber Problem
 * Created by Thomas Qvidahl 19.09.2017
 * Problem wiki article: https://en.wikipedia.org/wiki/Sleeping_barber_problem
 */
public class Main {

    private static boolean isSleeping = false;
    private final static int capacity = 10;
    private static int freeSeats = capacity;

    public static void main(String[] args) {


        while(true) { // Endless loop

            if (customerArrives()) {
                takeASeat();
            }

            // Barber checks the waiting room for new customers
            if(freeSeats < capacity) {
                startWorking();
            }else {
                //sleep(); // Barber goes to sleep
                msg("Shh! The barber is sleeping!");
            }


        }
    }

    private static void msg(String msg) {
        System.out.println(msg);
    }
    private boolean isFreeSeat() {
        return freeSeats > 0;
    }

    private static void startWorking() {

        freeSeats++; // Customer leaves waiting room
        isSleeping = false;
    }


    private static void takeASeat() {
        if (freeSeats > 0) {
            msg("Please take a seat, the barber will be with you shortly.. Maybe..");
            freeSeats =- 1;
        }else {
            msg("Sorry, no room for you right now. Please come back later!");
        }
    }

    private static boolean customerArrives() {

        return Math.random() >= 0.5; // Fifty-fifty chance of customer arriving
    }


}

