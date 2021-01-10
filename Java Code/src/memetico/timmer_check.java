/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package memetico;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Pasha
 */
public class timmer_check {
    Timer timer = new Timer();
    public static void main(final String[] args) {
        timmer_check a = new timmer_check();
        
        int seconds = 5;
        a.timmer_check_fun(seconds);
    }
       
TimerTask exitApp = new TimerTask() {
public void run() {
    System.out.println("test");
    System.exit(0);
    }
};


//    }
   public void timmer_check_fun(int seconds) {
timer.schedule(exitApp, new Date(System.currentTimeMillis()+seconds*1000));
    } 
}
