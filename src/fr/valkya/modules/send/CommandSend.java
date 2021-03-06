package fr.valkya.modules.send;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandSend extends Command implements TabExecutor {

	protected static class SendCallback {

		private final Map<ServerConnectRequest.Result, List<String>> results = new HashMap<>();
		private final CommandSender sender;
		private int count = 0;

		public SendCallback(CommandSender sender) {
			this.sender = sender;
			for (ServerConnectRequest.Result result : ServerConnectRequest.Result.values()) {
				results.put(result, new ArrayList<String>());
			}
		}

		public static class Entry implements Callback<ServerConnectRequest.Result> {

			private final SendCallback callback;
			private final ProxiedPlayer target;

			public Entry(SendCallback callback, ProxiedPlayer target) {
				this.callback = callback;
				this.target = target;
				this.callback.count++;
			}

			@Override
			public void done(ServerConnectRequest.Result result, Throwable error) {
				callback.results.get(result).add(target.getName());
			}
		}
	}

	public CommandSend() {
		super("send", "valkya.proxy.send");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length != 2) {
			sender.sendMessage(ProxyServer.getInstance().getTranslation("send_cmd_usage"));
			return;
		}
		ServerInfo server = ProxyServer.getInstance().getServerInfo(args[1]);
		if (server == null) {
			sender.sendMessage(ProxyServer.getInstance().getTranslation("no_server"));
			return;
		}

		List<ProxiedPlayer> targets;
		if (args[0].equalsIgnoreCase("all")) {
			targets = new ArrayList<>(ProxyServer.getInstance().getPlayers());
		} else if (args[0].equalsIgnoreCase("current")) {
			if (!(sender instanceof ProxiedPlayer)) {
				sender.sendMessage(ProxyServer.getInstance().getTranslation("player_only"));
				return;
			}
			ProxiedPlayer player = (ProxiedPlayer) sender;
			targets = new ArrayList<>(player.getServer().getInfo().getPlayers());
		} else {
			// If we use a server name, send the entire server. This takes priority over
			// players.
			ServerInfo serverTarget = ProxyServer.getInstance().getServerInfo(args[0]);
			if (serverTarget != null) {
				targets = new ArrayList<>(serverTarget.getPlayers());
			} else {
				ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
				if (player == null) {
					sender.sendMessage(ProxyServer.getInstance().getTranslation("user_not_online"));
					return;
				}
				targets = Collections.singletonList(player);
			}
		}

		final SendCallback callback = new SendCallback(sender);
		Iterator<ProxiedPlayer> iterator = targets.iterator();
		while (iterator.hasNext()) {
			ProxiedPlayer target = iterator.next();
			ServerConnectRequest request = ServerConnectRequest.builder().target(server)
					.reason(ServerConnectEvent.Reason.COMMAND).callback(new SendCallback.Entry(callback, target))
					.build();

			Thread t = new Thread(() -> {
				target.sendMessage(new TextComponent(ChatColor.GOLD + "Valkya »" + ChatColor.YELLOW
						+ " Connexion au serveur " + server.getName()));

				try {
					Thread.sleep(500 + (new Random().nextInt(500)));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				target.connect(request);
			});
			t.setDaemon(true);
			t.start();
		}

		sender.sendMessage(
				ChatColor.DARK_GREEN + "Attempting to send " + targets.size() + " players to " + server.getName());
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length > 2 || args.length == 0) {
			return ImmutableSet.of();
		}

		Set<String> matches = new HashSet<>();
		if (args.length == 1) {
			String search = args[0].toLowerCase(Locale.ROOT);
			for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
				if (player.getName().toLowerCase(Locale.ROOT).startsWith(search)) {
					matches.add(player.getName());
				}
			}
			if ("all".startsWith(search)) {
				matches.add("all");
			}
			if ("current".startsWith(search)) {
				matches.add("current");
			}
		} else {
			String search = args[1].toLowerCase(Locale.ROOT);
			for (String server : ProxyServer.getInstance().getServers().keySet()) {
				if (server.toLowerCase(Locale.ROOT).startsWith(search)) {
					matches.add(server);
				}
			}
		}
		return matches;
	}
}