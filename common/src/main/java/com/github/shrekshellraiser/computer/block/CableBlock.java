package com.github.shrekshellraiser.computer.block;

import com.github.shrekshellraiser.api.devices.IAttachableDevice;
import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

import static com.github.shrekshellraiser.ModBlocks.CABLE_BLOCK;
import static com.github.shrekshellraiser.ModBlocks.DEVICE_CABLE;

public class CableBlock extends Block {
    public static final BooleanProperty CABLE_NORTH;
    public static final BooleanProperty CABLE_EAST;
    public static final BooleanProperty CABLE_SOUTH;
    public static final BooleanProperty CABLE_WEST;
    public static final BooleanProperty CONNECTOR_NORTH;
    public static final BooleanProperty CONNECTOR_EAST;
    public static final BooleanProperty CONNECTOR_SOUTH;
    public static final BooleanProperty CONNECTOR_WEST;
    private static final BooleanProperty CONNECTOR_EXTENDED;
    public static final DirectionProperty WALL;
    public static final Map<Direction,BooleanProperty> CABLE_PROPERTIES = new HashMap<>();
    public static final Map<Direction,BooleanProperty> CONNECTOR_PROPERTIES = new HashMap<>();
    public CableBlock() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return getUpdatedState(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), null, blockPlaceContext.getClickedFace().getOpposite());
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CABLE_NORTH);
        builder.add(CABLE_EAST);
        builder.add(CABLE_SOUTH);
        builder.add(CABLE_WEST);
        builder.add(CONNECTOR_NORTH);
        builder.add(CONNECTOR_EAST);
        builder.add(CONNECTOR_SOUTH);
        builder.add(CONNECTOR_WEST);
        builder.add(WALL);
        builder.add(CONNECTOR_EXTENDED);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return CableShapes.getShape(blockState);
    }

    public static boolean cableOnFloor(BlockState state) {
        for (BooleanProperty direction : CABLE_PROPERTIES.values()) {
            if (state.getValue(direction)) return true;
        }
        return false;
    }

    private static boolean isSolidFloor(BlockPos blockPos, Level level) {
        return level.getBlockState(blockPos).isFaceSturdy(level, blockPos, Direction.UP);
    }

    private BlockState checkDiagonals(BlockState state, Level level, BlockPos blockPos, @Nullable BlockPos source) {
        boolean onFloor = isSolidFloor(blockPos.relative(Direction.DOWN), level);
        state = state.setValue(CONNECTOR_EXTENDED, false);
        for (Direction direction : CABLE_PROPERTIES.keySet()) {
            BlockPos offset = blockPos.relative(direction);
            BlockPos diagonalUpPos = offset.relative(Direction.UP);
            BlockState diagonalUp = level.getBlockState(offset.relative(Direction.UP));
            boolean wallHere = false;
            if (diagonalUp.is(DEVICE_CABLE) || diagonalUpPos.equals(source)) {
                boolean offsetOnFloor = isSolidFloor(offset, level);
                wallHere |= offsetOnFloor;
                if (offsetOnFloor && source == null) {
                    level.setBlock(diagonalUpPos, getUpdatedState(level, diagonalUpPos, blockPos, null), 0);
                }
            }
            BlockEntity blockEntity = level.getBlockEntity(diagonalUpPos);
            if (blockEntity instanceof IAttachableDevice attachableDevice) {
                boolean attaches = attachableDevice.cableAttaches(direction.getOpposite());
                wallHere |= attaches;
                if (attaches) state = state.setValue(CONNECTOR_EXTENDED, true);
            }
            BlockPos diagonalDownPos = offset.relative(Direction.DOWN);
            BlockState diagonalDown = level.getBlockState(diagonalDownPos);
            if (diagonalDown.is(DEVICE_CABLE) || diagonalDownPos.equals(source)) {
                state = state.setValue(CABLE_PROPERTIES.get(direction), onFloor);
                if (source == null) level.setBlock(diagonalDownPos, getUpdatedState(level, diagonalDownPos, blockPos, null), 0);
            }
            if (wallHere) {
                state = state.setValue(WALL, direction);
            }
        }
        return state;
    }

    private BlockState checkWalls(BlockState state, Level level, BlockPos blockPos, @Nullable BlockPos source) {
        BlockPos up = blockPos.relative(Direction.UP);
        BlockPos down = blockPos.relative(Direction.DOWN);
        boolean hasFloor = isSolidFloor(down, level);
        BlockState upState = level.getBlockState(up);
        boolean hasWalls = upState.is(DEVICE_CABLE) || !hasFloor || up.equals(source);
        if (upState.is(DEVICE_CABLE)) {
            Direction wallSide = upState.getValue(WALL);
            state = state.setValue(WALL, wallSide);
        }
        if (!state.getValue(WALL).equals(Direction.DOWN)) {
            Direction wallSide = state.getValue(WALL);
            if (isSolidFloor(down, level)) state = state.setValue(CABLE_PROPERTIES.get(wallSide), true);
            return state;
        }
        state = state.setValue(WALL, Direction.DOWN);
        for (Direction direction : CABLE_PROPERTIES.keySet()) {
            BlockPos offset = blockPos.relative(direction);
            var offsetState = level.getBlockState(offset);
            boolean wallHere = hasWalls && offsetState.isFaceSturdy(level, offset, direction.getOpposite());
            if (wallHere) {
                state = state.setValue(WALL, direction);
                var cableProperty = CABLE_PROPERTIES.get(direction);
                state = state.setValue(cableProperty, hasFloor || state.getValue(cableProperty));
                break;
            }
        }
        return state;
    }

    private BlockState checkCardinals(BlockState state, Level level, BlockPos blockPos, @Nullable BlockPos source) {
        boolean onFloor = isSolidFloor(blockPos.relative(Direction.DOWN), level);
        for (Direction direction : CABLE_PROPERTIES.keySet() ) {
            BooleanProperty property = CABLE_PROPERTIES.get(direction);
            BlockPos offset = blockPos.relative(direction);
            boolean connected = level.getBlockState(offset).is(DEVICE_CABLE);
            if (source != null) connected |= offset.equals(source);
            boolean connector = false;
            BlockEntity blockEntity = level.getBlockEntity(offset);
            if (blockEntity instanceof IAttachableDevice attachableDevice) {
                connector = attachableDevice.cableAttaches(direction.getOpposite());
            } else if (blockEntity instanceof ComputerBlockEntity computer) {
                connector = computer.cableAttaches(direction.getOpposite());
            }
            state = state.setValue(property, (connected || connector) && onFloor);
            state = state.setValue(CONNECTOR_PROPERTIES.get(direction), connector);
        }
        return state;
    }

    private BlockState getUpdatedState(Level level, BlockPos blockPos, @Nullable BlockPos source, @Nullable Direction face) {
        BlockState state = defaultBlockState();
        state = checkCardinals(state, level, blockPos, source);
        BlockState oState = level.getBlockState(blockPos);
        if (oState.is(DEVICE_CABLE)) {
            state = state.setValue(WALL, oState.getValue(WALL));
        }
        if (face != null && !face.equals(Direction.UP)) state = state.setValue(WALL, face);
        state = checkDiagonals(state, level, blockPos, source);
        return checkWalls(state, level, blockPos, source);
    }

    private BlockState getUpdatedState(Level level, BlockPos blockPos) {
        return getUpdatedState(level, blockPos, null, null);
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
        CONNECTOR_NORTH = BooleanProperty.create("connector_north");
        CONNECTOR_EAST = BooleanProperty.create("connector_east");
        CONNECTOR_SOUTH = BooleanProperty.create("connector_south");
        CONNECTOR_WEST = BooleanProperty.create("connector_west");
        CONNECTOR_PROPERTIES.put(Direction.NORTH, CONNECTOR_NORTH);
        CONNECTOR_PROPERTIES.put(Direction.EAST, CONNECTOR_EAST);
        CONNECTOR_PROPERTIES.put(Direction.SOUTH, CONNECTOR_SOUTH);
        CONNECTOR_PROPERTIES.put(Direction.WEST, CONNECTOR_WEST);
        WALL = DirectionProperty.create("wall");
        CONNECTOR_EXTENDED = BooleanProperty.create("connector_extended");
    }
}
