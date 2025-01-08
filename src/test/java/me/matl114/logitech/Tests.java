package me.matl114.logitech;


import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.matl114.logitech.Utils.*;
import me.matl114.logitech.Utils.Algorithms.DynamicArray;
import me.matl114.logitech.Utils.Algorithms.SimpleLinkList;
import me.matl114.logitech.Utils.UtilClass.CargoClass.CargoConfigs;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tests {
    public static void log(String message) {
        System.out.println("Test: "+message);
    }


    public void test_configCode(){
       boolean symm=false;
       boolean isnull=true;
       boolean lazy=false;
       boolean bklst=true;
       boolean fromInput=true;
       boolean toOutput=false;
       boolean reverse=false;
       int trans=647;
       int configCode= CargoConfigs.setAllConfig(symm,isnull,lazy,bklst,fromInput,toOutput,reverse,trans);
       log("[TEST CONFIG CODE] "+configCode);
       assert CargoConfigs.IS_SYMM.getConfig(configCode)==symm;
       assert CargoConfigs.IS_NULL.getConfig(configCode)==isnull;
       assert CargoConfigs.IS_LAZY.getConfig(configCode)==lazy;
       assert CargoConfigs.TO_OUTPUT.getConfig(configCode)==toOutput;
       assert CargoConfigs.REVERSE.getConfig(configCode)==reverse;
       assert CargoConfigs.IS_WHITELST.getConfig(configCode)==bklst;
       assert CargoConfigs.FROM_INPUT.getConfig(configCode)==fromInput;
       assert CargoConfigs.TRANSLIMIT.getConfigInt(configCode)==trans;
       log("[TEST CONFIG CODE] TEST SUCCESS");
        ArrayList<Integer> a=new ArrayList<>(){{
            for(int i=0;i<16;++i){
                add(i);
            }
        }};
        log(a.size()+"");
        int size=a.size();
        a.subList(size-8,size).clear();
        log(a.size()+"");
    }

    public void test_MathUtils(){
        int code=-1;
        for (int i=-1;i<1;++i){
            if(MathUtils.bitCount(i,32)!=MathUtils.bitCountStupid(i,32)){
                log(String.valueOf(i));
                log("FAILED");
            }
        }
        log("FINISH");
        log(String.valueOf(MathUtils.bitCount(code,32)));
        log(String.valueOf(MathUtils.bitCountStupid(code,32)));
    }
    public void test_Import(){
    }

    public void test_Tasks(){
        List<CompletableFuture> futures=new ArrayList<>();
        AtomicInteger integer=new AtomicInteger();
        for (int i=0;i<3276800;++i){
            futures.add(CompletableFuture.runAsync(()->{
                for (int a=0;a<100_000_000;++a){
                    integer.get();
                }
            }).exceptionally(ex->{log(ex+"");return null;}));
        }
        log("sended");
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        log("joined");
    }
    
    public void test_lock(){

        byte[] lock=new byte[0];
        synchronized(lock){
            synchronized(lock){
                log("Get in 1");
            }
            CompletableFuture.runAsync(()->{
                synchronized(lock){
                    log("Get in 2");
                }
            }).orTimeout(2,TimeUnit.SECONDS).exceptionally((ex)->{
                log("exception handled ");
                ex.printStackTrace();
                return null;
            }).join();
        }
    }

    public void test_color(){
        log(AddUtils.color("aaaaa"));
    }


}
