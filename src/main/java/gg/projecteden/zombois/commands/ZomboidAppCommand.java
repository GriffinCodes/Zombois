package gg.projecteden.zombois.commands;

import gg.projecteden.api.common.utils.Tasks;
import gg.projecteden.api.common.utils.TimeUtils.MillisTime;
import gg.projecteden.api.discord.appcommands.AppCommand;
import gg.projecteden.api.discord.appcommands.AppCommandEvent;
import gg.projecteden.api.discord.appcommands.AppCommandRegistry;
import gg.projecteden.api.discord.appcommands.annotations.Command;
import gg.projecteden.api.discord.appcommands.annotations.GuildCommand;
import gg.projecteden.api.discord.appcommands.annotations.RequiredRole;
import gg.projecteden.api.discord.appcommands.exceptions.AppCommandException;
import gg.projecteden.zombois.Config;
import gg.projecteden.zombois.Zombois;
import lombok.AllArgsConstructor;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gg.projecteden.zombois.Zombois.console;

@GuildCommand("948361092101455962")
@Command("Interact with the Project Zomboid server")
@RequiredRole("Zomboi")
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

	private static final String PASTE_URL = "https://paste.projecteden.gg/";
	private static final Pattern PASTE_URL_REGEX = Pattern.compile(PASTE_URL + "(\\w{5})\\..*");

	@AllArgsConstructor
	enum ConfigType {
		OPTIONS("options.ini"),
		SETTINGS("Server/{{SERVER}}.ini"),
		SANDBOXVARS("Server/{{SERVER}}_SandboxVars.lua"),
		SPAWNREGIONS("Server/{{SERVER}}_spawnregions.lua"),
		SPAWNPOINTS("Server/{{SERVER}}_spawnpoints.lua"),
		;

		private final String file;

		private static final String ZOMBOID_DIRECTORY = "/home/steam/Zomboid/";

		public String getPath() {
			return ZOMBOID_DIRECTORY + file.replaceAll("\\{\\{SERVER}}", Config.SERVER);
		}

		public String getPaste() {
			return PASTE_URL + "zomboid-" + name().toLowerCase() + "." + getExtension();
		}

		private String getExtension() {
			return file.split("\\.", 2)[1];
		}
	}

	static {
		AppCommandRegistry.registerConverter(Enum.class, argument -> {
			String input = argument.getInput();
			final Class<?> type = argument.getMeta().getType();
			if (input == null) throw new AppCommandException("Missing argument");
			return Arrays.stream(type.getEnumConstants())
				.filter(constant -> ((Enum<?>) constant).name().equalsIgnoreCase(input))
				.findFirst()
				.orElseThrow(() -> new AppCommandException(type.getSimpleName() + " from &e" + input + " &cnot found"));
		});
	}

	@Command("Update a config file")
	void config_set(ConfigType config, String link) {
		Matcher matcher = PASTE_URL_REGEX.matcher(link);
		if (!matcher.find())
			throw new AppCommandException("New config must be a " + PASTE_URL + " link");

		String raw = PASTE_URL + "raw/" + matcher.group();
		try {
			Files.copy(new URL(raw).openStream(), Paths.get(config.getPath()), StandardCopyOption.REPLACE_EXISTING);
			reply(link + " copied to " + config.getPath() + ". Reboot server to apply changes.");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new AppCommandException(ex.getMessage());
		}
	}

	@Command("View a config file")
	void config_get(ConfigType config) {
		reply(config.getPaste());
	}

}
