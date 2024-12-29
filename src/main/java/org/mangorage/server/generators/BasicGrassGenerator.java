package org.mangorage.server.generators;

import de.articdive.jnoise.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BasicGrassGenerator implements Generator {
    private final JNoise noise;
    private final Random random = new Random();

    public BasicGrassGenerator(long seed) {
        this.noise = JNoise.newBuilder()
                .superSimplex()
                .setFrequency(0.01) // Low frequency for smooth terrain
                .setSeed(seed)
                .build();
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point start = unit.absoluteStart();
        for (int x = 0; x < unit.size().x(); x++) {
            for (int z = 0; z < unit.size().z(); z++) {
                Point bottom = start.add(x, 0, z);

                synchronized (noise) { // Synchronization is necessary for JNoise
                    double height = noise.getNoise(bottom.x(), bottom.z()) * 16;
                    // * 16 means the height will be between -16 and +16

                    unit.modifier()
                            .fill(
                                    bottom,
                                    bottom
                                            .add(1, 0, 1)
                                            .withY(height),
                                    Block.STONE
                            );

                    unit.modifier()
                            .fill(
                                    bottom
                                            .add(0, 0, 0)
                                            .withY(height),
                                    bottom
                                            .add(1, 0, 1)
                                            .withY(height + 1),
                                    Block.GRASS_BLOCK
                            );

                    // Randomly decide if a tree should be placed here
                    if (random.nextInt(4000) < 5) { // 5% chance for a tree to generate
                        generateTree(unit, bottom.withY(height+1));
                    }
                    // Randomly place ores
                    placeRandomOres(
                            unit,
                            bottom
                                    .withY(
                                            random.nextDouble(-64, height - 10)
                                    )
                            );

                    // Randomly decide to generate water or lava lake
                    if (random.nextInt(1000) < 2) { // 2% chance to generate a lake
                        generateGlob(unit, bottom.withY(height - 5)); // Place globs a few blocks underground
                    }
                }
            }
        }
    }

    // Method to generate random "globs" underground
    private void generateGlob(GenerationUnit unit, Point position) {
        // Random size of the glob (between 3x3 and 7x7 blocks)
        int globSize = 3 + random.nextInt(5);  // Random glob size between 3 and 7 blocks in radius

        // Loop to place blocks in a cluster (glob) pattern
        for (int dx = -globSize; dx <= globSize; dx++) {
            for (int dz = -globSize; dz <= globSize; dz++) {
                // Create a spherical shape for the glob
                if (Math.abs(dx) + Math.abs(dz) <= globSize) {
                    // Randomly decide if the block at this position should be part of the glob
                    if (random.nextInt(100) < 70) { // 70% chance to place a block for the glob
                        Point globPos = position.add(dx, 0, dz);

                        // Check if the position is within the chunk bounds before setting blocks
                        if (isInChunkBounds(unit, globPos)) {
                            // Create a "glob" using random block types, e.g., ores or stone
                            Block globBlock = random.nextInt(100) < 50 ? Block.WATER : Block.LAVA; // Example: random between stone and coal ore
                            unit.modifier().setBlock(globPos.withY(position.y()), globBlock);
                        }
                    }
                }
            }
        }
    }

    // Method to randomly place ores
    private void placeRandomOres(GenerationUnit unit, Point position) {
        // Random chance for different ores
        int chance = random.nextInt(100);

        // 10% chance for Coal Ore
        if (chance < 10) {
            if (isInChunkBounds(unit, position)) {
                unit.modifier().setBlock(position, Block.COAL_ORE);
            }
        }
        // 5% chance for Iron Ore
        else if (chance < 15) {
            if (isInChunkBounds(unit, position)) {
                unit.modifier().setBlock(position, Block.IRON_ORE);
            }
        }
        // 3% chance for Gold Ore
        else if (chance < 2) {
            if (isInChunkBounds(unit, position)) {
                unit.modifier().setBlock(position, Block.GOLD_ORE);
            }
        }
        // 2% chance for Diamond Ore
        else if (chance < 20) {
            if (isInChunkBounds(unit, position)) {
                unit.modifier().setBlock(position, Block.DIAMOND_ORE);
            }
        }
    }

    private void generateTree(GenerationUnit unit, Point position) {
        // Get the start point of the chunk and the size (bounds)
        Point chunkStart = unit.absoluteStart();
        Point chunkSize = unit.size();

        // Random tree height (fixed to 5 blocks tall)
        int trunkHeight = 5; // Trunk height is fixed to 5 blocks

        // Create the trunk (log)
        for (int i = 0; i < trunkHeight; i++) {
            Point trunkPos = position.add(0, i, 0); // Trunk is a single column at (x, y, z)

            // Check if the trunk position is within the chunk bounds
            if (isInChunkBounds(unit, trunkPos)) {
                unit.modifier().setBlock(trunkPos, Block.OAK_LOG);
            }
        }

        // Create the foliage (leaves), around the top 2 logs of the trunk (y=4 and y=5)
        int leafRadius = 1; // Leaves will surround the top logs (at y=4 and y=5)

        // Leaf spread around the top 2 logs (y=4 and y=5)
        for (int dx = -leafRadius; dx <= leafRadius; dx++) {
            for (int dz = -leafRadius; dz <= leafRadius; dz++) {
                // Restrict the leaf spread to a 3x3 area around the top of the trunk
                if (Math.abs(dx) + Math.abs(dz) <= leafRadius) {
                    for (int dy = 4; dy <= 5; dy++) {
                        Point leafPos = position.add(dx, dy, dz);

                        // Check if the leaf position is within the chunk bounds
                        if (isInChunkBounds(unit, leafPos)) {
                            unit.modifier().setBlock(leafPos, Block.OAK_LEAVES);
                        }
                    }
                }
            }
        }
    }

    // Helper method to check if a position is within the chunk bounds
    private boolean isInChunkBounds(GenerationUnit unit, Point position) {
        Point chunkStart = unit.absoluteStart();
        Point chunkSize = unit.size();

        return position.x() >= chunkStart.x() && position.x() < chunkStart.x() + chunkSize.x() &&
                position.z() >= chunkStart.z() && position.z() < chunkStart.z() + chunkSize.z();
    }
}
