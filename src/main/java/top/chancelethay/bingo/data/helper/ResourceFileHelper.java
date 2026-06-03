package top.chancelethay.bingo.data.helper;

import top.chancelethay.bingo.lib.util.ConsoleMessenger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ResourceFileHelper
{
    public static boolean deleteFolderRecurse(String folderPath) {
        File folder = FileUtils.getFile(folderPath);
        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            ConsoleMessenger.error("Failed to delete folder " + folderPath + ": " + e.getMessage());
            return false;
        }

        return true;
    }
}
