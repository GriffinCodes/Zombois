package gg.projecteden.zombois.commands;

import gg.projecteden.discord.appcommands.AppCommand;
import gg.projecteden.discord.appcommands.AppCommandEvent;
import gg.projecteden.discord.appcommands.annotations.Command;
import gg.projecteden.discord.appcommands.annotations.GuildCommand;
import gg.projecteden.utils.Tasks;
import gg.projecteden.utils.TimeUtils.MillisTime;

import static gg.projecteden.zombois.Zombois.exec;

@GuildCommand("948361092101455962")
@Command("Interact with Project Zomboid server")
public class ZomboidAppCommand extends AppCommand {
	private static final Runnable start = () -> exec("./start-server.sh -servername Eden_v2");

	public ZomboidAppCommand(AppCommandEvent event) {
		super(event);
	}

	@Command("Reboot the server")
	void reboot() {
		exec("quit");
		Tasks.wait(MillisTime.SECOND.x(30), start);
		reply("Rebooting server");
	}

	@Command("Start the server")
	void start() {
		start.run();
		reply("Starting server");
	}

}
