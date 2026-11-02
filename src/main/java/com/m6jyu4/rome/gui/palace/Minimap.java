package com.m6jyu4.rome.gui.palace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;

public class Minimap {

//    private PalaceScreen screen;
//
//
//    Minimap(PalaceScreen screen){
//        this.screen = screen;
//    }
//
//
//








    public void renderStuff(GuiGraphics guiGraphics){
        guiGraphics.enableScissor(Layout.MAP_X_MIN, Layout.MAP_Y_MIN, Layout.MAP_X_MAX, Layout.MAP_Y_MAX);
        /* STUFF */
        renderBuildings();


        /* END OF STUFF */
        guiGraphics.disableScissor();
    }

    private void renderBuildings(){


        // get building list and pos
        // render them lol
    }

//    private void setBuildingDrawPos(){
//        for(BuildingEntry entry : )
//    }
//
//    private Direction setDirection(){
//        return switch(this.screen.getMinecraft().player.getDirection()) {
//            case Direction.EAST  -> ;
//            case Direction.SOUTH -> ;
//            case Direction.WEST  -> ;
//            default -> ;
//        };
//    }












}
