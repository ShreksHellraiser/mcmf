package com.github.shrekshellraiser;

import com.github.shrekshellraiser.computer.screen.*;
import com.github.shrekshellraiser.devices.api.GenericDeviceMenu;
import com.github.shrekshellraiser.devices.api.GenericDeviceScreen;
import com.github.shrekshellraiser.devices.flasher.FlasherDeviceMenu;
import com.github.shrekshellraiser.devices.flasher.FlasherDeviceScreen;
import com.github.shrekshellraiser.devices.screen.*;
import com.github.shrekshellraiser.serial.screen.SerialTerminalMenu;
import com.github.shrekshellraiser.serial.screen.SerialTerminalScreen;
import dev.architectury.platform.Platform;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(MOD_ID, Registry.MENU_REGISTRY);
    public static RegistrySupplier<MenuType<ComputerMenu>> COMPUTER_MENU;
    public static RegistrySupplier<MenuType<GenericDeviceMenu>> GENERIC_DEVICE_MENU;
    public static RegistrySupplier<MenuType<SerialTerminalMenu>> SERIAL_TERMINAL_MENU;
    public static RegistrySupplier<MenuType<FlasherDeviceMenu>> FLASHER_DEVICE_MENU;
    public static RegistrySupplier<MenuType<ScreenDeviceMenu>> SCREEN_DEVICE_MENU;

    public static void register() {
        COMPUTER_MENU = MENUS.register("computer", () -> new MenuType<>(ComputerMenu::new));
        GENERIC_DEVICE_MENU = MENUS.register("device", () -> new MenuType<>(GenericDeviceMenu::new));
        SERIAL_TERMINAL_MENU = MENUS.register("serial_terminal", () -> new MenuType<>(SerialTerminalMenu::new));
        FLASHER_DEVICE_MENU = MENUS.register("flasher_device", () -> new MenuType<>(FlasherDeviceMenu::new));
        SCREEN_DEVICE_MENU = MENUS.register("screen_device", () -> new MenuType<>(ScreenDeviceMenu::new));
        MENUS.register();
        if (Platform.getEnv() == EnvType.CLIENT) {
            MenuRegistry.registerScreenFactory(COMPUTER_MENU.get(), ComputerScreen::new);
            MenuRegistry.registerScreenFactory(GENERIC_DEVICE_MENU.get(), GenericDeviceScreen<GenericDeviceMenu>::new);
            MenuRegistry.registerScreenFactory(SERIAL_TERMINAL_MENU.get(), SerialTerminalScreen::new);
            MenuRegistry.registerScreenFactory(FLASHER_DEVICE_MENU.get(), FlasherDeviceScreen::new);
            MenuRegistry.registerScreenFactory(SCREEN_DEVICE_MENU.get(), ScreenDeviceScreen::new);
        }
    }
}
