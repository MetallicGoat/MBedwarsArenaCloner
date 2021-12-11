package me.metallicgoat.ArenaCloner.commands;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.arena.*;
import de.marcely.bedwars.api.command.CommandHandler;
import de.marcely.bedwars.api.command.SubCommand;
import de.marcely.bedwars.tools.Either;
import me.metallicgoat.ArenaCloner.CloneTools;
import me.metallicgoat.ArenaCloner.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RegisterBedwarsCommands {
    public static void RegisterCloneCommand(){
        SubCommand myCommand = BedwarsAPI.getRootCommandsCollection().addCommand("fullclone");
        if(myCommand != null) {
            myCommand.setHandler(new CommandHandler() {
                @Override
                public Plugin getPlugin() {
                    return Main.getInstance();
                }

                @Override
                public void onRegister(SubCommand subCommand) {

                }

                @Override
                public void onFire(CommandSender sender, String s, String[] args) {
                    if (sender.hasPermission("bw-cloner.admin")) {
                        if (args.length == 2) {

                            //Current arena
                            Arena arena = BedwarsAPI.getGameAPI().getArenaByName(args[0]);

                            //New arena name
                            String cloneName = args[1];

                            if (arena != null) {
                                long oldTime = System.currentTimeMillis();
                                Either<Arena, String> clonedArena = CloneTools.clonedArena(arena, cloneName, true);
                                if(clonedArena.hasLeft()) {
                                    sendMessage(sender, ChatColor.GREEN + arena.getName() + "cloned successfully after " + (System.currentTimeMillis() - oldTime) + " ms");
                                }else{
                                    sendMessage(sender, ChatColor.RED + clonedArena.right());
                                }
                            }else{
                                sendMessage(sender, ChatColor.RED + "Cloning arena not found!");
                            }
                        }else{
                            sendMessage(sender, "/bw fullclone <ArenaName> <ClonedArenaName>");
                        }
                    }
                }

                @Override
                public @Nullable List<String> onAutocomplete(CommandSender commandSender, String[] strings) {
                    return null;
                }
            });
        }
    }
    private static void sendMessage(CommandSender sender, String message){
        Main plugin = Main.getInstance();
        if (sender instanceof Player) {
            sender.sendMessage(message);
        } else {
            plugin.getLogger().info(message);
        }
    }
}
