package com.github.shrekshellraiser.computer.block;

import com.github.shrekshellraiser.api.devices.IAttachableDevice;
import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.github.shrekshellraiser.ModBlocks.DEVICE_CABLE;

public class CableBlock extends Block {
    public static final VoxelShape FLAT_CABLE = Block.box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0);

    public static final BooleanProperty CABLE_NORTH;
    public static final BooleanProperty CABLE_EAST;
    public static final BooleanProperty CABLE_SOUTH;
    public static final BooleanProperty CABLE_WEST;
    public static final Map<Direction,BooleanProperty> CABLE_PROPERTIES = new HashMap<>();
    public CableBlock() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return getUpdatedState(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CABLE_NORTH);
        builder.add(CABLE_EAST);
        builder.add(CABLE_SOUTH);
        builder.add(CABLE_WEST);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return FLAT_CABLE;
    }

    private BlockState getUpdatedState(Level level, BlockPos blockPos) {
        BlockState state = defaultBlockState();
        for (Direction direction : CableBlock.CABLE_PROPERTIES.keySet() ) {
            BooleanProperty property = CableBlock.CABLE_PROPERTIES.get(direction);
            BlockPos offset = blockPos.relative(direction);
            boolean connected = level.getBlockState(blockPos.relative(direction)).is(DEVICE_CABLE);
            BlockEntity blockEntity = level.getBlockEntity(offset);
            if (blockEntity instanceof IAttachableDevice attachableDevice) {
                connected = connected || attachableDevice.cableAttaches(direction.getOpposite());
            } else if (blockEntity instanceof ComputerBlockEntity computer) {
                connected = connected || computer.cableAttaches(direction.getOpposite());
            }
            state = state.setValue(property, connected);
        }
        return state;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
        level.setBlock(blockPos, getUpdatedState(level, blockPos), 2);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }


    static {
        CABLE_NORTH = BooleanProperty.create("cable_north");
        CABLE_EAST = BooleanProperty.create("cable_east");
        CABLE_SOUTH = BooleanProperty.create("cable_south");
        CABLE_WEST = BooleanProperty.create("cable_west");
        CABLE_PROPERTIES.put(Direction.NORTH, CABLE_NORTH);
        CABLE_PROPERTIES.put(Direction.EAST, CABLE_EAST);
        CABLE_PROPERTIES.put(Direction.SOUTH, CABLE_SOUTH);
        CABLE_PROPERTIES.put(Direction.WEST, CABLE_WEST);
    }
}
