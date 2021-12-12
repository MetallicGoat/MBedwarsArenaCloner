package me.metallicgoat.ArenaCloner;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.nms.CraftSlimeWorld;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.world.importer.WorldImporter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SlimeWorldManager {

    //TODO
    private SWMPlugin slimeAPI = (SWMPlugin) Bukkit.getServer().getPluginManager().getPlugin("SlimeWorldManager");

    private @Nullable SlimeWorld getSlimeWorld(World world) {
        return this.slimeAPI.getNms().getSlimeWorld(world);
    }

    public void saveWorldAsSlimeFile(World world, File target) throws Exception {
        final SlimeLoader loader = new SingleFileSlimeLoader(target);
        SlimeWorld slimeWorld = getSlimeWorld(world);

        // is a slime world
        if (slimeWorld == null)
            slimeWorld = WorldImporter.readFromDirectory(world.getWorldFolder());

        final byte[] serializedWorld = ((CraftSlimeWorld) slimeWorld).serialize();

        loader.saveWorld(world.getName(), serializedWorld, false);
    }

    public void regenWorldFromSlimeFile(World world, File source) throws Exception {
        final SlimeLoader loader = new SingleFileSlimeLoader(source);
        final SlimeWorld slimeWorld = this.slimeAPI.loadWorld(loader, world.getName(), true, new SlimePropertyMap());

        for (Player player : world.getPlayers())
            //TODO player.teleport(BedwarsAPI.getGameAPI()..getMainWorld().getSpawnLocation());

        Bukkit.unloadWorld(world, false);

        if (Bukkit.getWorlds().contains(world))
            throw new IllegalStateException("Failed to unload the world");

        this.slimeAPI.generateWorld(slimeWorld);
    }

    private static class SingleFileSlimeLoader implements SlimeLoader {

        private final File worldFile;
        private RandomAccessFile loadedWorld = null;

        public SingleFileSlimeLoader(File worldFile) {
            this.worldFile = worldFile;
        }

        @Override
        public byte[] loadWorld(String worldName, boolean readOnly) throws UnknownWorldException, WorldInUseException, IOException {
            if (!this.worldFile.exists())
                throw new UnknownWorldException(worldName);

            final RandomAccessFile file = this.loadedWorld = new RandomAccessFile(this.worldFile, "rw");

            if (!readOnly) {
                final FileChannel channel = file.getChannel();

                try {
                    if (channel.tryLock() == null)
                        throw new WorldInUseException(worldName);
                } catch (OverlappingFileLockException ex) {
                    throw new WorldInUseException(worldName);
                }
            }

            if (file.length() > Integer.MAX_VALUE)
                throw new IndexOutOfBoundsException("World is too big!");

            final byte[] serializedWorld = new byte[(int) file.length()];

            file.seek(0); // Make sure we're at the start of the file
            file.readFully(serializedWorld);

            return serializedWorld;
        }

        @Override
        public boolean worldExists(String worldName) throws IOException {
            return this.worldFile.exists();
        }

        @Override
        public List<String> listWorlds() throws IOException {
            if (this.worldFile.exists())
                return Arrays.asList(this.worldFile.getName());
            else
                return Collections.emptyList();
        }

        @Override
        public void saveWorld(String worldName, byte[] serializedWorld, boolean lock) throws IOException {
            RandomAccessFile worldFile = this.loadedWorld;
            boolean tempFile = worldFile == null;

            if (tempFile)
                worldFile = new RandomAccessFile(this.worldFile, "rw");

            worldFile.seek(0); // Make sure we're at the start of the file
            worldFile.setLength(0); // Delete old data
            worldFile.write(serializedWorld);

            if (lock) {
                final FileChannel channel = worldFile.getChannel();

                try {
                    channel.tryLock();
                } catch (OverlappingFileLockException ignored) {
                }
            }

            if (tempFile)
                worldFile.close();
        }

        @Override
        public void unlockWorld(String worldName) throws UnknownWorldException, IOException {
            if (!this.worldFile.exists())
                throw new UnknownWorldException(worldName);

            if (this.loadedWorld != null) {
                this.loadedWorld.close();
                this.loadedWorld = null;
            }
        }

        @Override
        public boolean isWorldLocked(String worldFile) throws UnknownWorldException, IOException {
            RandomAccessFile file = this.loadedWorld;
            boolean closeOnFinish = false;

            if (file == null) {
                file = new RandomAccessFile(this.worldFile, "rw");
                closeOnFinish = true;
            }

            final FileChannel channel = file.getChannel();

            try {
                final FileLock fileLock = channel.tryLock();

                if (fileLock != null) {
                    fileLock.release();
                    return true;
                }
            } catch (OverlappingFileLockException ignored) {
            } finally {
                if (closeOnFinish)
                    file.close();
            }

            return false;
        }

        @Override
        public void deleteWorld(String worldName) throws UnknownWorldException, IOException {
            if (!this.worldFile.exists())
                throw new UnknownWorldException(worldName);

            this.worldFile.delete();
        }
    }
}