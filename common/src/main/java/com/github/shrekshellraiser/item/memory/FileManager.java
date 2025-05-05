package com.github.shrekshellraiser.item.memory;

import dev.architectury.utils.GameInstance;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class FileManager {
    private static void checkDir(String folder) {
        File world = new File(GameInstance.getServer()
                .getWorldPath(new LevelResource(MOD_ID)).toString());
        if (!world.isDirectory()) {
            world.mkdir();
        }
        File folderFile = new File(world, folder);
        if (!folderFile.isDirectory()) {
            folderFile.mkdir();
        }
    }
    public static byte[] readFile(String folder, String name) {
        checkDir(folder);
        try {
            return Files.readAllBytes(Paths.get(GameInstance.getServer()
                    .getWorldPath(new LevelResource(MOD_ID)).toString(), folder, name));
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveFile(byte[] data, String folder, String name) {
        checkDir(folder);
        Path path = Paths.get(GameInstance.getServer()
                .getWorldPath(new LevelResource(MOD_ID)).toString(), folder, name);
        try (FileOutputStream fos = new FileOutputStream(path.toString())) {
            fos.write(data);
        } catch (IOException e) {
            return;
        }
    }
}
