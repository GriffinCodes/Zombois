package gg.projecteden.zombois.commands;

import gg.projecteden.discord.appcommands.AppCommand;
import gg.projecteden.discord.appcommands.AppCommandEvent;
import gg.projecteden.discord.appcommands.annotations.Command;
import gg.projecteden.discord.appcommands.annotations.GuildCommand;
import gg.projecteden.utils.Tasks;
import gg.projecteden.utils.TimeUtils.MillisTime;
import gg.projecteden.zombois.Config;
import gg.projecteden.zombois.Zombois;

import static gg.projecteden.zombois.Zombois.console;

@GuildCommand("948361092101455962")
@Command("Interact with the Project Zomboid server")
public class ZomboidAppCommand extends AppCommand {
	private static final Runnable start = () -> console("~/zomboid/start-server.sh -servername " + Config.SERVER);

	public ZomboidAppCommand(AppCommandEvent event) {
		super(event);
	}

	@Command("Stop the server")
	void stop() {
		console("quit");
		reply("Stopping server. will take approximately 30 seconds");
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
		event.getEvent().deferReply().queue(reply ->
			Zombois.getOnlineNerds().thenAccept(players ->
				reply.editOriginal(players).queue()));
	}

	@Command("Change server name")
	void settings_server(String server) {
		if (Config.SERVER.equals(server)) {
			reply("Already controlling server " + server);
			return;
		}

		reply("Stopping server " + Config.SERVER).thenRun(() -> {
			Config.SERVER = server;
			console("quit");
			reply("Now controlling server " + Config.SERVER);
		});
	}

}
