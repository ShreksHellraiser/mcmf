package com.github.shrekshellraiser.computer.block.entity;

import com.github.shrekshellraiser.ComputerMod;
import com.github.shrekshellraiser.computer.block.ComputerBlock;
import com.github.shrekshellraiser.computer.screen.ComputerMenu;
import com.github.shrekshellraiser.core.uxn.UXN;
import com.github.shrekshellraiser.core.uxn.UXNBus;
import com.github.shrekshellraiser.devices.screen.ScreenDeviceMenu;
import com.github.shrekshellraiser.network.ComputerContentPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.github.shrekshellraiser.ModBlockEntities.COMPUTER_BLOCK_ENTITY;

public class ComputerBlockEntity extends BaseContainerBlockEntity implements MenuProvider, IBusProvider {
    public static final int STRING_LENGTH = "WST 00 00 00 00 00 00 00 00 <".length(); // 29 characters
    public static final int DATA_START = STRING_LENGTH * 2;
    public static final int DATA_LENGTH = DATA_START + 5;
    public static final int CIRCLE_ITERS = 256;
    private static final float[] COS_LUT = new float[CIRCLE_ITERS];
    private static final float[] SIN_LUT = new float[CIRCLE_ITERS];
    static {
        for (int i = 0; i < CIRCLE_ITERS; i++) {
            float angle = (float) (i * 2 * Math.PI / CIRCLE_ITERS);
            COS_LUT[i] = (float) Math.cos(angle);
            SIN_LUT[i] = (float) Math.sin(angle);
        }
    }
    private final UXNBus bus;
    private final ContainerData data = new ContainerData() {
        private String rst = "RST 00 00 00 00 00 00 00 00|<";
        private String wst = "WST 00 00 00 00 00 00 00 00|<";
        @Override
        public int get(int i) {
            UXN uxn = bus.getUxn();
            if (uxn != null) {
                if (i == 0) {
                    rst = uxn.rst.toString();
                } else if (i == STRING_LENGTH) {
                    wst = uxn.wst.toString();
                }
            }
            if (i < STRING_LENGTH) {
                return rst.charAt(i);
            } else if (i < DATA_START) {
                return wst.charAt(i - STRING_LENGTH);
            } else if (i == DATA_START) {
                return (bus.isPoweredOn()) ? 1 : 0;
            } else if (i == DATA_START + 1) {
                if (uxn != null) {
                    return uxn.pc;
                }
            } else if (i == DATA_START + 2) {
                return (bus.isPaused()) ? 1 : 0;
            } else if (i == DATA_START + 3) {
                return bus.getEventCount();
            } else if (i == DATA_START + 4) {
                return bus.isArgumentMode() ? 1 : 0;
            }
            return 0;
        }

        @Override
        public void set(int i, int j) {
        }

        @Override
        public int getCount() {
            return DATA_LENGTH;
        }
    };

    public static final int CONTAINER_SIZE = 2;
    private NonNullList<ItemStack> items;

    @Override
    public UXNBus getBus(Direction dir) {
        return bus;
    }

    @Override
    public BlockPos getScanRoot() {
        Direction facing = this.getBlockState().getValue(ComputerBlock.FACING);
        return getBlockPos().relative(facing.getOpposite());
    }

    @Override
    public Direction getScanStartDir() {
        return this.getBlockState().getValue(ComputerBlock.FACING);
    }

    private void updateString(int o, String s) {
        for (int i = 0; i < STRING_LENGTH; i++) {
            data.set(o + i, s.charAt(i));
        }
    }

    public boolean startup() {
        if (bus.getUxn() == null) {
            // this will register the bus on this computer
            return bus.startup();
        }
        return false;
    }

    public double getRenderX() {
        var facing = getBlockState().getValue(ComputerBlock.FACING);
        if (facing.equals(Direction.WEST)) {
            return 0.06;
        } else if (facing.equals(Direction.EAST)) {
            return 0.94;
        }
        return 0.5;
    }

    public double getRenderZ() {
        var facing = getBlockState().getValue(ComputerBlock.FACING);
        if (facing.equals(Direction.NORTH)) {
            return 0.06;
        } else if (facing.equals(Direction.SOUTH)) {
            return 0.94;
        }
        return 0.5;
    }

    private int particle = 0;
    public void spawnEventParticle(boolean success) {
        SimpleParticleType particleType = success ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.SMOKE;
        if (getLevel() instanceof ServerLevel sLevel) {
            BlockPos pos = getBlockPos();
            float radius = 0.25f;
            particle %= CIRCLE_ITERS;
            float y = pos.getY() + radius * COS_LUT[particle] + 0.5f;
            var x = pos.getX() + getRenderX();
            var z = pos.getZ() + getRenderZ();
            var facing = getBlockState().getValue(ComputerBlock.FACING);
            if (facing.equals(Direction.EAST) || facing.equals(Direction.WEST)) {
                z += radius * SIN_LUT[particle];
            } else {
                x += radius * SIN_LUT[particle];
            }
            particle++;
            float speed = 0.5f;
            sLevel.sendParticles(particleType, x, y, z, 0, 0.0f, 0.0f, 0.0f, speed);
        }
    }

    public void spawnFailureParticle() {
        if (getLevel() instanceof ServerLevel sLevel) {
            BlockPos pos = getBlockPos();
            float x = pos.getX() + 0.5f;
            float z = pos.getZ() + 0.5f;
            float y = pos.getY() + 1f;
            sLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 0, 0.0f, 0.0f, 0.0f, 0.5f);
            sLevel.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public void shutdown() {
        if (bus.isPoweredOn()) {
            bus.shutdown();
        }
    }

    public void pause() {
        bus.pause();
    }

    public void togglePause(Player player) {
        bus.pause(!bus.isPaused());
    }

    public void togglePower(Player player) {
        if (bus.isPoweredOn()) shutdown();
        else {
            if (!startup()) {
                player.sendMessage(new TextComponent("Unable to start computer"), player.getUUID());
            }
        }
    }

    public ComputerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(COMPUTER_BLOCK_ENTITY.get(), blockPos, blockState);
        this.bus = new UXNBus(this);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent("Computer");
    }

    @Override
    protected Component getDefaultName() {
        return new TextComponent("Computer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ComputerMenu(i, inventory, this, data, this);
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new ComputerMenu(i, inventory, this, data, this);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        bus.tick();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((ComputerBlockEntity)t).tick(level, blockPos, blockState);
    }

    public void step(Player player) {
        if (bus.getUxn() == null) return;
        bus.getUxn().doStep = true;
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemStack = ContainerHelper.removeItem(this.items, i, j);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.items, i);
    }

    private double playerDistance(Player player) {
        return player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5);
    }

    private void sendUpdatePacket() {
        if (level != null && !level.isClientSide && level instanceof ServerLevel sLevel) {
            ComputerMod.LOGGER.warn("Sent update packet!");
            ComputerContentPacket.send(getItem(1), this.worldPosition, sLevel.players());
        }
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.items.set(i, itemStack);
        if (i == 1) sendUpdatePacket();
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        assert this.level != null;
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(playerDistance(player) > 64.0);
        }
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        sendUpdatePacket();
        ContainerHelper.loadAllItems(compoundTag, this.items);
    }

    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        ContainerHelper.saveAllItems(compoundTag, this.items);
    }

    public boolean cableAttaches(Direction attachSide) {
        Direction facing = this.getBlockState().getValue(ComputerBlock.FACING);
        return facing.getOpposite().equals(attachSide);
    }

    public void toggleArgumentMode() {
        bus.setArgumentMode(!bus.isArgumentMode());
    }
}
