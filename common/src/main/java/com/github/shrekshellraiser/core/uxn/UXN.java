package com.github.shrekshellraiser.core.uxn;

// Adapted directly from https://git.sr.ht/~rabbits/uxn11

import com.github.shrekshellraiser.core.uxn.devices.Device;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class UXN {
    public MemoryRegion memory = new MemoryRegion();
    private final ArrayList<MemoryRegion> expansion = new ArrayList<>();
    public final Stack rst = new Stack("RST"); // return stack
    public final Stack wst = new Stack("WST"); // working stack
    public int pc;
    private Stack s;
    private int ins;
    public int cycles = 0;
    private final UXNBus bus;
    private final Device[] devices = new Device[16];
    private boolean running = false;
    public boolean paused = false;
    public boolean doStep = false;
    private boolean estop = false;
    private final Queue<QueuedEvent> eventQueue = new LinkedList<>();
    private int eventSurplus = 0;

    public boolean _enable_debug = false;
    public static final int MAX_QUEUE = 256;

    private QueuedEvent runningVector;

    public String toString() {
        return wst + "\n" + rst;
    }

    public UXN(@NotNull UXNBus bus, MemoryRegion memory) {
        this(bus);
        this.memory = memory;
        // TODO add configurable expansion memory
        for (int i = 0; i < 16; i++) {
            expansion.add(new MemoryRegion());
        }
    }

    public MemoryRegion getRegion(int id) {
        if (id == 0) {
            return memory;
        }
        if (id - 1 < expansion.size()) {
            return expansion.get(id - 1);
        }
        return null;
    }

    public UXN(@NotNull UXNBus bus) {
        this.bus = bus;
        bus.setUxn(this);
        new SystemDevice().attach(bus);
    }

    public boolean queueEvent(UXNEvent event, UXNBus bus) {
        if (eventQueue.size() < MAX_QUEUE) {
            eventQueue.add(new QueuedEvent(event, bus));
            eventSurplus++;
            return true;
        }
        return false;
    }
    public float getCongestion() {
        return (float) Math.sqrt(eventSurplus / (float)MAX_QUEUE);
    }
    public boolean queueEvent(UXNEvent event) {
        return queueEvent(event, bus);
    }

    private void set(int x, int y, int x2, int y2) {
        if ((ins & 0x20) > 0) {
            x = x2;
            y = y2;
        }
        s.shift((ins & 0x80) > 0 ? x + y : y);
    }
    private void set(int x, int y) {
        // y is how much this instruction moves the stack pointer by default
        // x is how much this changes when in "keep" mode
        set(x, y, x * 2, y * 2);
    }
    private void flip() {
        if (s == rst) {
            s = wst;
            return;
        }
        s = rst;
    }

    public int getEventCount() {
        return eventQueue.size();
    }

    private void stopVector() {
        if (runningVector != null) {
            runningVector.event.post(runningVector.bus);
            runningVector = null;
        }
        running = false;
    }
    public void stop() {
        estop = true;
    }
    // execute 1 instruction
    private void step() {
        cycles++;
        if (pc == 0) {
            stopVector();
            return;
        }
        int r, t, n, l;
        ins = memory.readByte(pc++);
        pc = pc % 0xFFFF;
        s = (ins & 0x40) > 0 ? rst : wst;
        boolean mode2 = (ins & 0x20) > 0;
        if (_enable_debug) {
            System.err.printf("%04x %s\n", pc, DISASM[ins]);
        }
        switch(ins & 0x3f) {
        case 0x00: case 0x20:
            switch(ins) {
            case 0x00: // BRK
                stopVector();
                return;
            case 0x20: // JCI
                t = s.getT(false);
                s.shift(-1);
                if (t == 0) {
                    pc += 2;
                    break;
                } // intentional fall through
            case 0x40: // JMI
                // index into ram (replaces *rr)
                pc += 2 + (short) memory.readShort(pc);
                break;
            case 0x60: // JSI
                s.shift(2);
                int rr = pc;
                pc += 2;
                s.setT(true, pc);
                pc += (short) memory.readShort(rr); // turn signed
                break;
            case 0x80: case 0xc0: // LIT
            case 0xa0: case 0xe0: // LIT2
                set(0, 1);
                s.setT(mode2, memory.read(mode2, pc++));
                if (mode2) {
                    pc++;
                }
                break;
            } break;
        /* ALU */
        case 0x01: case 0x21: // INC
            t = s.getT(mode2);
            set(1, 0);
            s.setT(mode2, t + 1);
            break;
        case 0x02: case 0x22: // POP
            set(1, -1);
            break;
        case 0x03: case 0x23: // NIP
            t = s.getT(mode2);
            set(2, -1);
            s.setT(mode2, t);
            break;
        case 0x04: case 0x24: // SWP
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, 0);
            s.setT(mode2, n);
            s.setN(mode2, t);
            break;
        case 0x05: case 0x25: // ROT
            t = s.getT(mode2);
            n = s.getN(mode2);
            l = s.getL(mode2);
            set(3, 0);
            s.setT(mode2, l);
            s.setN(mode2, t);
            s.setL(mode2, n);
            break;
        case 0x06: case 0x26: // DUP
            t = s.getT(mode2);
            set(1, 1);
            s.setT(mode2, t);
            s.setN(mode2, t);
            break;
        case 0x07: case 0x27: // OVR
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, 1);
            s.setT(mode2, n);
            s.setN(mode2, t);
            s.setL(mode2, n);
            break;
        case 0x08: case 0x28: // EQU
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1, 4, -3);
            s.setT(false, n == t ? 1 : 0);
            break;
        case 0x09: case 0x29: // NEQ
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2,-1,4,-3);
            s.setT(false, n != t ? 1 : 0);
            break;
        case 0x0a: case 0x2a: // GTH
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1, 4, -3);
            s.setT(false, n > t ? 1 : 0);
            break;
        case 0x0b: case 0x2b: // LTH
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1, 4, -3);
            s.setT(false, n < t ? 1 : 0);
            break;
        case 0x0c: // JMP
            t = s.getT(false);
            set(1, -1);
            pc += (byte) t; // hopefully this will handle signed?
            break;
        case 0x2c: // JMP2
            t = s.getT(true);
            set(1, -1); // 2, -2 is implied
            pc = t;
            break;
        case 0x0d: // JCN
            t = s.getT(false);
            n = s.getN(false);
            set(2, -2);
            if (n > 0) pc += (byte) t;
            break;
        case 0x2d: // JCN2
            t = s.getT(true);
            n = s.getL(false);
            set(2, -2, 3, -3);
            if (n > 0) pc = t;
            break;
        case 0x0e: // JSR
            t = s.getT(false);
            set(1, -1);
            flip();
            s.shift(2);
            s.setT(true, pc);
            pc += (byte) t;
            break;
        case 0x2e: // JSR2
            t = s.getT(true);
            set(1, -1); // 2, -2 is implied
            flip();
            s.shift(2);
            s.setT(true, pc);
            pc = t;
            break;
        case 0x0f: case 0x2f: // STH
            t = s.getT(mode2);
            set(1, -1);
            flip();
            s.shift(mode2 ? 2 : 1);
            s.setT(mode2, t);
            break;
        case 0x10: case 0x30: // LDZ
            t = s.getT(false);
            set(1, 0, 1, 1);
            s.setT(mode2, memory.readZP(mode2, t));
            break;
        case 0x11: // STZ
            t = s.getT(false);
            n = s.getN(mode2);
            set(2, -2, 3, -3);
            memory.writeZP(mode2, t, n);
            break;
        case 0x31: // STZ2
            t = s.getT(false);
            n = s.getH2();
            set(2, -2, 3, -3);
            memory.writeZP(mode2, t, n);
            break;
        case 0x12: case 0x32: // LDR
            t = s.getT(false);
            set(1, 0, 1, 1);
                r = pc + (byte) t; // should be signed
                s.setT(mode2, memory.read(mode2, r));
            break;
        case 0x13: // STR
            t = s.getT(false);
            n = s.getN(false);
            set(2, -2, 3, -3);
            r = pc + (byte) t;
            memory.writeByte(r, (byte) n);
            break;
        case 0x33: // STR2
            t = s.getT(false);
            n = s.getH2();
            set(2, -2, 3, -3);
            r = pc + (byte) t;
            memory.writeShort(r, (short) n);
            break;
        case 0x14: case 0x34: // LDA
            t = s.getT(true);
            set(2, -1, 2, 0);
            s.setT(mode2, memory.read(mode2, t));
            break;
        case 0x15: // STA
            t = s.getT(true);
            n = s.getL(false);
            set(3, -3, 4, -4);
            memory.write(false, t, n);
            break;
        case 0x35: // STA2
            t = s.getT(true);
            n = s.getN(true);
            set(3, -3, 4, -4);
            memory.write(mode2, t, n);
            break;
        case 0x16: case 0x36: // DEI
            t = s.getT(false);
            set(1, 0, 1, 1);
            if (mode2) {
                s.setN(false, bus.dei(t));
                s.setT(false, bus.dei((t + 1) % 256));
            } else {
                s.setT(false, bus.dei(t));
            }
            break;
        case 0x17: case 0x37: // DEO
            t = s.getT(false);
            n = s.getN(false);
            l = s.getL(false);
            set(2, -2, 3, -3);
            if (mode2) {
                bus.deo(t, (byte) l);
                bus.deo((t + 1) % 256, (byte) n);
            } else {
                bus.deo(t, (byte) n);
            }
            break;
        case 0x18: case 0x38: // ADD
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1);
            s.setT(mode2, n + t);
            break;
        case 0x19: case 0x39: // SUB
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1);
            s.setT(mode2, n - t);
            break;
        case 0x1a: case 0x3a: // MUL
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1);
            s.setT(mode2, n * t);
            break;
        case 0x1b: case 0x3b: // DIV
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1);
            s.setT(mode2, t != 0 ? n / t : 0);
            break;
        case 0x1c: case 0x3c: // AND
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1);
            s.setT(mode2, n & t);
            break;
        case 0x1d: case 0x3d: // ORA
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1);
            s.setT(mode2, n | t);
            break;
        case 0x1e: case 0x3e: // EOR
            t = s.getT(mode2);
            n = s.getN(mode2);
            set(2, -1);
            s.setT(mode2, n ^ t);
            break;
        case 0x1f: // SFT
            t = s.getT(false);
            n = s.getN(false);
            set(2, -1);
            s.setT(false, n >> (t & 0xf) << (t >> 4));
            break;
        case 0x3f: // SFT2
            t = s.getT(false);
            n = s.getH2();
            set(2, -1, 3, -1);
            s.setT(true, n >> (t & 0xf) << (t >> 4));
            break;
        }
        pc = pc % 0xFFFF;
    }
    public void run() {
        running = true;
        while (running) {
            step();
        }
    }

    public int runLimited(int limit) {
        if (paused && !doStep) {
            return 0;
        }
        int used;
        for (used = 0; used < limit; used++) {
            if (estop) break;
            if (!running) {
                // check for vector in the queue
                var event = eventQueue.poll();
                if (event == null) return used;
                eventSurplus--;
                runningVector = event;
                event.event.handle(event.bus);
                running = true;
            }
            step();
            if (paused) {
                doStep = false;
                break;
            }
        }
        return used;
    }

    public boolean isRunning() {return running;}

    public static final String[] DISASM = new String[]{"BRK","INC", "POP","NIP", "SWP","ROT", "DUP","OVR", "EQU","NEQ", "GTH","LTH","JMP","JCN","JSR","STH", "LDZ","STZ", "LDR","STR", "LDA","STA", "DEI","DEO", "ADD","SUB", "MUL","DIV", "AND","ORA", "EOR","SFT", "JCI","INC2", "POP2","NIP2", "SWP2","ROT2", "DUP2","OVR2", "EQU2","NEQ2", "GTH2","LTH2", "JMP2","JCN2", "JSR2","STH2", "LDZ2","STZ2", "LDR2","STR2", "LDA2","STA2", "DEI2","DEO2", "ADD2","SUB2", "MUL2","DIV2", "AND2","ORA2", "EOR2","SFT2", "JMI","INCr", "POPr","NIPr", "SWPr","ROTr", "DUPr","OVRr", "EQUr","NEQr", "GTHr","LTHr", "JMPr","JCNr", "JSRr","STHr", "LDZr","STZr", "LDRr","STRr", "LDAr","STAr", "DEIr","DEOr", "ADDr","SUBr", "MULr","DIVr", "ANDr","ORAr", "EORr","SFTr", "JSI","INC2r", "POP2r","NIP2r", "SWP2r","ROT2r", "DUP2r","OVR2r", "EQU2r","NEQ2r", "GTH2r","LTH2r", "JMP2r","JCN2r", "JSR2r","STH2r", "LDZ2r","STZ2r", "LDR2r","STR2r", "LDA2r","STA2r", "DEI2r","DEO2r", "ADD2r","SUB2r", "MUL2r","DIV2r", "AND2r","ORA2r", "EOR2r","SFT2r", "LIT","INCk", "POPk","NIPk", "SWPk","ROTk", "DUPk","OVRk", "EQUk","NEQk", "GTHk","LTHk", "JMPk","JCNk", "JSRk","STHk", "LDZk","STZk", "LDRk","STRk", "LDAk","STAk", "DEIk","DEOk", "ADDk","SUBk", "MULk","DIVk", "ANDk","ORAk", "EORk","SFTk","LIT2", "INC2k","POP2k", "NIP2k","SWP2k", "ROT2k","DUP2k", "OVR2k","EQU2k", "NEQ2k","GTH2k", "LTH2k","JMP2k", "JCN2k","JSR2k", "STH2k","LDZ2k", "STZ2k","LDR2k", "STR2k","LDA2k", "STA2k","DEI2k", "DEO2k","ADD2k", "SUB2k","MUL2k", "DIV2k","AND2k", "ORA2k","EOR2k", "SFT2k","LITr", "INCkr","POPkr", "NIPkr","SWPkr", "ROTkr","DUPkr", "OVRkr","EQUkr", "NEQkr","GTHkr", "LTHkr","JMPkr", "JCNkr","JSRkr", "STHkr","LDZkr", "STZkr","LDRkr", "STRkr","LDAkr", "STAkr","DEIkr", "DEOkr","ADDkr", "SUBkr","MULkr", "DIVkr","ANDkr", "ORAkr","EORkr", "SFTkr","LIT2r", "INC2kr","POP2kr", "NIP2kr","SWP2kr", "ROT2kr","DUP2kr", "OVR2kr","EQU2kr", "NEQ2kr","GTH2kr", "LTH2kr","JMP2kr", "JCN2kr","JSR2kr", "STH2kr","LDZ2kr", "STZ2kr","LDR2kr", "STR2kr","LDA2kr", "STA2kr","DEI2kr", "DEO2kr","ADD2kr", "SUB2kr","MUL2kr", "DIV2kr","AND2kr", "ORA2kr","EOR2kr", "SFT2kr"};
}

