package me.metallicgoat.ArenaCloner;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.arena.*;
import de.marcely.bedwars.api.exception.ArenaBuildException;
import de.marcely.bedwars.api.game.spawner.Spawner;
import de.marcely.bedwars.api.world.WorldStorage;
import de.marcely.bedwars.api.world.hologram.HologramControllerType;
import de.marcely.bedwars.api.world.hologram.HologramEntity;
import de.marcely.bedwars.tools.Either;
import org.bukkit.Location;
import org.bukkit.World;

public class CloneTools {

    static final MVWorldManager mvWorldManager = Main.getMultiverseCore().getMVWorldManager();

    public static Either<Arena, String> clonedArena(Arena arena, String cloneName, long oldTime, boolean permanentArena) {
        World world = arena.getGameWorld();
        if (arena.getStatus() == ArenaStatus.STOPPED
                && arena.getRegenerationType() != RegenerationType.VOTING
                && world != null) {
            //sendMessage(sender, ChatColor.GREEN + "Cloning Arena...");

            //Clone world with multiverse
            if(mvWorldManager.getMVWorld(cloneName) == null) {
                if(!mvWorldManager.cloneWorld(mvWorldManager.getMVWorld(world).getName(), cloneName)){
                    //sendMessage(sender, ChatColor.RED + "Error! There is likely already a folder in your main server directory called '" + cloned + "'");
                    return Either.right("Error! There is likely already a folder in your main server directory called '" + cloneName + "'");
                }
            }else{
                //sendMessage(sender, ChatColor.RED + "There is already a MV World with the name \"" + cloneName + "\"");
                return Either.right("There is already a MV World with the name '" + cloneName + "'");
            }
            //Start duplicating arena
            ArenaBuilder arenaBuilder = BedwarsAPI.getGameAPI().createArena();
            //get cloned world
            World clonedWorld = mvWorldManager.getMVWorld(cloneName).getCBWorld();
            //set name
            arenaBuilder.setName(cloneName);
            //Set regen type
            arenaBuilder.setRegenerationType(arena.getRegenerationType());
            //Set arena world
            arenaBuilder.setWorld(clonedWorld);
            //Set arena borders
            if(arena.getRegenerationType() == RegenerationType.REGION){
                arenaBuilder.setLocation1(arena.getMinRegionCorner());
                arenaBuilder.setLocation2(arena.getMaxRegionCorner());
            }

            //Clone arena data
            try {
                //Register arena
                Arena clonedArena = arenaBuilder.finish();

                //Clone holograms
                WorldStorage worldStorage = BedwarsAPI.getWorldStorage(world);
                WorldStorage clonedWorldStorage = BedwarsAPI.getWorldStorage(clonedWorld);
                if(worldStorage != null && clonedWorldStorage != null){
                    for (HologramEntity hologramEntity:worldStorage.getHolograms()){
                        if(hologramEntity.getControllerType() == HologramControllerType.DEALER
                                || hologramEntity.getControllerType() == HologramControllerType.UPGRADE_DEALER
                                || hologramEntity.getControllerType() == HologramControllerType.TEAM_SELECTOR) {
                            Location spawnLocation = hologramEntity.getSpawnLocation();
                            spawnLocation.setWorld(clonedWorld);
                            clonedWorldStorage.spawnHologram(hologramEntity.getControllerType(), spawnLocation);
                        }
                    }
                }

                //Set Lobby Location
                clonedArena.setLobbyLocation(arena.getLobbyLocation());

                //Add Spawners
                for(Spawner spawner:arena.getSpawners()){
                    clonedArena.addSpawner(spawner.getLocation(), spawner.getDropType());
                }

                //Enable teams & set beds/spawns
                for(Team team : arena.getEnabledTeams()){
                    clonedArena.setTeamEnabled(team, true);
                    clonedArena.setBedLocation(team, arena.getBedLocation(team));
                    clonedArena.setTeamSpawn(team, arena.getTeamSpawn(team));
                }

                //Set min players
                clonedArena.setMinPlayers(arena.getMinPlayers());

                //Set players per team
                clonedArena.setPlayersPerTeam(arena.getMaxPlayers() / arena.getEnabledTeams().size());

                //Set icon
                clonedArena.setIcon(clonedArena.getIcon());

                //Enable
                clonedArena.setStatus(ArenaStatus.LOBBY);

                //Complete
                return Either.left(clonedArena);
                //sendMessage(sender, ChatColor.GREEN + "Arena cloned successfully in " + (System.currentTimeMillis() - oldTime) + "ms!");

            } catch (ArenaBuildException e) {
                e.printStackTrace();
                //sendMessage(sender,  ChatColor.RED + "Arena cloning failed after " + (System.currentTimeMillis() - oldTime) + "ms!");
                return Either.right("Arena cloning failed after " + (System.currentTimeMillis() - oldTime) + "ms!");
            }
        } else {
            return Either.right("You can only clone arenas in Lobby status.");
            //sendMessage(sender, ChatColor.RED + "You can only clone arenas in Lobby status.");
        }
    }
}
