package ru.dvdishka.backuper.backend.common;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import ru.dvdishka.backuper.backend.config.Config;
import ru.dvdishka.backuper.backend.utils.UIUtils;
import ru.dvdishka.backuper.backend.utils.Utils;
import ru.dvdishka.backuper.handlers.commands.Permissions;

import java.util.ArrayList;
import java.util.Arrays;

public class Logger {

    public static Logger getLogger() {
        return new Logger();
    }

    public void log(String text) {
        Utils.plugin.getLogger().info(text);
    }

    public void log(String text, CommandSender sender) {

        Utils.plugin.getLogger().info(text);

        if (!(sender instanceof ConsoleCommandSender)) {
            try {
                UIUtils.sendMessage(text, sender);
            } catch (Exception ignored) {}
        }
    }

    public void devLog(String text) {
        if (Config.getInstance().isBetterLogging()) {
            Utils.plugin.getLogger().info(text);
        }
    }

    public void devLog(String text, CommandSender sender) {

        if (Config.getInstance().isBetterLogging()) {
            Utils.plugin.getLogger().info(text);

            if (!(sender instanceof ConsoleCommandSender)) {
                try {
                    UIUtils.sendMessage(text, sender);
                } catch (Exception ignored) {}
            }
        }
    }


    public void warn(String text) {
        Utils.plugin.getLogger().warning(text);
    }

    public void warn(String text, CommandSender sender) {

        Utils.plugin.getLogger().warning(text);

        if (!(sender instanceof ConsoleCommandSender)) {
            try {
                UIUtils.returnWarning(text, sender);
            } catch (Exception ignored) {}
        }
    }

    public void success(String text) {
        Utils.plugin.getLogger().info(text);
    }

    public void success(String text, CommandSender sender) {

        Utils.plugin.getLogger().info(text);

        if (!(sender instanceof ConsoleCommandSender)) {
            try {
                UIUtils.returnSuccess(text, sender);
            } catch (Exception ignored) {}
        }
    }

    public void devWarn(Object sourceClass, String text) {
        if (Config.getInstance().isBetterLogging()) {
            Utils.plugin.getLogger().warning(sourceClass.getClass().getSimpleName() + ": " + text);
        }
    }

    public void devWarn(String sourceClassName, String text) {
        if (Config.getInstance().isBetterLogging()) {
            Utils.plugin.getLogger().warning(sourceClassName + ": " + text);
        }
    }

    public void warn(Object sourceClass, Exception exception) {
        Utils.plugin.getLogger().warning(sourceClass.getClass().getSimpleName() + ": " + exception.getMessage() + "\n" + Arrays.toString(exception.getStackTrace()));
    }

    public void warn(String sourceClassName, Exception exception) {
        Utils.plugin.getLogger().warning(sourceClassName + ": " + exception.getMessage() + "\n" + Arrays.toString(exception.getStackTrace()));
    }
}
