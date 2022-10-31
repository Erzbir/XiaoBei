package com.erzbir.xiaobei_fast;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @Author: Erzbir
 * @Date: 2022/10/31 15:47
 *
 * <code> 此类是线程类, 除 report(上报)线程 是Callable实现, 其他均为Thread </code>
 */
public class Threads {
    public Thread login;
    public Thread verify;
    public Thread getHealth;
    public Thread report;
    public Thread send;
    public Thread save;
    public Thread getPlace;
    public FutureTask<Boolean> futureTask; // 通过futureTask.get()获取返回值
    Action action;

    public Threads(User user, Head head) {
        action = new Action(user, head);
        Callable<Boolean> callable = () -> {
            login.join();
            getHealth.join();
            return action.report();
        };
        futureTask = new FutureTask<>(callable);
        verify = new Thread(() -> action.verify());
        login = new Thread(() -> {
            try {
                verify.join();
                action.logIn();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        report = new Thread(futureTask);
        getHealth = new Thread(() -> {
            try {
                getPlace.join();
                action.getHealth();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        getPlace = new Thread(() -> action.getPlace());
        save = new Thread(() -> {
            try {
                verify.join();
                login.join();
                report.join();
                action.saveLog();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        send = new Thread(() -> {
            try {
                verify.join();
                login.join();
                report.join();
                action.getSendMessage().send_email();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean begin() throws ExecutionException, InterruptedException {
        save.setPriority(1);
        send.setPriority(1);
        verify.setPriority(4);
        getPlace.setPriority(4);
        getPlace.setPriority(4);
        verify.start();
        login.start();
        report.start();
        getHealth.start();
        send.start();
        save.start();
        try {
            send.join();
            save.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return futureTask.get();
        // System.out.println(user.getUSERNAME() + "操作完成");
    }

}
