package me.matl114.logitech.core.Items.Abstracts;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class CustomProps extends CustomItemWithHandler<ItemUseHandler> {
    public CustomProps(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe){
        super(itemGroup, item, recipeType, recipe);
    }
    public CustomProps(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, List<ItemStack> displayes){
        super(itemGroup, item, recipeType, recipe);
        setDisplayRecipes(displayes);
    }
    /**
     * used for clickAction items
     */
    public ItemUseHandler[] getItemHandler(){
        return new ItemUseHandler[]{(ItemUseHandler) this::onClickAction};
    }
    public abstract void onClickAction(PlayerRightClickEvent event);
    public void preRegister(){
        super.preRegister();

    }

}
