package org.mangorage.servertest.generators;

import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GeneratorList implements Generator {
    private final List<Generator> generators = new ArrayList<>();

    public GeneratorList add(Generator generator) {
        this.generators.add(generator);
        return this;
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        this.generators.forEach(g -> g.generate(unit));
    }
}
