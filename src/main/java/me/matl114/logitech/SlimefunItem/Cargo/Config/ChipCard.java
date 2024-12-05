package me.matl114.logitech.SlimefunItem.Cargo.Config;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.matl114.logitech.SlimefunItem.CustomSlimefunItem;
import me.matl114.logitech.SlimefunItem.DistinctiveCustomItemStack;
import org.bukkit.inventory.ItemStack;

public class ChipCard extends DistinctiveCustomItemStack {
    public ChipCard(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe){
        super(itemGroup, item, recipeType, recipe);
    }
}
