package me.metallicgoat.ArenaCloner;

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
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.List;

public class CloneTools {

    public static Either<Arena, String> clonedArena(Arena arena, String cloneName, boolean permanentArena) throws IOException {
        FileConfiguration dataYml = Main.getDataYml();
        List<String> activeArenas = dataYml.getStringList("Active-Arenas");
        activeArenas.add(cloneName);
        dataYml.set("Active-Arenas", activeArenas);
        dataYml.save(Main.dataYmlFile);

        World world = arena.getGameWorld();
        if (arena.getStatus() == ArenaStatus.STOPPED
                && arena.getRegenerationType() != RegenerationType.VOTING
                && world != null) {
            long oldTime = System.currentTimeMillis();
            //sendMessage(sender, ChatColor.GREEN + "Cloning Arena...");

            //Clone world with multiverse
            if(Main.getMultiverseCore().getCore().getMVWorldManager().getMVWorld(cloneName) == null) {
                if(!Main.getMultiverseCore().getCore().getMVWorldManager().cloneWorld(Main.getMultiverseCore().getCore().getMVWorldManager().getMVWorld(world).getName(), cloneName)){
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
            World clonedWorld = Main.getMultiverseCore().getCore().getMVWorldManager().getMVWorld(cloneName).getCBWorld();
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

                //Complete Message
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

    public static void deleteClonedArena(Arena arena){
        //Delete Arena
        //Delete Multiverse/SWM world
        //Delete world folder
        //Remove from data file
        //Save data file
    }
}
