package com.github.shrekshellraiser.computer.block;

import com.github.shrekshellraiser.serial.SerialCableBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class CableShapes {
    private static final double MIN = 0.1875;
    private static final double MAX = 1 - MIN;
    private static final double THICKNESS = 0.1; // Slightly larger than 1 pixel tall
    private static final VoxelShape SHAPE_CABLE_FLOOR = Shapes.box(MIN, 0, MIN, MAX, THICKNESS, MAX);
    private static final Map<Direction, VoxelShape> SHAPE_CABLE_ARM = new HashMap<>();
    private static final Map<Integer, VoxelShape> SHAPE_CABLE_WALL = new HashMap<>();
    private static final Map<Direction, Integer> CABLE_WALL_VALUE = new HashMap<>();

    // 4 possible directions for the arms, one wall with 5 options
    private static final VoxelShape[] CABLE_SHAPES = new VoxelShape[1 << 4 + 5];

    private static int getCableIndex(BlockState state) {
        var index = 0;
        int i = 0;
        for (Direction facing : CableBlock.CABLE_PROPERTIES.keySet()) {
            if (state.getValue(CableBlock.CABLE_PROPERTIES.get(facing))) index |= 1 << i;
            i++;
        }
        index |= (CABLE_WALL_VALUE.get(state.getValue(CableBlock.WALL))) << i;

        return index;
    }

    private static VoxelShape getCableShape(int index) {
        var shape = CABLE_SHAPES[index];
        if (shape != null) return shape;

        shape = Shapes.empty();
        boolean hasCore = false;
        int i = 0;
        for (Direction facing : CableBlock.CABLE_PROPERTIES.keySet()) {
            if ((index & (1 << i)) != 0) {
                hasCore = true;
                shape = Shapes.or(shape, SHAPE_CABLE_ARM.get(facing));
            }
            i++;
        }
        boolean hasWall = false;
        int facingIndex = index >> i;
        if (facingIndex > 0) {
            shape = Shapes.or(shape, SHAPE_CABLE_WALL.get(facingIndex));
            hasWall = true;
        }
        if (hasCore || !hasWall) shape = Shapes.or(shape, SHAPE_CABLE_FLOOR);
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
        SHAPE_CABLE_ARM.put(Direction.NORTH, Shapes.box(MIN, 0, 0, MAX, THICKNESS, MIN));
        SHAPE_CABLE_ARM.put(Direction.SOUTH, Shapes.box(MIN, 0, MAX, MAX, THICKNESS, 1));
        SHAPE_CABLE_ARM.put(Direction.EAST, Shapes.box(MAX, 0, MIN, 1, THICKNESS, MAX));
        SHAPE_CABLE_ARM.put(Direction.WEST, Shapes.box(0, 0, MIN, MIN, THICKNESS, MAX));
        SHAPE_CABLE_WALL.put(1, Shapes.box(MIN, 0, 0, MAX, 1, THICKNESS));
        SHAPE_CABLE_WALL.put(2, Shapes.box(MIN, 0, 1 - THICKNESS, MAX, 1, 1));
        SHAPE_CABLE_WALL.put(3, Shapes.box(1 - THICKNESS, 0, MIN, 1, 1, MAX));
        SHAPE_CABLE_WALL.put(4, Shapes.box(0, 0, MIN, THICKNESS, 1, MAX));

        CABLE_WALL_VALUE.put(Direction.DOWN, 0);
        CABLE_WALL_VALUE.put(Direction.UP, 0);
        CABLE_WALL_VALUE.put(Direction.NORTH, 1);
        CABLE_WALL_VALUE.put(Direction.SOUTH, 2);
        CABLE_WALL_VALUE.put(Direction.EAST, 3);
        CABLE_WALL_VALUE.put(Direction.WEST, 4);
    }
}
