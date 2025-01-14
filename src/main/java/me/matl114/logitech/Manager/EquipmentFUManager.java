package me.matl114.logitech.Manager;

import com.google.common.base.Preconditions;
import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.Validate;
import lombok.Getter;
import me.matl114.logitech.MyAddon;
import me.matl114.logitech.Utils.AddUtils;
import me.matl114.logitech.Utils.Debug;
import me.matl114.logitech.Utils.PdcUtils;
import me.matl114.logitech.Utils.UtilClass.EquipClass.EquipmentFU;
import me.matl114.matlib.core.Manager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EquipmentFUManager implements Manager, Listener {
    private Plugin plugin;
    @Getter
    private static EquipmentFUManager manager;
    private boolean registered = false;
    private final ForkJoinPool asyncTaskRunner = new ForkJoinPool(Math.min(4, Runtime.getRuntime().availableProcessors()));
    private BukkitTask equipmentTickTask;
    public EquipmentFUManager(){
        manager=this;
        init(MyAddon.getInstance());
    }
    @Override
    public EquipmentFUManager init(Plugin plugin, String... strings) {
        this.plugin = plugin;
        registerFunctional();
        this.equipmentTickTask=new BukkitRunnable() {
            @Override
            public void run() {
                EquipmentFUManager.this.equipmentTick();
            }
        }.runTaskTimer(plugin, 20, 1);
        return this;
    }

    @Override
    public EquipmentFUManager reload() {
        deconstruct();
        return init(plugin);
    }
    @Override
    public void deconstruct() {
        unregisterFunctional();
        asyncTaskRunner.shutdown();
    }

    private EquipmentFUManager registerFunctional(){
        Validate.isTrue(!registered, "EquipmentFUManager functional have already been registered!");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.registered=true;
        return this;
    }
    private EquipmentFUManager unregisterFunctional(){
        Validate.isTrue(registered, "EquipmentFUManager functional havem't been unregistered!");
        HandlerList.unregisterAll(this);
        this.registered=false;
        return this;
    }
    private final ConcurrentHashMap<UUID, HashMap<EquipmentFU,Integer>> equipmentFUMap=new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID,Long> updateTimeStamps=new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, BukkitTask> runningTasks=new ConcurrentHashMap<>();
    @EventHandler(ignoreCancelled=false,priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event){
        asyncTriggerPlayerUpdate(event,event.getPlayer());
    }
    @EventHandler(ignoreCancelled =false,priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        UUID uid=event.getPlayer().getUniqueId();
        removeTraceOfPlayer(uid);
    }
    //you know what?
    //swaping main hand and off hand doesn't seem to change LEVELS
//    @EventHandler(ignoreCancelled=true,priority = EventPriority.MONITOR)
//    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event){
//
//    }
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onPlayerSwapHotbar(PlayerItemHeldEvent event){
        asyncTriggerPlayerUpdate(event,event.getPlayer(),event.getNewSlot());
    }
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onPlayerInventory(InventoryClickEvent event){
        if(event.getWhoClicked() instanceof Player p){
            Schedules.execute(()->asyncTriggerPlayerUpdate(event,p),true);
        }
    }
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event){
        if(event.getWhoClicked() instanceof Player p){
            Schedules.execute(()->asyncTriggerPlayerUpdate(event,p),true);
        }
    }
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onPlayerItemDrop(PlayerDropItemEvent event){
        asyncTriggerPlayerUpdate(event,event.getPlayer());
    }
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        asyncTriggerPlayerUpdate(event,event.getPlayer());
    }
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onPlayerReload(PlayerChangedWorldEvent event){
        asyncTriggerPlayerUpdate(event,event.getPlayer());
    }
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onPlayerReloadTp(PlayerTeleportEvent event){
        asyncTriggerPlayerUpdate(event,event.getPlayer());
    }
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onPlayerItemDamage(PlayerItemDamageEvent event){
        if(event.getDamage()>=event.getItem().getType().getMaxDurability()){
            Schedules.execute(()->asyncTriggerPlayerUpdate(event,event.getPlayer()),true);
        }
    }
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onPlayerBeForceChange(BlockDispenseArmorEvent event){
        if(event.getTargetEntity() instanceof Player p){
            Schedules.execute(()->asyncTriggerPlayerUpdate(event,p),true);
        }
    }
    //maybe a block place? or some special equipment called
    @EventHandler(ignoreCancelled = false,priority = EventPriority.MONITOR)
    public void onPlayerDoSth(PlayerInteractEvent event){
        Schedules.execute(()->asyncTriggerPlayerUpdate(event,event.getPlayer()),true);
    }
    //todo find more events
    //todo add event Handler
