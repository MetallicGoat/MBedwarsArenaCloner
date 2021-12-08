package me.metallicgoat.ArenaCloner;


import com.onarandombox.MultiverseCore.MultiverseCore;
import de.marcely.bedwars.api.BedwarsAPI;
import me.metallicgoat.ArenaCloner.commands.RegisterBedwarsCommands;
import me.metallicgoat.ArenaCloner.configupdater.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class Main extends JavaPlugin {

    private static Main instance;
    private static MultiverseCore multiverseCore;
    private static FileConfiguration dataYml;
    public static File dataYmlFile;

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

        loadDataYml();

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

    public static FileConfiguration getDataYml(){
        return dataYml;
    }

    private void loadDataYml(){
        String ymlName = "data.yml";

        dataYmlFile = new File(getDataFolder(), ymlName);
        if (!dataYmlFile.exists()) {
            saveResource(ymlName, false);
        }

        try {
            ConfigUpdater.update(this, ymlName, dataYmlFile, Collections.singletonList("Nothing"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataYml = new YamlConfiguration();
        try {
            dataYml.load(dataYmlFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void log(String... args) {
        for(String s : args)
            getLogger().info(s);
    }
}