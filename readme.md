# Minecraft Mainframes (working title)
This is a mod in early development for Minecraft Fabric 1.18.2.

This mod adds computers which implement the [UXN](https://wiki.xxiivv.com/site/uxntal.html) instruction set.

## Getting Started
* Place down a Flasher Device
* Place an SROM item into the slot
* Drag+drop compiled UXN roms into the window

Place down a computer, and use 'cables' to connect it to devices.
**Pay close attention to the side the visual 'port' is on the device's texture!**

Each device has a right-click menu where you can select which device slot
you want the device to in.

The serial device implements the standard Varvara Console device, but you'll need to connect a terminal using
serial cables to the serial device to get any I/O.

## Argument Mode
Flip on the argument mode switch and the computer will enter a special state.
When it is booted with this switch enabled, it will search for the lowest # serial
device connected. It will then send a string "ARGS>" followed by the ascii 0x05 character
to indicate it is expecting a list of arguments. Simply reply with a newline terminated string,
either via the serial terminal, or via another UXN computer.

## Custom Devices
There are currently two non-varvara devices implemented.
These are the redstone device, and the multiplexer device.

The redstone device is for controlling redstone and has this signature:
`@Redstone &vector $2 &front $1 &right $1  &left $1 &back $1 &up $1 &down $1 `

The multiplexer device lets you attach more devices accessible through one device slot.
To use it look at the top texture of the block, it looks vaguely like a fork.
The side with one 'branch' connects to your computer's device network. The other side
acts as a host for another subnetwork of up to 15 additional devices.
To interact with this, write the device's address you would like to write to the `.Multiplexer/device` byte,
and then read or write a byte or word to the `.Multiplexer/data` byte/word.
The signature of this device is: `@Mux &device $1 &data $2`.

These devices are both subject to change as the mod is developed more!

## Issues
* Serial stuff is currently non-directional, so a terminal does not require the cable to enter the back
this will be changed in the future.
* The redstone device queues multiple redstone vectors for a single redstone update,
this will eventually get fixed (hopefully).
