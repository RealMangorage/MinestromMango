package org.mangorage.servertest.generators;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.api.NoiseResult;
import de.articdive.jnoise.noise.perlin.PerlinNoiseBuilder;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.UnitModifier;
import org.mangorage.servertest.block.handlers.LavaBlockHandler;

import java.util.Random;

public class SimpleTerrainGenerator implements Generator {

    private final JNoise hillNoiseGenerator;
    private final JNoise caveNoiseGenerator;
    private final JNoise bedrockNoiseGenerator;

    public SimpleTerrainGenerator() {

        hillNoiseGenerator= new PerlinNoiseBuilder()
                .setSeed(System.currentTimeMillis()) // Use a dynamic seed for variation
                .setFrequency(0.01f) // Adjust frequency for cave size
                .build();
        // Create a Perlin noise generator for 3D noise
        caveNoiseGenerator = new PerlinNoiseBuilder()
                .setSeed(System.currentTimeMillis()) // Use a dynamic seed for variation
                .setFrequency(0.05f) // Adjust frequency for cave size
                .build();

        bedrockNoiseGenerator = new PerlinNoiseBuilder()
                .setSeed(System.currentTimeMillis()) // Use a dynamic seed for variation
                .setFrequency(0.1f) // Adjust frequency for cave size
                .build();
    }

    private void spawnRandomOre(UnitModifier modifier, int x, int y, int z) {
        var random = new Random();
        var result = random.nextInt(100);
        if (y < 30 && result == 2) {
            modifier.setBlock(x, y, z, Block.GOLD_BLOCK);
        }
    }

    @Override
    public void generate(GenerationUnit unit) {
        // Get the chunk bounds
        Point start = unit.absoluteStart();
        Point end = unit.absoluteEnd();
        var modifier = unit.modifier();

        // First pass: Generate the terrain (stone, grass)
        for (int x = start.blockX(); x < end.blockX(); x++) {
            for (int z = start.blockZ(); z < end.blockZ(); z++) {
                // Generate height using the hill noise generator (only use x, z for height)
                NoiseResult hillNoiseResult = hillNoiseGenerator.getNoiseResult(x, 0, z);
                double hillNoiseValue = hillNoiseResult.getPureValue();

                // Scale and offset the height noise to determine terrain height (hills and valleys)
                int terrainHeight = (int) (hillNoiseValue * 20 + 60); // Adjust multiplier for hill height

                for (int y = start.blockY(); y < end.blockY(); y++) {
                    if (y < terrainHeight) {
                        // Below the terrain height, place stone (underground)
                        modifier.setBlock(x, y, z, Block.STONE);
                        spawnRandomOre(modifier, x, y, z);
                    } else if (y == terrainHeight) {
                        // At the terrain height, place grass (on surface)
                        modifier.setBlock(x, y, z, Block.GRASS_BLOCK);
                    } else {
                        // Otherwise, place air (sky or space above ground)
                        modifier.setBlock(x, y, z, Block.AIR);
                    }
                }
            }
        }

        // Second pass: Carve out caves (replace stone with air)
        for (int x = start.blockX(); x < end.blockX(); x++) {
            for (int z = start.blockZ(); z < end.blockZ(); z++) {
                for (int y = start.blockY(); y < end.blockY(); y++) {
                    // Generate cave noise using the cave noise generator (using x, y, z for caves)
                    NoiseResult caveNoiseResult = caveNoiseGenerator.getNoiseResult(x, y, z);
                    double caveNoiseValue = caveNoiseResult.getPureValue();

                    // Carve out caves by replacing stone with air if the cave noise is low
                    if (caveNoiseValue < -0.4) { // Lowered cave threshold for clearer caves
                        // If the current block is stone, replace it with air (carve the cave)
                        modifier.setBlock(x, y, z, Block.AIR); // Carve out the cave (replace stone with air)
                    }
                }
            }
        }

        // Third Pass: // Deal with bedrock
        for (int x = start.blockX(); x < end.blockX(); x++) {
            for (int z = start.blockZ(); z < end.blockZ(); z++) {
                var wasBedrock = false;
                for (int y = start.blockY(); y < end.blockY(); y++) {
                    wasBedrock = false;
                    if (y == -64) {
                        modifier.setBlock(x, y, z, Block.BEDROCK);
                    } else if (y <= -60) {
                        if (new Random().nextInt(2) == 1) {
                            modifier.setBlock(x, y, z, Block.BEDROCK);
                            modifier.setBlock(x, y+1, z, Block.LAVA.withHandler(new LavaBlockHandler()));

                        } else {
                            modifier.setBlock(x, y, z, Block.STONE);
                        }
                    }
                }
            }
        }

    }

}