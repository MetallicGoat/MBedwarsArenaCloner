package me.metallicgoat.ArenaCloner;


import com.onarandombox.MultiverseCore.MultiverseCore;
import me.metallicgoat.ArenaCloner.commands.Commands;
import me.metallicgoat.ArenaCloner.commands.TabComp;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private static MultiverseCore multiverseCore;

    public void onEnable() {
        //int pluginId = 11928;
        //Metrics metrics = new Metrics(this, pluginId);

        instance = this;
        registerCommands();

        multiverseCore = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        PluginDescriptionFile pdf = this.getDescription();

        log(
                "------------------------------",
                pdf.getName() + " For MBedwars",
                "By: " + pdf.getAuthors(),
                "Version: " + pdf.getVersion(),
                "------------------------------"
        );
    }
    private void registerCommands() {
        PluginCommand command = getCommand("bw-cloner");
        if(command != null) {
            command.setExecutor(new Commands());
            command.setTabCompleter(new TabComp());
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public static MultiverseCore getMultiverseCore() {
        return multiverseCore;
    }

    private void log(String... args) {
        for(String s : args)
            getLogger().info(s);
    }
}