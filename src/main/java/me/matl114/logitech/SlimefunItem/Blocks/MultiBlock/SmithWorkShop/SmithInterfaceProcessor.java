package me.matl114.logitech.SlimefunItem.Blocks.MultiBlock.SmithWorkShop;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import lombok.Getter;
import me.matl114.logitech.SlimefunItem.AddItem;
import me.matl114.logitech.Utils.*;
import me.matl114.logitech.Utils.UtilClass.ItemClass.ItemGreedyConsumer;
import me.matl114.logitech.Utils.UtilClass.RecipeClass.MultiCraftingOperation;
import me.matl114.matlib.Implements.Slimefun.core.CustomRecipeType;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class SmithInterfaceProcessor extends SmithingInterface implements MachineProcessHolder<MultiCraftingOperation> {
    @Override
    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }
    protected final int PROCESSOR_SLOT=4;
    protected final int[] OUTPUT_BORDER=new int[]{
            9,10,11,12,13,14,15,16,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53
    };
    protected final int[] OUTPUT_SLOT=new int[]{
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };
    protected final ItemStack NO_MULTIBLOCK_ITEM=new CustomItemStack(Material.RED_STAINED_GLASS_PANE,"&c待机中","&7没有接入多方块结构");
    public static enum Operation{
        //装配部件
        ASSEMBLE,
        //拆卸部件
        DISMANTLE;
        //剩下的呢?还么想好
        Operation(){

        }
    }
    @Getter
    private static final List<MachineRecipe> interfacedCrafttableRecipes=new ArrayList<>();
    public static final int CRAFT_TABLE_TICKS=0;
    public static final int ANVIL_TICKS=8;
    public static final CustomRecipeType INTERFACED_CRAFTTABLE=new CustomRecipeType(AddUtils.getNameKey("interfaced-craft-table"),AddUtils.addGlow( new  CustomItemStack(Material.CRAFTING_TABLE,"&a锻铸作坊配方","&7该配方需要在","&e\"锻造合成端口\"","&7邻接的工作台中合成"))).relatedTo((in,out)->{
        interfacedCrafttableRecipes.add(MachineRecipeUtils.shapeFrom(CRAFT_TABLE_TICKS,in,new ItemStack[]{out}));
    },(in,out)->{
        interfacedCrafttableRecipes.removeIf(i->CraftUtils.matchItemStack(out,i.getOutput()[0],true));
    });
    public static final CustomRecipeType INTERFACED_ANVIL=new CustomRecipeType(AddUtils.getNameKey("interfaced-anvil-icon"),AddUtils.addGlow( new  CustomItemStack(Material.ANVIL,"&a锻铸作坊配方","&7该配方需要在","&e\"锻造合成端口\"","&7邻接的铁砧中操作")));
    public static final CustomRecipeType INTERFACED_SMITH=new CustomRecipeType(AddUtils.getNameKey("interfaced-smith-icon"),AddUtils.addGlow( new  CustomItemStack(Material.SMITHING_TABLE,"&a锻铸作坊配方","&7该配方需要在","&e\"锻造合成端口\"","&7邻接的锻造台中操作")));
    @Getter
    private static HashSet<Function<AnvilInventory, Supplier<MultiCraftingOperation>>> anvilLogics=new LinkedHashSet<>();
    public static void registerAnvilLogic(Function<AnvilInventory, Supplier<MultiCraftingOperation>> anvilCraft) {
        anvilLogics.add(anvilCraft);
    }
    static{
        //demo
        registerAnvilLogic((anvilInventory -> {
            if(anvilInventory.getSize()>=2){
                ItemStack tool=anvilInventory.getItem(0);
                if(tool!=null){
                    int durability=tool.getDurability();
                    String fixName=anvilInventory.getRenameText();
                    boolean hasName=false;
                    if(fixName!=null&&!fixName.isEmpty()){
                        hasName=true;
                    }
                    if(durability>0||hasName){
                        ItemStack fix=anvilInventory.getItem(1);
                        if(CraftUtils.matchItemStack(fix, AddItem.ABSTRACT_INGOT,false)){
                            int supply=fix.getAmount();
                            int cost= Math.min( ( (durability+249)/250)+(hasName?1:0),supply);
                            if(cost>0){
                                final boolean applyName=hasName&&(supply>(( durability/250)+1));
                                int fixCost=applyName?(cost-1):cost;
                                //here we set repair cost for judgements
                                anvilInventory.setRepairCost(-5*cost);
                                return ()->{
                                    ItemStack toolCopy=tool.clone();
                                    tool.setAmount(0);
                                    fix.setAmount(fix.getAmount()-cost);
                                    ItemMeta meta=toolCopy.getItemMeta();
                                    if(meta instanceof Damageable dm){
                                        dm.setDamage(Math.max(durability-fixCost*250,0));
                                    }
                                    if(meta instanceof Repairable rp){
                                        if(rp.hasRepairCost()){
                                            rp.setRepairCost(0);
                                        }
                                    }
                                    if(applyName){
                                        meta.setDisplayName("§f"+fixName);
                                    }
                                    toolCopy.setItemMeta(meta);
                                    return new MultiCraftingOperation(new ItemGreedyConsumer[]{CraftUtils.getGreedyConsumerOnce(toolCopy)},8);
                                };
                            }
                        }
                    }
                }
            }//no repair cost
            anvilInventory.setRepairCost(0);
            return null;
        }));
        //INTERFACED_ANVIL.register();
    }
    @Getter
    private static HashMap<Operation,HashSet<Function<SmithingInventory, Supplier<MultiCraftingOperation>>>> smithLogics=new HashMap<>();
    public static void registerSmithLogic(Function<SmithingInventory, Supplier<MultiCraftingOperation>> smithCraft, Operation operation) {
        smithLogics.computeIfAbsent(operation,k->new LinkedHashSet<>()).add(smithCraft);
    }



    private MachineProcessor<MultiCraftingOperation> processor;
    public SmithInterfaceProcessor(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int energybuffer, int energyConsumption) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption, false);
        this.processor=new MachineProcessor<>(this);
        this.processor.setProgressBar(AddUtils.addGlow(new ItemStack(Material.CHIPPED_ANVIL)));
        setDisplayRecipes(
                Utils.list(
                        AddUtils.getInfoShow("&f机制",
                                "&7该机器需要位于锻铸工坊结构中方可运行",
                                "&7将原版工作方块(如工作台,铁砧,锻造台)等置于其左右邻接的方块",
                                "&7即可在工作方块中制作特殊的配方",
                                "&7或者执行特殊的操作",
                                "&7其结果将被输出至该机器运行",
                                "&e若是操作结果需要等待时间,则该机器需要电力以执行进程"),null,
                        INTERFACED_CRAFTTABLE.toItem(),null,
                        INTERFACED_ANVIL.toItem(),null,
                        INTERFACED_SMITH.toItem(),null

                )
        );
    }
    @Nonnull
    @Override
    public MachineProcessor<MultiCraftingOperation> getMachineProcessor() {
        return this.processor;
    }
    @Override
    public void constructMenu(BlockMenuPreset preset) {
        IntStream.range(0,9).forEach(i->preset.addItem(i, ChestMenuUtils.getBackground(),ChestMenuUtils.getEmptyClickHandler()));
        for (int i:OUTPUT_BORDER){
            preset.addItem(i,ChestMenuUtils.getOutputSlotTexture(),ChestMenuUtils.getEmptyClickHandler());
        }
    }
    public boolean acceptable(BlockMenu inv){
        if(inv==null)return false;
        if(this.processor.getOperation(inv.getLocation())!=null){
            return false;
        }
        return hasEmpty(inv);
    }
    public boolean hasEmpty(BlockMenu inv){
        for (int i:OUTPUT_SLOT){
            if(inv.getItemInSlot(i)==null){
                return true;
            }
        }
        return false;
    }

    public boolean acceptProgress(MultiCraftingOperation operation,Location location){
        BlockMenu inv= DataCache.getMenu(location);
        if(inv!=null){
            if(acceptable(inv)){
                if(operation.getTotalTicks()<=0){
                    CraftUtils.multiForcePush(operation.getResults(),inv,OUTPUT_SLOT);
                    return true;
                }
                this.processor.startOperation(location,operation);
                return true;
            }
        }
        return false;
    }
    public void processInterface(Block b, BlockMenu menu, SlimefunBlockData data, Location coreLocation,int speed) {
        //implement logic here
        Location loc=menu.getLocation();
        var a=this.processor.getOperation(loc);
        if(a!=null){
            if(a.isFinished()){
                if(hasEmpty(menu)){
                    CraftUtils.multiForcePush(a.getResults(),menu,OUTPUT_SLOT);
                    this.processor.endOperation(loc);
                    if(menu.hasViewer()){
                        menu.replaceExistingItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_NULL);
                    }
                }else {
                    if(menu.hasViewer()){
                        menu.replaceExistingItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_SPACE);
                    }
                }
            }else{
                if(conditionHandle(b,menu)){
                    progressorCost(b,menu);
                    a.progress(1+speed);
                    if(menu.hasViewer()){
                        this.processor.updateProgressBar(menu,PROCESSOR_SLOT,a);
                    }
                }else {
                    if(menu.hasViewer()){
                        menu.replaceExistingItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_ENERGY);
                    }
                }
            }
        }else{
            if(menu.hasViewer()){
                menu.replaceExistingItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_NULL);
            }
        }
    }

    @Override
    public void processFailed(Block b, BlockMenu menu, SlimefunBlockData data) {
        if(menu.hasViewer()){
            menu.replaceExistingItem(PROCESSOR_SLOT, NO_MULTIBLOCK_ITEM);
        }
    }
}
