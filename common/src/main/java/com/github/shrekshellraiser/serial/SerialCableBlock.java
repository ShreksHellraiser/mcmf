package com.github.shrekshellraiser.serial;

import com.github.shrekshellraiser.api.serial.ISerialPeer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SerialCableBlock extends Block {
    public static final BooleanProperty CABLE_NORTH;
    public static final BooleanProperty CABLE_EAST;
    public static final BooleanProperty CABLE_SOUTH;
    public static final BooleanProperty CABLE_WEST;
    public static final BooleanProperty CABLE_UP;
    public static final BooleanProperty CABLE_DOWN;
    public static final BooleanProperty CONNECTOR_NORTH;
    public static final BooleanProperty CONNECTOR_EAST;
    public static final BooleanProperty CONNECTOR_SOUTH;
    public static final BooleanProperty CONNECTOR_WEST;
    public static final Map<Direction,BooleanProperty> CABLE_PROPERTIES = new HashMap<>();
    public static final Map<Direction,BooleanProperty> CONNECTOR_PROPERTIES = new HashMap<>();
    public SerialCableBlock() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SerialCableShapes.getShape(blockState);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
        level.setBlock(blockPos, getUpdatedState(level, blockPos), 2);
    }
    private BlockState getUpdatedState(Level level, BlockPos blockPos) {
        BlockState state = defaultBlockState();
        for (Direction direction : CABLE_PROPERTIES.keySet() ) {
            BlockPos newPos = blockPos.relative(direction);
            BooleanProperty property = CABLE_PROPERTIES.get(direction);
            boolean connected = level.getBlockState(newPos).is(this);
            boolean connector = false;
            if (level.getBlockEntity(newPos) instanceof ISerialPeer) {
                connector = true;
            }
            state = state.setValue(property, connected || connector);
            BooleanProperty connectorProperty = CONNECTOR_PROPERTIES.get(direction);
            if (connectorProperty != null) {
                state = state.setValue(CONNECTOR_PROPERTIES.get(direction), connector);
            }
        }
        return state;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return getUpdatedState(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CABLE_NORTH);
        builder.add(CABLE_EAST);
        builder.add(CABLE_SOUTH);
        builder.add(CABLE_WEST);
        builder.add(CABLE_UP);
        builder.add(CABLE_DOWN);
        builder.add(CONNECTOR_NORTH);
        builder.add(CONNECTOR_EAST);
        builder.add(CONNECTOR_SOUTH);
        builder.add(CONNECTOR_WEST);
    }

    static {
        CABLE_NORTH = BooleanProperty.create("cable_north");
        CABLE_EAST = BooleanProperty.create("cable_east");
        CABLE_SOUTH = BooleanProperty.create("cable_south");
        CABLE_WEST = BooleanProperty.create("cable_west");
        CABLE_UP = BooleanProperty.create("cable_up");
        CABLE_DOWN = BooleanProperty.create("cable_down");
        CABLE_PROPERTIES.put(Direction.NORTH, CABLE_NORTH);
        CABLE_PROPERTIES.put(Direction.EAST, CABLE_EAST);
        CABLE_PROPERTIES.put(Direction.SOUTH, CABLE_SOUTH);
        CABLE_PROPERTIES.put(Direction.WEST, CABLE_WEST);
        CABLE_PROPERTIES.put(Direction.UP, CABLE_UP);
        CABLE_PROPERTIES.put(Direction.DOWN, CABLE_DOWN);
        CONNECTOR_NORTH = BooleanProperty.create("connector_north");
        CONNECTOR_EAST = BooleanProperty.create("connector_east");
        CONNECTOR_SOUTH = BooleanProperty.create("connector_south");
        CONNECTOR_WEST = BooleanProperty.create("connector_west");
        CONNECTOR_PROPERTIES.put(Direction.NORTH, CONNECTOR_NORTH);
        CONNECTOR_PROPERTIES.put(Direction.EAST, CONNECTOR_EAST);
        CONNECTOR_PROPERTIES.put(Direction.SOUTH, CONNECTOR_SOUTH);
        CONNECTOR_PROPERTIES.put(Direction.WEST, CONNECTOR_WEST);
    }
}
