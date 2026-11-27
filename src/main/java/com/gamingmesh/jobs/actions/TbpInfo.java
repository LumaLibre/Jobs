package com.gamingmesh.jobs.actions;

import com.gamingmesh.jobs.container.ActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import dev.jsinco.brewery.api.brew.BrewQuality;

public class TbpInfo implements ActionInfo {
    private final String recipeName;
    private final BrewQuality quality;

    public TbpInfo(String recipeName, BrewQuality quality) {
        this.recipeName = recipeName;
        this.quality = quality;
    }

    @Override
    public String getName() {
        return recipeName;
    }

    @Override
    public String getNameWithSub() {
        return recipeName + "/" + quality;
    }

    @Override
    public ActionType getType() {
        return ActionType.TBP;
    }
}
