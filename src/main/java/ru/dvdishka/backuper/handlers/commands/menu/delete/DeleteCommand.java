package ru.dvdishka.backuper.handlers.commands.menu.delete;

import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import ru.dvdishka.backuper.Backuper;
import ru.dvdishka.backuper.backend.classes.Backup;
import ru.dvdishka.backuper.backend.classes.LocalBackup;
import ru.dvdishka.backuper.backend.classes.SftpBackup;
import ru.dvdishka.backuper.backend.config.Config;
import ru.dvdishka.backuper.backend.tasks.local.folder.DeleteDirTask;
import ru.dvdishka.backuper.backend.common.Logger;
import ru.dvdishka.backuper.backend.common.Scheduler;
import ru.dvdishka.backuper.backend.utils.*;
import ru.dvdishka.backuper.handlers.commands.Command;
import ru.dvdishka.backuper.handlers.commands.status.StatusCommand;

import java.io.File;

public class DeleteCommand extends Command {

    private String storage= "";

    public DeleteCommand(String storage, CommandSender sender, CommandArguments arguments) {
        super(sender, arguments);

        this.storage = storage;
    }

    @Override
    public void execute() {

        String backupName = (String) arguments.get("backupName");

        if (storage.equals("local") && !LocalBackup.checkBackupExistenceByName(backupName) ||
                storage.equals("sftp") && !SftpBackup.checkBackupExistenceByName(backupName)) {
            cancelSound();
            returnFailure("Backup does not exist!");
            return;
        }

        Backup backup = null;

        if (storage.equals("local")) {
            backup = LocalBackup.getInstance(backupName);
        }
        if (storage.equals("sftp")) {
            backup = SftpBackup.getInstance(backupName);
        }

        if (Backuper.isLocked()) {
            cancelSound();
            returnFailure("Blocked by another operation!");
            return;
        }

        buttonSound();

        StatusCommand.sendTaskStartedMessage("Delete", sender);

        final Backup finalBackup = backup;

        Scheduler.getScheduler().runAsync(Utils.plugin, () -> {

            try {

                finalBackup.delete(true, sender);
                successSound();
                sendMessage("Delete task completed");

            } catch (Exception e) {

                Logger.getLogger().warn("Delete task has been finished with an exception!", sender);
                cancelSound();
            }
        });
    }
}
