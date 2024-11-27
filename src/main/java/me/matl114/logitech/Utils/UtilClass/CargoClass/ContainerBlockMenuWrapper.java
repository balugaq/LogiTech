package me.matl114.logitech.Utils.UtilClass.CargoClass;

import lombok.Getter;
import me.matl114.logitech.Utils.ContainerUtils;
import me.matl114.logitech.Utils.Debug;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ContainerBlockMenuWrapper extends BlockMenu implements Cloneable{
    public static void setup(){

    }
    private static final ContainerBlockMenuWrapper instanceTemplate=new ContainerBlockMenuWrapper();
    @Getter
    private int slotSize=0;
    protected Inventory inventory;
    protected Location location;
    //protected ItemStack[] inventorySnapShot;
    public void setup(Inventory inventory, Location location,int slotSize) {
        this.inventory = inventory;
        this.location = location;
        this.slotSize=slotSize;
    }
    public static BlockMenu getContainerBlockMenu(Inventory inv,Location loc) {
        return getContainerBlockMenu(inv,loc,inv.getSize());
    }
    public static BlockMenu getContainerBlockMenu(Inventory inv,Location loc,int slotSize) {
        ContainerBlockMenuWrapper containerBlockMenu=instanceTemplate.clone();
        containerBlockMenu.setup(inv,loc,slotSize);
        return containerBlockMenu;
    }
    private ContainerBlockMenuWrapper(){
        super(ContainerUtils.getContainerWrapperMenuPreset(),new Location(null,0,0,0));
        this.setEmptySlotsClickable(false);
        this.setPlayerInventoryClickable(false);
    }

    @Override
    protected ContainerBlockMenuWrapper clone() {
        try {
            return (ContainerBlockMenuWrapper) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("clone failed", e);
        }
    }
    @Override
    public Location getLocation() {
        return this.location;
    }
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
    @Override
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    @Override
    public void replaceExistingItem(int slot,ItemStack itemStack) {
        if(slot<this.slotSize){
            inventory.setItem(slot,itemStack);
            this.markDirty();
            //inventorySnapShot[slot]=inventory.getItem(inventory.getSize()-1);
        }
    }
    public void replaceExistingItem(int slot, ItemStack item, boolean event) {
//        if (event) {
//            ItemStack previous = this.getItemInSlot(slot);
//            item = this.preset.onItemStackChange(this, slot, previous, item);
//        }
        this.replaceExistingItem(slot, item);

    }
    @Override
    public ItemStack getItemInSlot(int slot) {
        if(slot<this.slotSize){
            return inventory.getItem(slot);
        }else return null;
    }
    @Override
    public void open(Player... players){
        throw new UnsupportedOperationException("Not supported in Container Impl.");
    }
    @Override
    public void close(){
        throw new UnsupportedOperationException("Not supported in Container Impl.");
    }
    @Override
    public boolean hasViewer() {
        //because this is not persistent instance,so call hasViewer we return true
        return true;
    }
    @Override
    public void save(Location l){
        throw new UnsupportedOperationException("Not supported in Container Impl.");
    }
    @Override
    public void reload() {
        throw new UnsupportedOperationException("Not supported in Container Impl.");
    }
    @Override
    public Block getBlock() {
        return this.location.getBlock();
    }
    @Override
    public void delete(Location l) {
        throw new UnsupportedOperationException("Not supported in Container Impl.");
    }
}