//    @EventHandler(ignoreCancelled = false,priority = EventPriority.HIGHEST)
//    public void onPlayerEvent(PlayerEvent event){
//
//    }
    //todo shit ,this can't be managed with only a PlayerEvent
    @EventHandler(ignoreCancelled = false,priority = EventPriority.HIGHEST)
    public void onPlayerEvent1(PlayerInteractEvent event){
        onPlayerEvent(event);
    }
    @EventHandler(ignoreCancelled = false,priority = EventPriority.HIGHEST)
    public void onPlayerEvent2(PlayerSwapHandItemsEvent event){
        onPlayerEvent(event);
    }
    @EventHandler(ignoreCancelled = false,priority = EventPriority.HIGHEST)
    public void onPlayerEvent3(PlayerToggleSneakEvent event){
        onPlayerEvent(event);
    }
    @EventHandler(ignoreCancelled = false,priority = EventPriority.HIGHEST)
    public void onPlayerEvent4(PlayerToggleSprintEvent event){
        onPlayerEvent(event);
    }

    //more priority for attacking event
    //some event can be cancelled earlier
    @EventHandler(ignoreCancelled = false,priority = EventPriority.NORMAL)
    public void onPlayerDamageNormal(EntityDamageEvent event){
        if(event.getEntity() instanceof Player p){
            onPlayerDamage(event,p,EventPriority.NORMAL);
        }
    }
    @EventHandler(ignoreCancelled = false,priority = EventPriority.HIGHEST)
    public void onPlayerDamageHigh(EntityDamageEvent event){
        if(event.getEntity() instanceof Player p){
            onPlayerDamage(event,p,EventPriority.HIGHEST);
        }
    }
    @EventHandler(ignoreCancelled = false,priority = EventPriority.NORMAL)
    public void onPlayerAttackNormal(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player p){
            onPlayerAttack(event,p,EventPriority.NORMAL);
        }
    }
    @EventHandler(ignoreCancelled = false,priority = EventPriority.HIGHEST)
    public void onPlayerAttackHigh(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player p){
            onPlayerAttack(event,p,EventPriority.HIGHEST);
        }
    }

    private void onPlayerEvent(PlayerEvent event){
        Player player=event.getPlayer();
        var data=getEquipmentFUMap(player.getUniqueId());
        if(!data.isEmpty()){
            for (var entry:data.entrySet()){
                entry.getKey().onPlayerEventHandle(event,player,entry.getValue());
            }
        }
    }
    private void onPlayerDamage(EntityDamageEvent event,Player p,EventPriority priority){
        UUID uid=event.getEntity().getUniqueId();
        var data=getEquipmentFUMap(uid);
        if(!data.isEmpty()){
            for (var entry:data.entrySet()){
                entry.getKey().onDamage(event,priority,p,entry.getValue());
            }
        }
    }
    private void onPlayerAttack(EntityDamageByEntityEvent event,Player playerAttacker, EventPriority priority){
        UUID uid=playerAttacker.getUniqueId();
        var data=getEquipmentFUMap(uid);
        if(!data.isEmpty()){
            for (var entry:data.entrySet()){
                entry.getKey().onAttack(event,priority,playerAttacker,entry.getValue());
            }
        }
    }

    @Nonnull
    public Map<EquipmentFU,Integer> getEquipmentFUMap(UUID uuid){
        return Map.copyOf( equipmentFUMap.computeIfAbsent(uuid,(u)->new HashMap<>()));
    }
    private void removeTraceOfPlayer(UUID uid){
        equipmentFUMap.remove(uid);
        updateTimeStamps.remove(uid);
        runningTasks.remove(uid);
    }
