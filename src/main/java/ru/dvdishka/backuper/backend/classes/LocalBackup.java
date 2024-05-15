package ru.dvdishka.backuper.backend.classes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.dvdishka.backuper.backend.common.Logger;
import ru.dvdishka.backuper.backend.config.Config;
import ru.dvdishka.backuper.backend.config.LocalConfig;
import ru.dvdishka.backuper.backend.tasks.local.folder.DeleteDirTask;
import ru.dvdishka.backuper.backend.utils.UIUtils;
import ru.dvdishka.backuper.backend.utils.Utils;
import ru.dvdishka.backuper.handlers.commands.Permissions;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class LocalBackup implements Backup {

    private String backupName;
    private LocalDateTime backupLocalDateTime;

    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static final double zipCompressValue = 1.5;

    private static HashMap<String, LocalBackup> backups = new HashMap();

    public static LocalBackup getInstance(String backupName) {

        if (!checkBackupExistenceByName(backupName)) {
            return null;
        }
        if (backups.containsKey(backupName)) {
            return backups.get(backupName);
        }
        LocalBackup backup = new LocalBackup(backupName);
        backups.put(backupName, backup);
        return backup;
    }

    private LocalBackup(String backupName) {

        this.backupName = backupName;
        this.backupLocalDateTime = LocalDateTime.parse(backupName, dateTimeFormatter);
    }

    public static ArrayList<LocalBackup> getBackups() {

        ArrayList<LocalBackup> backups = new ArrayList<>();
        for (File file : Objects.requireNonNull(new File(Config.getInstance().getLocalConfig().getBackupsFolder()).listFiles())) {
            try {
                LocalBackup localBackup = LocalBackup.getInstance(file.getName().replace(".zip", ""));

                if (localBackup != null) {
                    backups.add(localBackup);
                }
            } catch (Exception ignored) {}
        }
        return backups;
    }

    public String getName() {
        return backupName;
    }

    public LocalDateTime getLocalDateTime() {
        return backupLocalDateTime;
    }

    public long getMBSize() {

        File backupsFolder = new File(Config.getInstance().getLocalConfig() .getBackupsFolder());
        String backupFilePath;

        if (backupsFolder.toPath().resolve(backupName).toFile().exists()) {
            backupFilePath = backupsFolder.toPath().resolve(backupName).toFile().getPath();
        } else {
            backupFilePath = backupsFolder.toPath().resolve(backupName).toFile().getPath() + ".zip";
        }

        long backupSize = Utils.getFileFolderByteSize(new File(backupFilePath));

        if (backupSize != 0) {
            backupSize /= (1024 * 1024);
        }

        return backupSize;
    }

    public long getByteSize(CommandSender sender) {

        File backupsFolder = new File(Config.getInstance().getLocalConfig().getBackupsFolder());
        String backupFilePath;

        if (backupsFolder.toPath().resolve(backupName).toFile().exists()) {
            backupFilePath = backupsFolder.toPath().resolve(backupName).toFile().getPath();
        } else {
            backupFilePath = backupsFolder.toPath().resolve(backupName).toFile().getPath() + ".zip";
        }

        return Utils.getFileFolderByteSize(new File(backupFilePath));
    }

    public String zipOrFolder() {

        File backupsFolder = new File(Config.getInstance().getLocalConfig().getBackupsFolder());
        String zipOrFolder = "(ZIP)";

        if (backupsFolder.toPath().resolve(backupName).toFile().exists()) {
            zipOrFolder = "(Folder)";
        }

        return zipOrFolder;
    }

    public File getFile() {

        File backupsFolder = new File(Config.getInstance().getLocalConfig().getBackupsFolder());

        if (this.zipOrFolder().equals("(ZIP)")) {
            return backupsFolder.toPath().resolve(backupName + ".zip").toFile();
        } else {
            return backupsFolder.toPath().resolve(backupName).toFile();
        }
    }

    public File getZIPFile() {

        File backupsFolder = new File(Config.getInstance().getLocalConfig().getBackupsFolder());

        if (backupsFolder.toPath().resolve(backupName + ".zip").toFile().exists()) {
            return backupsFolder.toPath().resolve(backupName + ".zip").toFile();
        }
        return null;
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    public static boolean checkBackupExistenceByName(String backupName) {

        try {
            LocalDateTime.parse(backupName, dateTimeFormatter);
        } catch (Exception e) {
            return false;
        }

        File backupsFolder = new File(Config.getInstance().getLocalConfig().getBackupsFolder());

        return backupsFolder.toPath().resolve(backupName).toFile().exists() ||
                backupsFolder.toPath().resolve(backupName + ".zip").toFile().exists();
    }

    public static void sendBackupAlert(long timeSeconds, String afterBackup) {

        String action = "backed\nup ";
        boolean restart = false;

        if (afterBackup.equals("STOP")) {
            Logger.getLogger().log("Server will be backed up and stopped in " + timeSeconds + " second(s)");
            action = "backed\nup and restarted\n";
            restart = true;
        }
        if (afterBackup.equals("RESTART")) {
            Logger.getLogger().log("Server will be backed up and restarted in " + timeSeconds + " second(s)");
            action = "backed\nup and restarted\n";
            restart = true;
        }
        if (afterBackup.equals("NOTHING")) {
            Logger.getLogger().log("Server will be backed up in " + timeSeconds + " second(s)");
            action = "backed\nup ";
        }

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!player.hasPermission(Permissions.ALERT.getPermission())) {
                continue;
            }

            if (restart || !Config.getInstance().isAlertOnlyServerRestart()) {

                Component header = Component.empty();

                header = header
                        .append(Component.text("Alert")
                                .decorate(TextDecoration.BOLD));

                Component message = Component.empty();

                message = message
                        .append(Component.text("Server will be " + action + "in "))
                        .append(Component.text(timeSeconds)
                                .color(NamedTextColor.RED)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" second(s)"));

                UIUtils.sendFramedMessage(header, message, 15, player);
                UIUtils.notificationSound(player);
            }
        }
    }

    public void delete(boolean setLocked, CommandSender sender) {

        new DeleteDirTask(this.getFile(), setLocked, sender).run();
    }
}