class SystemDevice extends Device {
    public SystemDevice() {
        deviceId = 0;
    }
    @Override
    public void write(int address) {
        UXN uxn = bus.getUxn();
        int port = address & 0x0F;
        switch (port) {
        case 0x00: case 0x01: // Reset vector*
            break;
        case 0x02: // ignore the first byte of expansion*
            break;
        case 0x03: // expansion*`
            int addr = bus.readDev(address - 1) << 8 | bus.readDev(address);
            int operation = uxn.memory.readByte(addr);
            int length = uxn.memory.readShort(addr+1);
            int sbank = uxn.memory.readShort(addr+3);
            int saddr = uxn.memory.readShort(addr+5);
            if (operation == 0) {
                MemoryRegion source = uxn.getRegion(sbank);
                int value = uxn.memory.readByte(addr+7);
                source.fill(saddr, length, value);
            } else if (operation == 1 || operation == 2) {
                int dbank = uxn.memory.readShort(addr+7);
                int daddr = uxn.memory.readShort(addr+9);
                MemoryRegion source = uxn.getRegion(sbank);
                MemoryRegion destination = uxn.getRegion(dbank);
                if (operation == 1) {
                    source.copyTo(destination, saddr, daddr, length);
                } else {
                    source.copyToL(destination, saddr, daddr, length);
                }
            }
            break;
        case 0x04:
            uxn.wst.setPtr(bus.readDev(address)); break;
        case 0x05:
            uxn.rst.setPtr(bus.readDev(address)); break;
        case 0x08: case 0x09: // red*
        case 0x0a: case 0x0b: // green*
        case 0x0c: case 0x0d: // blue*
            bus.updateColors();
            break;
        case 0x0e: // debug
            int data = bus.readDev(address);
            if (data == 0x01) {
                System.out.println(uxn.wst); // TODO replace these
                System.out.println(uxn.rst);
            }
            break;
        case 0x0f: // state
            break;
        }
    }

    @Override
    public void read(int address) {
        UXN uxn = bus.getUxn();
        int port = address & 0x0F;
        switch (port) {
        case 0x00: case 0x01: // Reset vector*
            break;
        case 0x02: case 0x03: // expansion*
            break;
        case 0x04:
            bus.writeDev(address, (byte)uxn.wst.getPtr()); break;
        case 0x05:
            bus.writeDev(address, (byte)uxn.rst.getPtr()); break;
        case 0x08: case 0x09: // red*
        case 0x0a: case 0x0b: // green*
        case 0x0c: case 0x0d: // blue*
            // TODO
            break;
        case 0x0e: // debug
            break;
        case 0x0f: // state
            break;
        }
    }

    @Override
    public String getLabel() {
        return "System";
    }
}

class QueuedEvent {
    public final UXNEvent event;
    public final UXNBus bus;

    QueuedEvent(UXNEvent event, UXNBus bus) {
        this.event = event;
        this.bus = bus;
    }
}