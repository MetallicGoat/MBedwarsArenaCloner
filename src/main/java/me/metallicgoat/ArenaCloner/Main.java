package me.metallicgoat.ArenaCloner;


import com.onarandombox.MultiverseCore.MultiverseCore;
import de.marcely.bedwars.api.BedwarsAPI;
import me.metallicgoat.ArenaCloner.commands.RegisterBedwarsCommands;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private static MultiverseCore multiverseCore;

    public void onEnable() {

        instance = this;

        PluginDescriptionFile pdf = this.getDescription();

        log(
                "------------------------------",
                pdf.getName() + " For MBedwars",
                "By: " + pdf.getAuthors(),
                "Version: " + pdf.getVersion(),
                "------------------------------"
        );

        if(Bukkit.getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
            multiverseCore = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        }else{
            log("This extension of MBedwars requires Multiverse-Core to function. Disabling...");
            this.setEnabled(false);
        }

        BedwarsAPI.onReady(RegisterBedwarsCommands::RegisterCloneCommand);
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