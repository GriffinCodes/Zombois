package gg.projecteden.zombois;

import gg.projecteden.EdenAPI;
import gg.projecteden.mongodb.DatabaseConfig;
import gg.projecteden.utils.Env;
import gg.projecteden.utils.Tasks;
import gg.projecteden.utils.TimeUtils.MillisTime;
import gg.projecteden.utils.Utils;
import it.sauronsoftware.cron4j.Scheduler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Zombois extends EdenAPI {
	public static final String RUN_DIRECTORY = "/home/steam/Zomboid/";
	public static final String INSTALL_DIRECTORY = "/home/steam/zomboid/";

	@Getter
	private static final Scheduler cron = new Scheduler();

	public static void main(String[] args) {
		new Zombois();
	}

	public Zombois() {
		instance = this;

		try {
			cron.start();
			Discord.init();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@NotNull
	@SneakyThrows
	public static void console(String command) {
		Utils.bash("tmux send-keys -t zomboid.0 %s ENTER".formatted(command.replaceAll(" ", " SPACE ")));
	}

	public static CompletableFuture<String> getOnlineNerds() {
		var future = new CompletableFuture<String>();
		console("players");
		Tasks.wait(MillisTime.SECOND, () -> {
			try {
				final String tail = String.join("\n", Files.readAllLines(Paths.get(RUN_DIRECTORY + "server-console.txt"), UTF_8));
				final String[] split = tail.split("Players connected");
				final String list = split[split.length - 1].split("\n\n")[0];
				final List<String> players = Arrays.asList(list.split(": ", 2)[1].replaceFirst("\n-", "").split("\n-"));
				future.complete("Online Nerds (%d): %s".formatted(players.size(), String.join(", ", players)));
			} catch (Exception ex) {
				ex.printStackTrace();
				future.complete("Error: " + ex.getMessage());
			}
		});
		return future;
	}

	@Override
	public Env getEnv() {
		return Env.PROD;
	}

	@Override
	public DatabaseConfig getDatabaseConfig() {
		return null;
	}

}
