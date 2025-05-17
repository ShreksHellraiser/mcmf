package com.github.shrekshellraiser.serial;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class SerialCableShapes {
    private static final double MIN = 0.375;
    private static final double MAX = 1 - MIN;

    private static final VoxelShape SHAPE_CABLE_CORE = Shapes.box(MIN, MIN, MIN, MAX, MAX, MAX);
    private static final Map<Direction, VoxelShape> SHAPE_CABLE_ARM = new HashMap<>();

    private static final VoxelShape[] CABLE_SHAPES = new VoxelShape[1 << 6];

    private static int getCableIndex(BlockState state) {
        var index = 0;
        for (Direction facing : Direction.values()) {
            if (state.getValue(SerialCableBlock.CABLE_PROPERTIES.get(facing))) index |= 1 << facing.ordinal();
        }

        return index;
    }

    private static VoxelShape getCableShape(int index) {
        var shape = CABLE_SHAPES[index];
        if (shape != null) return shape;

        shape = SHAPE_CABLE_CORE;
        for (Direction facing : Direction.values()) {
            if ((index & (1 << facing.ordinal())) != 0) {
                shape = Shapes.or(shape, SHAPE_CABLE_ARM.get(facing));
            }
        }

        CABLE_SHAPES[index] = shape;
        return CABLE_SHAPES[index];
    }

    public static VoxelShape getShape(BlockState state) {
        var index = getCableIndex(state);
        var shape = CABLE_SHAPES[index];
        if (shape != null) return shape;

        shape = getCableShape(index);
        return shape;
    }

    static {
        SHAPE_CABLE_ARM.put(Direction.DOWN, Shapes.box(MIN, 0, MIN, MAX, MIN, MAX));
        SHAPE_CABLE_ARM.put(Direction.UP, Shapes.box(MIN, MAX, MIN, MAX, 1, MAX));
        SHAPE_CABLE_ARM.put(Direction.NORTH, Shapes.box(MIN, MIN, 0, MAX, MAX, MIN));
        SHAPE_CABLE_ARM.put(Direction.SOUTH, Shapes.box(MIN, MIN, MAX, MAX, MAX, 1));
        SHAPE_CABLE_ARM.put(Direction.WEST, Shapes.box(0, MIN, MIN, MIN, MAX, MAX));
        SHAPE_CABLE_ARM.put(Direction.EAST, Shapes.box(MAX, MIN, MIN, 1, MAX, MAX));
    }
}
