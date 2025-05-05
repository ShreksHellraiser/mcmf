package com.github.shrekshellraiser.serial;

import net.minecraft.core.BlockPos;

public interface ISerialPeer {
    void attach(ISerialPeer device);
    void detach(ISerialPeer device);

    void write(char ch); // Assumes STDIO type
    void write(char ch, SerialType type);

    void setConflicting(boolean b);

    ISerialPeer getPeer();
    BlockPos getPos();

}