//    @EventHandler
//    public void onPlayerLeaveWorld(PlayerKickEvent event){}
    public  void asyncTriggerPlayerUpdate(Event trigger,Player player){
        asyncTriggerPlayerUpdate(trigger,player,player.getInventory().getHeldItemSlot());
    }
    public  void asyncTriggerPlayerUpdate(Event trigger,Player player,int mainHandSlotNew){
        CompletableFuture.runAsync(()->{
            asyncTriggerPlayerFUUpdate(trigger,player,mainHandSlotNew,false);
        },asyncTaskRunner);
    }
    private void asyncTriggerPlayerFUUpdate(Event trigger, Player player,int mainHandSlot,boolean delayed ){
        long timeStamp=System.currentTimeMillis();
        UUID uuid=player.getUniqueId();
        //1个玩家1秒内不得触发超过1次
        //如果触发吵过一次，计划在20t后执行一次update 期间所有update全部忽视
        if(!delayed&&(runningTasks.containsKey(uuid)||updateTimeStamps.getOrDefault(player.getUniqueId(),0L)+500>timeStamp)){
            runningTasks.computeIfAbsent(uuid,(uid)->{
                return new BukkitRunnable() {
                    public void run() {
                        asyncTriggerPlayerFUUpdate(trigger,player,mainHandSlot,true);
                    }
                }.runTaskLater(plugin,10L);
            });
            return ;
        }
        HashMap<EquipmentFU,Integer> newData=new HashMap<>();
        PlayerInventory playerInventory=player.getInventory();
        //compute six slot and calculate sum
        //in case selectedChange
        ItemStack itemHand=playerInventory.getItem(mainHandSlot); //playerInventory.getItemInMainHand();
        calEquipmentFUMap(itemHand,EquipmentSlot.HAND,newData);
        ItemStack itemOffHand=playerInventory.getItemInOffHand();
        calEquipmentFUMap(itemOffHand,EquipmentSlot.OFF_HAND,newData);
        ItemStack itemHelmet=playerInventory.getHelmet();
        calEquipmentFUMap(itemHelmet,EquipmentSlot.HEAD,newData);
        ItemStack itemChestPlate=playerInventory.getChestplate();
        calEquipmentFUMap(itemChestPlate,EquipmentSlot.CHEST,newData);
        ItemStack itemLeggings=playerInventory.getLeggings();
        calEquipmentFUMap(itemLeggings,EquipmentSlot.LEGS,newData);
        ItemStack itemBoots=playerInventory.getBoots();
        calEquipmentFUMap(itemBoots,EquipmentSlot.FEET,newData);
        HashMap<EquipmentFU,Integer> oldData= equipmentFUMap.put(uuid,newData);
        oldData=oldData==null?new HashMap<>():new HashMap<>(oldData);
        //compare old and new, call related method
        for(var entry:newData.entrySet()){
            EquipmentFU equipmentFU=entry.getKey();
            if(oldData.containsKey(equipmentFU)){
                oldData.remove(equipmentFU);
                equipmentFU.onUpdate(trigger,player,entry.getValue());
            }else{
                equipmentFU.onEnable(trigger,player,entry.getValue());
            }
        }
        for(var entry:oldData.entrySet()){
            EquipmentFU equipmentFU=entry.getKey();
            equipmentFU.onRemove(trigger,player,entry.getValue());
        }
        //reset timings
        if(!delayed){
            updateTimeStamps.put(uuid,timeStamp);
        }else {
            runningTasks.remove(uuid);
        }
        //Debug.logger("update datas");
    }
    public void calEquipmentFUMap(ItemStack item, EquipmentSlot slot,Map<EquipmentFU,Integer> map){
        if(item==null||item.getType().isAir())return;
        ItemMeta itemMeta=item.getItemMeta();
        PersistentDataContainer dataContainer=itemMeta.getPersistentDataContainer();
        PersistentDataContainer fuField=PdcUtils.getTag(dataContainer,equipmentFUKey);
        if(fuField!=null){
            var keys= fuField.getKeys();
            for (var key : keys){
                EquipmentFU fu=EquipmentFU.getFunctionUnit(slot,key);
                if(fu!=null){
                    Integer level=fuField.get(key,PersistentDataType.INTEGER);
                    if(level!=null){
                        map.merge(fu,level,Integer::sum);
                    }
                }
            }
        }
       // return map;
    }
    private NamespacedKey equipmentFUKey = AddUtils.getNameKey("equip-fulvl");
    private NamespacedKey eqFUDataKey = AddUtils.getNameKey("equip-fudata");
    public void addEquipmentFU(ItemStack item, EquipmentFU equipmentFU,int level){
        addEquipmentFU(item, Map.of(equipmentFU,level));
    }
    public void addEquipmentFU(ItemStack item,Map<EquipmentFU,Integer> equipmentFU){
        Preconditions.checkArgument(item!=null&&item.getType().isAir() ,"itemStack cannot be null!");
        ItemMeta meta=item.getItemMeta();
        PersistentDataContainer container=meta.getPersistentDataContainer();
        PersistentDataContainer fuFields = PdcUtils.getOrCreateTag(container, equipmentFUKey);
        PersistentDataContainer fuDataField = PdcUtils.getOrCreateTag(container, eqFUDataKey);
        for(var equipmentFu:equipmentFU.entrySet()){
            NamespacedKey key=equipmentFu.getKey().getKey();
            Integer level=0;
            if(fuFields.has(key, PersistentDataType.INTEGER)){
                level=fuFields.get(key, PersistentDataType.INTEGER);
            }
            level=level==null?equipmentFu.getValue():level+equipmentFu.getValue();
            fuFields.set(key, PersistentDataType.INTEGER, level);
            equipmentFu.getKey().onEquip(item,meta,fuDataField,level);
        }
        PdcUtils.setTag(container,equipmentFUKey,fuFields);
        PdcUtils.setTag(container,eqFUDataKey,fuDataField);
        item.setItemMeta(meta);
    }
    private AtomicInteger tickCount=new AtomicInteger();
    private AtomicBoolean taskRunning=new AtomicBoolean();
    public void equipmentTick(){
        if(taskRunning.compareAndSet(false,true)){
            try{
                int tickCount=this.tickCount.get();
                for(var p: Bukkit.getOnlinePlayers()){
                    UUID uuid=p.getUniqueId();
                    if(equipmentFUMap.containsKey(uuid)){
                        var equipmentFU=equipmentFUMap.get(uuid);
                        for (var entry: equipmentFU.entrySet()){
                            entry.getKey().onTick(p,entry.getValue(),tickCount);
                        }
                    }
                }
            }finally {
                taskRunning.set(false);
            }
        }
    }
}
