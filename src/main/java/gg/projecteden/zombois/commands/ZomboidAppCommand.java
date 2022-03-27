package gg.projecteden.zombois.commands;

import gg.projecteden.discord.appcommands.AppCommand;
import gg.projecteden.discord.appcommands.AppCommandEvent;
import gg.projecteden.discord.appcommands.annotations.Command;
import gg.projecteden.discord.appcommands.annotations.GuildCommand;
import gg.projecteden.utils.Tasks;
import gg.projecteden.utils.TimeUtils.MillisTime;

import java.nio.file.Files;
import java.nio.file.Paths;

import static gg.projecteden.zombois.Zombois.RUN_DIRECTORY;
import static gg.projecteden.zombois.Zombois.console;
import static java.nio.charset.StandardCharsets.UTF_8;

@GuildCommand("948361092101455962")
@Command("Interact with the Project Zomboid server")
public class ZomboidAppCommand extends AppCommand {
	private static final Runnable start = () -> console("~/zomboid/start-server.sh -servername Eden_v2");

	public ZomboidAppCommand(AppCommandEvent event) {
		super(event);
	}

	@Command("Reboot the server")
	void reboot() {
		console("quit");
		Tasks.wait(MillisTime.SECOND.x(30), start);
		reply("Rebooting server, will take approximately 90 seconds");
	}

	@Command("Start the server")
	void start() {
		start.run();
		reply("Starting server, will take approximately 60 seconds");
	}

	@Command("Broadcast a message to the server")
	void broadcast(String message) {
		console("servermsg \"" + message + "\"");
		reply("Broadcasted message `" + message + "`");
	}

	@Command("List online players")
	void players() {
		console("players");
		event.getEvent().deferReply().queue(reply -> Tasks.wait(MillisTime.SECOND, () -> {
			try {
				final String tail = String.join("\n", Files.readAllLines(Paths.get(RUN_DIRECTORY + "server-console.txt"), UTF_8));
				final String[] split = tail.split("Players connected");
				final String list = split[split.length - 1].split("\n\n")[0];
				reply.editOriginal("Players online" + list.replaceFirst("\n-", "").replaceAll("\n-", ", ")).queue();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}));
	}

}
