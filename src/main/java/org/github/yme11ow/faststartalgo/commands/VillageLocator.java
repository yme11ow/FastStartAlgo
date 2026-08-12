package org.github.yme11ow.faststartalgo.commands;

import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.SpiralIterator;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mcfeature.structure.RegionStructure;
import com.seedfinding.mcfeature.structure.Village;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.TerrainGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class VillageLocator implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args){
        int StartX = 0;
        int StartY =0;
        int StartZ = 0;
        long seed = 0L;

        // Get starting coordinates, via player coordinates or directly stated in the command
        if (args.length == 0){
            if (!(sender instanceof Player)){
                sender.sendMessage("You must specify coordinates when sending commands from the console!");
                return true;
            }

            Player player = (Player) sender;
            Location loc = player.getLocation();

            StartX = loc.getBlockX();
            StartY = loc.getBlockY();
            StartZ = loc.getBlockZ();

            seed = player.getWorld().getSeed();
        } else if (args.length == 3){
            try {
                StartX = Integer.parseInt(args[0]);
                StartY = Integer.parseInt(args[1]);
                StartZ = Integer.parseInt(args[2]);

                World world = Bukkit.getWorld("world");
                if (world != null){
                    seed = world.getSeed();
                }

            } catch (NumberFormatException e){
                sender.sendMessage("Coordinates must be numbers! Hint: try putting your username before the coordinates");
                return true;
            }
        } else {
            sender.sendMessage("Usage: /VillageLocator [x y z]");
            return true;
        }

        // Village Locator logic here
        RegionStructure<?, ?> village = new Village(MCVersion.v1_8_8);
        // RegionStructure<?, ?> regionStructure = village;
        int chunkInRegion = village.getSpacing();
        int regionSize = chunkInRegion * 16;

        final int border = 30_000_000;

        SpiralIterator<RPos> spiralIterator = new SpiralIterator<>(new BPos(StartX, StartY, StartZ).toRegionPos(regionSize), new BPos(-border, 0, -border).toRegionPos(regionSize), new BPos(border, 0, border).toRegionPos(regionSize), 1, (x, y, z) -> new RPos(x, z, regionSize));

        long finalSeed = seed;
        ChunkRand chunkRand = new ChunkRand();
        BiomeSource source = BiomeSource.of(Dimension.OVERWORLD, MCVersion.v1_8_8, finalSeed);
        TerrainGenerator terrainGenerator = TerrainGenerator.of(source);
        int maxResults = 5;
        List<BPos> result = new ArrayList<>();
        StreamSupport.stream(spiralIterator.spliterator(), false).map(rPos -> village.getInRegion(finalSeed, rPos.getX(), rPos.getZ(), chunkRand)).filter(Objects::nonNull).filter(cPos -> (village.canSpawn(cPos, source)) && (terrainGenerator == null || village.canGenerate(cPos, terrainGenerator))).limit(maxResults).forEach(cPos -> result.add(cPos.toBlockPos().add(9, 0,9)));

        if (result.isEmpty()){
            sender.sendMessage("No villages found :(");
        } else {
            sender.sendMessage("Found " + result.size() + " village(s):");

            for (BPos pos : result) {
                sender.sendMessage("X: " + pos.getX() + " Z: " + pos.getZ());
            }
        }
        return true;
    }
}
