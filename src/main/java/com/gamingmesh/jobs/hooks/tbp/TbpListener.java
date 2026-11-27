package com.gamingmesh.jobs.hooks.tbp;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.TbpInfo;
import com.gamingmesh.jobs.container.JobsPlayer;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.recipe.RecipeMatcher;
import dev.jsinco.brewery.api.recipe.RecipeMatcherResult;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryExtractEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TbpListener implements Listener {

    private static final Key KEY = Key.key("jobs", "tbp/last_action");

    private final TheBrewingProjectApi api;

    public TbpListener(TheBrewingProjectApi api) {
        this.api = api;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCauldronExtract(CauldronExtractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Optional<Brew> brewOptional = api.getBrewManager().fromItem(event.getItemResult().get());
        if (!brewOptional.isPresent()) {
            return;
        }
        ItemStack override = handleBrew(player, brewOptional.get(), "cauldron");
        if (override != null) {
            event.setItemResult(override);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBarrelExtract(BarrelExtractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        ItemSource itemSource = event.getTransactionSession().getResult();
        if (itemSource == null) {
            return;
        }
        Optional<Brew> brewOptional = api.getBrewManager().fromItem(itemSource.get());
        if (!brewOptional.isPresent()) {
            return;
        }
        ItemStack override = handleBrew(player, brewOptional.get(), "barrel");
        if (override != null) {
            event.getTransactionSession().setResult(new ItemSource.ItemBasedSource(override));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDistilleryExtract(DistilleryExtractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        ItemSource itemSource = event.getTransactionSession().getResult();
        if (itemSource == null) {
            return;
        }
        Optional<Brew> brewOptional = api.getBrewManager().fromItem(itemSource.get());
        if (!brewOptional.isPresent()) {
            return;
        }
        ItemStack override = handleBrew(player, brewOptional.get(), "distillery");
        if (override != null) {
            event.getTransactionSession().setResult(new ItemSource.ItemBasedSource(override));
        }
    }

    private @Nullable ItemStack handleBrew(Player player, Brew brew, String source) {
        JobsPlayer jPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        if (jPlayer == null) {
            return null;
        }
        RecipeMatcher<ItemStack> recipeMatcher = api.getBrewManager().matcher()
                .build();
        RecipeMatcherResult<ItemStack> result = recipeMatcher.match(brew);
        if (!result.recipeMatch().isPresent() || !result.quality().isPresent() || !result.score().completed()) {
            return null;
        }
        String lastAction = brew.meta(KEY, MetaDataType.STRING);
        if (source.equalsIgnoreCase(lastAction)) {
            return null;
        }
        Jobs.action(jPlayer, new TbpInfo(result.recipeMatch().get().getRecipeName(), result.quality().get()));
        return api.getBrewManager().toItem(
                brew.withMeta(KEY, MetaDataType.STRING, source), new Brew.State.Other()
        );
    }
}
