package me.metallicgoat.ArenaCloner;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class AutoCloneArenas implements Listener{

    //Do we stop arenas before they start to regenerate?
    //Do we need to reset Integer values?
    //Do we remove arenas after use immediately?
    HashMap<Arena, Integer> activeArenaClones = new HashMap<>();
    
    //TODO Right event? 
    
    @EventHandler
    public void onRoundStart(RoundStartEvent e){
        if(getUsedArenaToAllArenasRatio() > somenumberfromconfig){
            //chose best arena to clone
        }
    }
    
    private double getUsedArenaToAllArenasRatio(){

        //Instead, maybe subtract players not in a game, to the amount oof free spots in arenas in LOBBY

        float usedArenas = 0;
        float arenas = 0;
        for(Arena arena:BedwarsAPI.getGameAPI().getArenas()){
            if(arena.getStatus() == ArenaStatus.LOBBY){
                arenas++;
            }
        }
        return (usedArenas/arenas) * 100;
    }
}
