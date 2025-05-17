package com.github.shrekshellraiser;

import com.github.shrekshellraiser.computer.screen.*;
import com.github.shrekshellraiser.api.devices.GenericDeviceMenu;
import com.github.shrekshellraiser.api.devices.GenericDeviceScreen;
import com.github.shrekshellraiser.devices.flasher.FlasherDeviceMenu;
import com.github.shrekshellraiser.devices.flasher.FlasherDeviceScreen;
import com.github.shrekshellraiser.devices.screen.*;
import com.github.shrekshellraiser.serial.terminal.SerialTerminalMenu;
import com.github.shrekshellraiser.serial.terminal.SerialTerminalScreen;
import dev.architectury.platform.Platform;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;
import static com.github.shrekshellraiser.ComputerMod.REGISTRIES;

public class ModMenus {
    public static final Registrar<MenuType<?>> MENUS = REGISTRIES.get().get(Registry.MENU_REGISTRY);
    public static RegistrySupplier<MenuType<ComputerMenu>> COMPUTER_MENU;
    public static RegistrySupplier<MenuType<GenericDeviceMenu>> GENERIC_DEVICE_MENU;
    public static RegistrySupplier<MenuType<SerialTerminalMenu>> SERIAL_TERMINAL_MENU;
    public static RegistrySupplier<MenuType<FlasherDeviceMenu>> FLASHER_DEVICE_MENU;
    public static RegistrySupplier<MenuType<ScreenDeviceMenu>> SCREEN_DEVICE_MENU;

    private static <T extends AbstractContainerMenu> RegistrySupplier<MenuType<T>> register(String name, Supplier<MenuType<T>> menuType) {
        return MENUS.register(new ResourceLocation(MOD_ID, name), menuType);
    }

    public static void registerServer() {
        COMPUTER_MENU = register("computer", () -> new MenuType<>(ComputerMenu::new));
        GENERIC_DEVICE_MENU = register("device", () -> new MenuType<>(GenericDeviceMenu::new));
        SERIAL_TERMINAL_MENU = register("serial_terminal", () -> new MenuType<>(SerialTerminalMenu::new));
        FLASHER_DEVICE_MENU = register("flasher_device", () -> new MenuType<>(FlasherDeviceMenu::new));
        SCREEN_DEVICE_MENU = register("screen_device", () -> new MenuType<>(ScreenDeviceMenu::new));
    }

    public static void registerClient() {
        MenuRegistry.registerScreenFactory(COMPUTER_MENU.get(), ComputerScreen::new);
        MenuRegistry.registerScreenFactory(GENERIC_DEVICE_MENU.get(), GenericDeviceScreen<GenericDeviceMenu>::new);
        MenuRegistry.registerScreenFactory(SERIAL_TERMINAL_MENU.get(), SerialTerminalScreen::new);
        MenuRegistry.registerScreenFactory(FLASHER_DEVICE_MENU.get(), FlasherDeviceScreen::new);
        MenuRegistry.registerScreenFactory(SCREEN_DEVICE_MENU.get(), ScreenDeviceScreen::new);
    }
}
