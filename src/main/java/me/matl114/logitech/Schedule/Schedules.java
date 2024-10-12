package me.matl114.logitech.Schedule;


import me.matl114.logitech.Utils.Debug;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class Schedules {
    private static BukkitRunnable autoSaveThread;
    private static BukkitRunnable effectThread;
    public static Plugin plugin;
    public static void setupSchedules(Plugin plugin){
        Schedules.plugin=plugin;
        autoSaveThread = new BukkitRunnable() {
            @Override
            public void run() {
                ScheduleSave.onPeriodicSave();
            }
        };
        int periodAutoSave=3 * 60 * 20;//三分钟保存一次
        autoSaveThread.runTaskTimerAsynchronously(plugin, periodAutoSave, periodAutoSave);

        effectThread = new BukkitRunnable() {
            public void run() {
                ScheduleEffects.scheduleEffects();
            }
        };
        int periodEffect=20;
        effectThread.runTaskTimer(plugin, 200, periodEffect);

        BukkitRunnable postRegisterTask = new BukkitRunnable() {
            public void run() {
                SchedulePostRegister.schedulePostRegister();
            }
        };
        int delayPostRegister=1;
        postRegisterTask.runTaskLater(plugin,delayPostRegister);
    }
    public static void onDisableSchedules(Plugin plugin){
        Debug.logger("保存附属数据中");
        ScheduleSave.onFinalSave();
        Debug.logger("附属数据保存完成");
    }
    public static void launchSchedules(BukkitRunnable thread ,int delay,boolean isSync){
        launchSchedules(thread,delay,isSync,-1);
    }
    public static void launchSchedules(BukkitRunnable thread ,int delay,boolean isSync,int period){
        if(period<=0){
            if(isSync){
                if(delay!=0)
                    thread.runTaskLater(plugin, delay);
                else
                    thread.runTask(plugin);
            }else{
                if(delay!=0)
                    thread.runTaskLaterAsynchronously(plugin,delay);
                else
                    thread.runTaskAsynchronously(plugin);
            }
        }else{
            if(isSync){
                thread.runTaskTimer(plugin, delay, period);
            }else{
                thread.runTaskTimerAsynchronously(plugin, delay,period);
            }
        }
    }
    public static void launchSchedules(Runnable thread ,int delay,boolean isSync,int period){
        launchSchedules(getRunnable(thread),delay,isSync,period);
    }
    public static void launchRepeatingSchedule(Consumer<Integer> thread , int delay, boolean isSync, int period,int repeatTime){
        launchSchedules(new BukkitRunnable() {
            int runTime=0;
            @Override
            public void run() {
                try{
                    thread.accept(runTime);
                }catch (Throwable e){
                    e.printStackTrace();
                }
                finally {
                    this.runTime+=1;
                    if(this.runTime>=repeatTime){
                        this.cancel();
                    }
                }
            }
        },delay,isSync,period);
    }
    //this method should be called async
    public static void asyncWaitSchedule(Runnable thread,int delay,boolean isSync){
        //不得阻塞主线程
        assert !Bukkit.isPrimaryThread();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        launchSchedules(()->{thread.run();countDownLatch.countDown();},delay,isSync,0);
        try{
            countDownLatch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
    public static void asyncWaithRepeatingSchedule(Consumer<Integer> thread , int delay, boolean isSync, int period,int repeatTime){
        assert !Bukkit.isPrimaryThread();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        launchSchedules(new BukkitRunnable() {
            int runTime=0;
            @Override
            public void run() {
                try{
                    thread.accept(runTime);
                }catch (Throwable e){
                    e.printStackTrace();
                }
                finally {
                    this.runTime+=1;
                    if(this.runTime>=repeatTime){
                        countDownLatch.countDown();
                        this.cancel();
                    }
                }
            }
        },delay,isSync,period);
        try{
            countDownLatch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
    public static BukkitRunnable getRunnable(Runnable runnable){
        return new BukkitRunnable() {
            public void  run(){
                runnable.run();
            }
        };
    }
}
