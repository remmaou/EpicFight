package yesman.epicfight.platform.fabric.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.block.state.BlockState;

/// NeoForge's BlockParticleOption carried a BlockPos. Vanilla's does not, so this subclass keeps it for locally spawned particles
/// (it is not synchronized over the network; receivers fall back to the particle's own position).
public class PositionedBlockParticleOption extends BlockParticleOption {
    private final BlockPos pos;

    public PositionedBlockParticleOption(ParticleType<BlockParticleOption> type, BlockState state, BlockPos pos) {
        super(type, state);
        this.pos = pos;
    }

    public BlockPos pos() {
        return this.pos;
    }
}
