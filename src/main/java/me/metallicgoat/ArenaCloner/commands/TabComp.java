package me.metallicgoat.ArenaCloner.commands;

import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TabComp implements org.bukkit.command.TabCompleter {

    List<String> arguments = new ArrayList<>();

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command c, @NotNull String s, String[] args) {

        if (this.arguments.isEmpty() &&
                sender.hasPermission("bw-cloner.admin")) {
            for(Arena arena: GameAPI.get().getArenas()){
                arguments.add(arena.getName());
            }
        }

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String a : this.arguments) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(a);
                }
            }
            return result;
        }

        return null;
    }
}
