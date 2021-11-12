package me.metallicgoat.ArenaCloner.commands;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.arena.*;
import de.marcely.bedwars.api.exception.ArenaBuildException;
import de.marcely.bedwars.api.game.spawner.Spawner;
import de.marcely.bedwars.api.world.WorldStorage;
import de.marcely.bedwars.api.world.hologram.HologramControllerType;
import de.marcely.bedwars.api.world.hologram.HologramEntity;
import me.metallicgoat.ArenaCloner.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (sender.hasPermission("bw-cloner.admin")) {
            if (args.length == 2) {

                //Current arena
                Arena arena = BedwarsAPI.getGameAPI().getArenaByName(args[0]);

                //New arena name
                String cloned = args[1];

                if (arena != null) {
                    World world = arena.getGameWorld();
                    if (arena.getStatus() == ArenaStatus.LOBBY
                            && arena.getRegenerationType() != RegenerationType.VOTING
                            && world != null) {
                        long oldTime = System.currentTimeMillis();
                        sendMessage(sender, ChatColor.GREEN + "Cloning Arena...");

                        //Clone world with multiverse
                        if(Main.getMultiverseCore().getCore().getMVWorldManager().getMVWorld(cloned) == null) {
                            if(!Main.getMultiverseCore().getCore().getMVWorldManager().cloneWorld(Main.getMultiverseCore().getCore().getMVWorldManager().getMVWorld(world).getName(), cloned)){
                                sendMessage(sender, ChatColor.RED + "Error! There is likely already a folder in your main server directory called '" + cloned + "'");
                                return true;
                            }
                        }else{
                            sendMessage(sender, ChatColor.RED + "There is already a MV World with the name \"" + cloned + "\"");
                            return true;
                        }
                        //Start duplicating arena
                        ArenaBuilder arenaBuilder = BedwarsAPI.getGameAPI().createArena();
                        //get cloned world
                        World clonedWorld = Main.getMultiverseCore().getCore().getMVWorldManager().getMVWorld(cloned).getCBWorld();
                        //set name
                        arenaBuilder.setName(cloned);
                        //Set regen type
                        arenaBuilder.setRegenerationType(arena.getRegenerationType());
                        //Set arena world
                        arenaBuilder.setWorld(clonedWorld);
                        //Set arena borders
                        if(arena.getRegenerationType() == RegenerationType.REGION){
                            arenaBuilder.setLocation1(arena.getMinRegionCorner());
                            arenaBuilder.setLocation2(arena.getMinRegionCorner());
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
                            sendMessage(sender, ChatColor.GREEN + "Arena cloned successfully in " + (System.currentTimeMillis() - oldTime) + "ms!");

                        } catch (ArenaBuildException e) {
                            e.printStackTrace();
                            sendMessage(sender,  ChatColor.RED + "Arena cloning failed after " + (System.currentTimeMillis() - oldTime) + "ms!");
                        }
                    } else {
                        sendMessage(sender, ChatColor.RED + "You can only clone arenas in Lobby status.");
                    }
                }else{
                    sendMessage(sender, ChatColor.RED + "Cloning arena not found!");
                }
            }else{
                sendMessage(sender, "/bw-cloner <ArenaName> <ClonedArenaName>");
            }
        }
        return true;
    }
    private void sendMessage(CommandSender sender, String message){
        Main plugin = Main.getInstance();
        if (sender instanceof Player) {
            sender.sendMessage(message);
        } else {
            plugin.getLogger().info(message);
        }
    }
}
