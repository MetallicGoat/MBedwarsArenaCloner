package me.metallicgoat.ArenaCloner;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.event.arena.ArenaEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class AutoClonePrep implements Listener {
    //TODO on startup
    public static void stopArenasToClone(){
        List<String> cloneArenas;
        for (Arena arena : BedwarsAPI.getGameAPI().getArenas()){
            if(cloneArenas.contains(arena.getName())){
                //This arena is designated for cloning
                arena.setStatus(ArenaStatus.STOPPED);
            }

        }
    }

    @EventHandler
    public static void onArenaEnable(ArenaEnableEvent e){
        Arena arena = e.getArena();
        List<String> cloneArenas;
        if(cloneArenas.contains(arena.getName())){
            //This arena is designated for cloning
            e.addIssue();
        }
    }
}
