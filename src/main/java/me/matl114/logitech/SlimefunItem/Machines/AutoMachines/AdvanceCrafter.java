package me.matl114.logitech.SlimefunItem.Machines.AutoMachines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.matl114.logitech.SlimefunItem.Machines.AbstractAdvancedProcessor;
import me.matl114.logitech.Utils.RecipeSupporter;
import me.matl114.logitech.Utils.UtilClass.RecipeClass.ImportRecipes;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdvanceCrafter  {
    public List<ItemStack> displayedMemory = null;
    protected final RecipeType[] craftType;
    public AdvanceCrafter(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, Material progressItem,
                         int energybuffer, int energyConsumption,
                          RecipeType... craftType) {
        //super(category,item,recipeType,recipe,progressItem,energybuffer,energyConsumption,null);
        this.craftType = craftType;
    }



}
