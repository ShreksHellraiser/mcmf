package com.github.shrekshellraiser.serial.infuser;

import com.github.shrekshellraiser.api.serial.SerialPeerBlockEntity;
import com.github.shrekshellraiser.serial.infuser.puzzles.HighLowPuzzle;
import com.github.shrekshellraiser.serial.infuser.puzzles.InfuserPuzzle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;

import static com.github.shrekshellraiser.ModBlockEntities.INFUSER_BLOCK_ENTITY;

public class InfuserBlockEntity extends SerialPeerBlockEntity {
    public InfuserBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(INFUSER_BLOCK_ENTITY.get(), blockPos, blockState);
    }
    private int maxCraftProgress = 5;
    private int craftProgress = 0;
    private boolean crafting = true;

    ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            if (i == 0) {
                return craftProgress;
            } else if (i == 1) {
                return maxCraftProgress;
            }
            return 0;
        }

        @Override
        public void set(int i, int j) {

        }

        @Override
        public int getCount() {
            return 2;
        }
    };
    private StringBuilder buffer = new StringBuilder();
    InfuserPuzzle puzzle = new HighLowPuzzle();

    private void writePeerString(String s) {
        if (getPeer() == null) return;
        for (char ch : s.toCharArray()) {
            writePeer(ch);
        }
    }
    private void tickCraft() {
        if (crafting) {
            craftProgress++;
            if (craftProgress >= maxCraftProgress) {
                crafting = false;
                craftProgress = 0;
            }
        }
    }
    private void onLine(String s) {
        writePeerString(puzzle.tick(s));
        if (puzzle.isSolved()) {
            puzzle.reset();
            tickCraft();
        }
    }

    @Override
    public void write(char ch) {
        if (ch == '\n') {
            String line = buffer.toString();
            onLine(line);
            buffer.setLength(0);
        } else {
            buffer.append(ch);
        }
    }

    @Override
    protected Component getDefaultName() {
        return new TextComponent("Infuser");
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new InfuserMenu(i, inventory, this, data);
    }
}
