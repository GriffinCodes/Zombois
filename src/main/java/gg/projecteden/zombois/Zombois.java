package gg.projecteden.zombois;

import gg.projecteden.EdenAPI;
import gg.projecteden.discord.appcommands.AppCommandRegistry;
import gg.projecteden.mongodb.DatabaseConfig;
import gg.projecteden.utils.Env;
import gg.projecteden.utils.Utils;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.util.Objects;
import java.util.stream.Stream;

public class Zombois extends EdenAPI {
	public static JDA JDA;
	public static final String ZOMBOIS_GUILD_ID = "948361092101455962";
	public static final String RUN_DIRECTORY = "/home/steam/Zomboid/";
	public static final String INSTALL_DIRECTORY = "/home/steam/zomboid/";

	public static void main(String[] args) {
		new Zombois();
	}

	public Zombois() {
		instance = this;

		try {
			jda();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@NotNull
	@SneakyThrows
	public static void console(String command) {
		Utils.bash("tmux send-keys -t zomboid.0 %s ENTER".formatted(command.replaceAll(" ", " SPACE ")));
	}

	@SneakyThrows
	private void jda() {
		JDA = JDABuilder.createDefault(Config.DISCORD_TOKEN)
			.addEventListeners(getListeners().toArray())
			.build()
			.awaitReady();

		final String commandsPackage = gg.projecteden.zombois.Zombois.class.getPackage().getName() + ".commands";
		new AppCommandRegistry(JDA, commandsPackage).registerAll();
	}

	@Override
	public Env getEnv() {
		return Env.PROD;
	}

	@Override
	public DatabaseConfig getDatabaseConfig() {
		return null;
	}

	private Stream<? extends ListenerAdapter> getListeners() {
		final Reflections reflections = new Reflections(gg.projecteden.zombois.Zombois.class.getPackage().getName());
		return reflections.getSubTypesOf(ListenerAdapter.class).stream().map(clazz -> {
			try {
				if (Utils.canEnable(clazz))
					return clazz.getConstructor().newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return null;
		}).filter(Objects::nonNull);
	}

}
