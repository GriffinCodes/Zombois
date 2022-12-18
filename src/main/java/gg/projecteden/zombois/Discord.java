package gg.projecteden.zombois;

import gg.projecteden.api.common.utils.ReflectionUtils;
import gg.projecteden.api.common.utils.Utils;
import gg.projecteden.api.discord.appcommands.AppCommand;
import gg.projecteden.api.discord.appcommands.AppCommandRegistry;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;
import java.util.stream.Stream;

public class Discord {
	public static JDA JDA;
	public static final String GUILD_ID = "948361092101455962";
	public static final String GENERAL_CHANNEL_ID = "948361092751556650";

	public static void init() {
		jda();
		Zombois.getCron().schedule("*/6 * * * *", Discord::updateTopic);
	}

	@SneakyThrows
	private static void jda() {
		JDA = JDABuilder.createDefault(Config.DISCORD_TOKEN)
			.addEventListeners(getListeners().toArray())
			.build()
			.awaitReady();

		final String commandsPackage = Zombois.class.getPackageName() + ".commands";
		new AppCommandRegistry(JDA, commandsPackage).registerAll();
	}

	private static void updateTopic() {
		Zombois.getOnlineNerds().thenAccept(topic -> {
			final TextChannel channel = JDA.getTextChannelById(GENERAL_CHANNEL_ID);
			if (channel == null)
				return;

			channel.getManager().setTopic(topic + timestamp()).queue();
		});
	}

	private static String timestamp() {
		return "%n%n%s".formatted("Last update: <t:" + System.currentTimeMillis() / 1000 + ">");
	}

	private static Stream<? extends ListenerAdapter> getListeners() {
		return ReflectionUtils.subTypesOf(ListenerAdapter.class, Zombois.class.getPackageName()).stream()
			.map(listener -> {
				try {
					if (Utils.canEnable(listener))
						return listener.getConstructor().newInstance();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return null;
			}).filter(Objects::nonNull);
	}

}
