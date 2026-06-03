package top.chancelethay.bingo.action;

import top.chancelethay.bingo.cards.CardSize;
import top.chancelethay.bingo.data.BingoCardData;
import top.chancelethay.bingo.data.BingoSettingsData;
import top.chancelethay.bingo.data.PlayerSerializationData;
import top.chancelethay.bingo.data.config.BingoConfigurationData;
import top.chancelethay.bingo.data.config.BingoOptions;
import top.chancelethay.bingo.data.helper.SerializablePlayer;
import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.gameloop.GameManager;
import top.chancelethay.bingo.gameloop.phase.PregameLobby;
import top.chancelethay.bingo.lib.action.ActionResult;
import top.chancelethay.bingo.lib.action.ActionTree;
import top.chancelethay.bingo.lib.platform.ServerSoftware;
import top.chancelethay.bingo.lib.platform.WorldHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.util.ConsoleMessenger;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.player.BingoPlayer;
import top.chancelethay.bingo.player.EffectOptionFlags;
import top.chancelethay.bingo.settings.BingoSettings;
import top.chancelethay.bingo.settings.BingoSettingsBuilder;
import top.chancelethay.bingo.settings.PlayerKit;
import top.chancelethay.bingo.settings.gamemode.BingoGamemodes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BingoAdminAction extends ActionTree {

	private final ServerSoftware platform;
	private final GameManager manager;

	public BingoAdminAction(ServerSoftware platform, GameManager manager) {
		super("bingoadmin", List.of("bingo.admin"));
		this.platform = platform;
		this.manager = manager;

		this.addSubAction(new ActionTree("start", args -> start()));

		this.addSubAction(new ActionTree("kit", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setKit(settings, args);
		}).addTabCompletion(args ->
				List.of("hardcore", "normal", "overpowered", "reloaded",
						"custom_1", "custom_2", "custom_3", "custom_4", "custom_5")
		).addUsage("<kit_name>"));

		this.addSubAction(new ActionTree("effects", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setEffect(settings, args);
		}).addUsage("<effect_name> [true | false]")
				.addTabCompletion(args -> {
					if (args.length <= 1) {
						List<String> effects = Arrays.stream(EffectOptionFlags.values())
								.map(v -> v.toString().toLowerCase())
								.collect(Collectors.toList());
						effects.add("none");
						effects.add("all");
						return effects;
					} else if (args.length == 2) {
						if (!args[0].equals("none") && !args[0].equals("all")) {
							return List.of("true", "false");
						}
					}
					return List.of();
				}));

		this.addSubAction(new ActionTree("card", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setCard(settings, args);
		}).addUsage("<card_name>"));

		this.addSubAction(new ActionTree("excludetags", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setCardExcludedTags(settings, args);
		}).addUsage("<excluded_tags>"));

		this.addSubAction(new ActionTree("countdown", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setCountdown(settings, args);
		}).addUsage("<type>")
				.addTabCompletion(args -> args.length == 1 ? List.of("disabled", "duration", "time_limit") : List.of()));

		this.addSubAction(new ActionTree("duration", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setDuration(settings, args);
		}).addUsage("<duration_minutes>"));

		this.addSubAction(new ActionTree("team", args -> setPlayerTeam(args))
				.addUsage("<player_name> <team_name>")
				.addTabCompletion(args -> args.length == 1 || args.length == 2 ? List.of("") : List.of()));

		this.addSubAction(new ActionTree("teamsize", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setTeamSize(settings, args);
		}).addUsage("<size>"));

		this.addSubAction(new ActionTree("teamcount", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setTeamCount(settings, args);
		}).addUsage("<count>"));

		this.addSubAction(new ActionTree("gamemode", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setGamemode(settings, args);
		}).addUsage("<regular | lockout | complete | hotswap | blitz> [3 | 5]")
				.addTabCompletion(args -> switch (args.length) {
					case 1 -> List.of("regular", "lockout", "complete", "hotswap", "blitz");
					case 2 -> List.of("3", "5");
					default -> List.of();
				}));

		this.addSubAction(new ActionTree("hotswap_goal", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setHotswapGoal(settings, args);
		}).addUsage("<win_goal>"));

		this.addSubAction(new ActionTree("hotswap_expire", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setHotswapExpire(settings, args);
		}).addUsage("<true | false>")
				.addTabCompletion(args -> args.length == 1 ? List.of("true", "false") : List.of()));

		this.addSubAction(new ActionTree("complete_goal", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setCompleteGoal(settings, args);
		}).addUsage("<win_goal>"));

		this.addSubAction(new ActionTree("separate_cards", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return setDifferentCardPerTeam(settings, args);
		}).addUsage("<true | false>")
				.addTabCompletion(args -> args.length == 1 ? List.of("true", "false") : List.of()));

		this.addSubAction(new ActionTree("end", args -> end()));

		this.addSubAction(new ActionTree("preset", args -> {
			var settings = getSettingsBuilder();
			if (settings == null) {
				sendFailed("Bingo session is not set up.");
				return ActionResult.INCORRECT_USE;
			}
			return preset(settings, args);
		}).addUsage("<save | load | remove | default> <preset_name>")
				.addTabCompletion(args -> {
					BingoSettingsData settingsData = new BingoSettingsData();
					return switch (args.length) {
						case 1 -> List.of("save", "load", "remove", "default");
						case 2 -> new ArrayList<>(settingsData.getPresetNames());
						default -> List.of();
					};
				}));

		this.addSubAction(new ActionTree("addplayer", this::addPlayerToSession)
				.addUsage("<player_name>")
				.addTabCompletion(args -> args.length == 1 ? null : List.of()));

		this.addSubAction(new ActionTree("kickplayer", this::removePlayerFromSession)
				.addUsage("<player_name> <target_world_name>")
				.addTabCompletion(args -> {
					if (args.length == 1) {
						return null;
					} else if (args.length == 2) {
						return platform.getLoadedWorlds().stream().map(WorldHandle::name).toList();
					}
					return List.of();
				}));

		this.addSubAction(new ActionTree("kickplayers", this::removeAllPlayersFromSession)
				.addUsage("<target_world_name>")
				.addTabCompletion(args -> args.length == 1
						? platform.getLoadedWorlds().stream().map(WorldHandle::name).toList()
						: List.of()));

		this.addSubAction(new ActionTree("vote", this::voteForPlayer)
				.addUsage("<player_name> <vote_category> <vote_for>")
				.addTabCompletion(args -> {
					BingoConfigurationData.VoteList voteList = manager.getGameConfig().getOptionValue(BingoOptions.VOTE_LIST);
					if (args.length <= 1) {
						return null;
					} else if (args.length == 2) {
						return List.of("kits", "gamemodes", "cards", "cardsizes");
					} else if (args.length == 3) {
						return switch (args[1]) {
							case "kits" -> voteList.kits();
							case "gamemodes" -> voteList.gamemodes();
							case "cards" -> voteList.cards();
							case "cardsizes" -> voteList.cardSizes();
							default -> List.of();
						};
					}
					return List.of();
				}));

		this.addSubAction(new ActionTree("playerdata", this::playerDataCommand)
				.addUsage("<save | load | remove> <player_name>")
				.addTabCompletion(args -> {
					if (args.length <= 1) {
						return List.of("save", "load", "remove");
					} else if (args.length == 2) {
						return null;
					}
					return List.of();
				}));
	}

	private BingoSettingsBuilder getSettingsBuilder() {
		BingoSession session = manager.getSession();
		return session == null ? null : session.settingsBuilder;
	}

	public ActionResult start() {
		if (manager.startGame()) {
			sendSuccess("The game has started!");
			return ActionResult.SUCCESS;
		}
		sendFailed("Could not start game, see console for details.");
		return ActionResult.IGNORED;
	}

	public ActionResult setKit(BingoSettingsBuilder settings, String[] args) {
		if (args.length != 1) {
			sendFailed("Expected 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		PlayerKit kit = PlayerKit.fromConfig(args[0]);

		if (!kit.isValid()) {
			sendFailed("Cannot set kit to " + kit.getDisplayName() + ". This custom kit is not defined. To create custom kits first, use /bingo kit.");
			return ActionResult.IGNORED;
		}
		settings.kit(kit);
		sendSuccess("Kit set to " + kit.getDisplayName());
		return ActionResult.SUCCESS;
	}

	public ActionResult setEffect(BingoSettingsBuilder settings, String[] args) {
		if (args.length == 0) {
			sendFailed("Expected at least 1 argument!");
			return ActionResult.INCORRECT_USE;
		}
		String effect = args[0];
		boolean enable = args.length == 1 || !args[1].equals("false");

		if (effect.equals("all")) {
			settings.effects(EnumSet.allOf(EffectOptionFlags.class));
			sendSuccess("Updated active effects to " + EnumSet.allOf(EffectOptionFlags.class));
			return ActionResult.SUCCESS;
		} else if (effect.equals("none")) {
			settings.effects(EnumSet.noneOf(EffectOptionFlags.class));
			sendSuccess("Updated active effects to " + EnumSet.noneOf(EffectOptionFlags.class));
			return ActionResult.SUCCESS;
		}

		try {
			settings.toggleEffect(EffectOptionFlags.valueOf(effect.toUpperCase()), enable);
			sendSuccess("Updated active effects to " + settings.view().effects());
			return ActionResult.SUCCESS;
		} catch (IllegalArgumentException e) {
			sendFailed("Invalid effect: " + effect);
			return ActionResult.INCORRECT_USE;
		}
	}

	public ActionResult setCard(BingoSettingsBuilder settings, String[] args) {
		if (args.length == 0) {
			sendFailed("Expected at least 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		String cardName = args[0];
		int seed = args.length > 1 ? BingoAction.toInt(args[1], 0) : 0;

		BingoCardData cardsData = new BingoCardData();
		if (cardsData.getCardNames().contains(cardName)) {
			settings.cardName(cardName).cardSeed(seed);
			sendSuccess("Playing card set to " + cardName + " with" + (seed == 0 ? " no seed" : " seed " + seed));
			return ActionResult.SUCCESS;
		}
		sendFailed("No card named '" + cardName + "' was found!");
		return ActionResult.INCORRECT_USE;
	}

	public ActionResult setCardExcludedTags(BingoSettingsBuilder settings, String[] args) {
		BingoCardData cardsData = new BingoCardData();
		Set<String> allTags = cardsData.tags().getAllTags().keySet();

		Set<String> tags = Arrays.stream(args)
				.map(String::toLowerCase)
				.filter(allTags::contains)
				.collect(Collectors.toSet());

		settings.excludedTags(tags);
		sendSuccess("Set card to exclude tasks with the following tags: " + tags);
		return ActionResult.SUCCESS;
	}

	public ActionResult setCountdown(BingoSettingsBuilder settings, String[] args) {
		if (args.length != 1) {
			sendFailed("Expected 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		switch (args[0]) {
			case "true", "duration" -> settings.countdownType(BingoSettings.CountdownType.DURATION);
			case "false", "disabled" -> settings.countdownType(BingoSettings.CountdownType.DISABLED);
			case "time_limit" -> settings.countdownType(BingoSettings.CountdownType.TIME_LIMIT);
			default -> {
				sendFailed("Invalid countdown type '" + args[0] + "'");
				return ActionResult.INCORRECT_USE;
			}
		}
		sendSuccess("Set countdown type to " + args[0]);
		return ActionResult.SUCCESS;
	}

	public ActionResult setDuration(BingoSettingsBuilder settings, String[] args) {
		if (args.length != 1) {
			sendFailed("Expected 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		int gameDuration = BingoAction.toInt(args[0], 0);
		if (gameDuration > 0) {
			settings.countdownGameDuration(gameDuration);
			sendSuccess("Set game duration for countdown mode to " + gameDuration);
			return ActionResult.SUCCESS;
		}

		sendFailed("Cannot set duration to " + gameDuration);
		return ActionResult.INCORRECT_USE;
	}

	public ActionResult setPlayerTeam(String[] args) {
		if (args.length != 2) {
			sendFailed("Expected 2 arguments!");
			return ActionResult.INCORRECT_USE;
		}

		BingoSession session = manager.getSession();
		if (session == null) {
			sendFailed("Cannot add player to team, bingo session is not set up.");
			return ActionResult.INCORRECT_USE;
		}

		String playerName = args[0];
		String teamName = args[1];

		PlayerHandle player = platform.getPlayerFromName(playerName);
		if (player == null) {
			sendFailed("Cannot add " + playerName + " to team, player does not exist or is not online!");
			return ActionResult.IGNORED;
		}

		if (teamName.equalsIgnoreCase("none")) {
			BingoParticipant participant = session.teamManager.getPlayerAsParticipant(player);
			if (participant == null) {
				sendFailed(playerName + " did not join any teams!");
				return ActionResult.IGNORED;
			}
			session.teamManager.removeMemberFromTeam(participant);
			sendSuccess("Player " + playerName + " removed from all teams");
			return ActionResult.SUCCESS;
		}

		BingoParticipant participant = session.teamManager.getPlayerAsParticipant(player);
		if (participant == null) {
			participant = new BingoPlayer(player, session);
		}
		if (!session.teamManager.addMemberToTeam(participant, teamName)) {
			sendFailed("Player " + playerName + " could not be added to team " + teamName);
			return ActionResult.IGNORED;
		}
		sendSuccess("Player " + playerName + " added to team " + teamName);
		return ActionResult.SUCCESS;
	}

	public ActionResult setTeamSize(BingoSettingsBuilder settings, String[] args) {
		if (args.length != 1) {
			sendFailed("Expected 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		int teamSize = Math.min(64, Math.max(1, BingoAction.toInt(args[0], 1)));
		settings.maxTeamSize(teamSize);
		sendSuccess("Set maximum team size to " + teamSize + " players");
		return ActionResult.SUCCESS;
	}

	public ActionResult setTeamCount(BingoSettingsBuilder settings, String[] args) {
		if (args.length != 1) {
			sendFailed("Expected 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		int newCount = Math.min(64, Math.max(1, BingoAction.toInt(args[0], 1)));
		settings.maxTeamCount(newCount);
		sendSuccess("Set maximum team count to " + newCount + " teams");
		return ActionResult.SUCCESS;
	}

	public ActionResult setGamemode(BingoSettingsBuilder settings, String[] args) {
		if (args.length == 0) {
			sendFailed("Expected at least 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		var mode = BingoGamemodes.fromDataString(args[0], true);
		if (mode == null) {
			sendFailed("Unknown gamemode '" + args[0] + "'");
			return ActionResult.INCORRECT_USE;
		}
		settings.mode(mode);

		if (args.length == 2 && args[1].equals("3")) {
			settings.cardSize(CardSize.X3);
		} else {
			settings.cardSize(CardSize.X5);
		}

		BingoSettings view = settings.view();
		sendSuccess("Set gamemode to " + args[0] + " " + view.size().size + "x" + view.size().size);
		return ActionResult.SUCCESS;
	}

	public ActionResult setHotswapGoal(BingoSettingsBuilder settings, String[] args) {
		if (args.length == 0) {
			sendFailed("Expected at least 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		int goal;
		try {
			goal = Integer.parseInt(args[0]);
		} catch (NumberFormatException exception) {
			sendFailed("Invalid win goal amount '" + args[0] + "'");
			return ActionResult.INCORRECT_USE;
		}

		settings.hotswapGoal(goal);
		sendSuccess("Set hotswap goal to " + goal);
		return ActionResult.SUCCESS;
	}

	public ActionResult setHotswapExpire(BingoSettingsBuilder settings, String[] args) {
		if (args.length != 1) {
			sendFailed("Expected 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		boolean value = args[0].equals("true");
		settings.expireHotswapTasks(value);
		sendSuccess((value ? "Enabled" : "Disabled") + " hotswap task expiration");
		return ActionResult.SUCCESS;
	}

	public ActionResult setCompleteGoal(BingoSettingsBuilder settings, String[] args) {
		if (args.length == 0) {
			sendFailed("Expected at least 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		int goal;
		try {
			goal = Integer.parseInt(args[0]);
		} catch (NumberFormatException exception) {
			sendFailed("Invalid win goal amount '" + args[0] + "'");
			return ActionResult.INCORRECT_USE;
		}

		settings.completeGoal(goal);
		sendSuccess("Set complete goal to " + goal);
		return ActionResult.SUCCESS;
	}

	public ActionResult setDifferentCardPerTeam(BingoSettingsBuilder settings, String[] args) {
		if (args.length != 1) {
			sendFailed("Expected 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		boolean value = args[0].equals("true");
		settings.differentCardPerTeam(value);
		sendSuccess((value ? "Enabled" : "Disabled") + " separate cards per team");
		return ActionResult.SUCCESS;
	}

	public ActionResult end() {
		if (manager.endGame()) {
			sendSuccess("Game forcefully ended!");
			return ActionResult.SUCCESS;
		}
		sendFailed("Could not end the game, see console for details.");
		return ActionResult.IGNORED;
	}

	public ActionResult preset(BingoSettingsBuilder settingsBuilder, String[] args) {
		if (args.length != 2) {
			sendFailed("Expected 2 arguments!");
			return ActionResult.INCORRECT_USE;
		}

		String path = args[1];
		if (path.isBlank()) {
			sendFailed("Please enter a valid preset name");
			return ActionResult.INCORRECT_USE;
		}

		BingoSettingsData settingsData = new BingoSettingsData();
		switch (args[0]) {
			case "save" -> {
				settingsData.saveSettings(path, settingsBuilder.view());
				sendSuccess("Saved settings to '" + path + "'.");
			}
			case "load" -> {
				BingoSettings settings = settingsData.getSettings(path);
				if (settings == null) {
					sendFailed("Invalid settings path " + path);
					return ActionResult.IGNORED;
				}
				settingsBuilder.fromOther(settings);
				sendSuccess("Loaded settings from '" + path + "'.");
			}
			case "remove" -> {
				settingsData.removeSettings(path);
				sendSuccess("Removed settings preset '" + path + "'.");
			}
			case "default" -> {
				settingsData.setDefaultSettings(path);
				sendSuccess("Set '" + path + "' as default settings for new sessions.");
			}
		}

		return ActionResult.SUCCESS;
	}

	private ActionResult addPlayerToSession(String... args) {
		if (args.length != 1) {
			sendFailed("Expected 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		String playerName = args[0];
		PlayerHandle player = platform.getPlayerFromName(playerName);
		if (player == null) {
			sendFailed("Player " + playerName + " could not be found.");
			return ActionResult.IGNORED;
		}

		BingoSession session = manager.getSession();
		if (session == null || session.getOverworld() == null) {
			sendFailed("Could not teleport player, the bingo world is not set up.");
			return ActionResult.IGNORED;
		}

		player.teleportBlocking(session.getOverworld().spawnPoint());
		sendSuccess("Teleported " + playerName + " to the bingo world");
		return ActionResult.SUCCESS;
	}

	private ActionResult removePlayerFromSession(String... args) {
		if (args.length != 2) {
			sendFailed("Expected 2 arguments!");
			return ActionResult.INCORRECT_USE;
		}

		String playerName = args[0];
		PlayerHandle player = platform.getPlayerFromName(playerName);
		if (player == null) {
			sendFailed("Player " + playerName + " could not be found.");
			return ActionResult.IGNORED;
		}

		BingoSession session = manager.getSession();
		if (session == null || !session.ownsWorld(player.world())) {
			sendFailed("Player cannot be teleported: " + playerName + " is not in the bingo world.");
			return ActionResult.IGNORED;
		}

		String targetWorldName = args[1];
		WorldHandle world = platform.getWorld(targetWorldName);
		if (world == null) {
			sendFailed("Could not teleport " + playerName + " to invalid world " + targetWorldName + ".");
			return ActionResult.IGNORED;
		}

		boolean teleportSucceeded = player.teleportBlocking(world.spawnPoint());
		if (!manager.getGameConfig().getOptionValue(BingoOptions.SAVE_PLAYER_INFORMATION) && !teleportSucceeded) {
			sendFailed("Could not teleport " + playerName + " to " + targetWorldName + " because of some error.");
			return ActionResult.IGNORED;
		}
		sendSuccess("Teleported " + playerName + " to " + targetWorldName);
		return ActionResult.SUCCESS;
	}

	private ActionResult removeAllPlayersFromSession(String... args) {
		if (args.length != 1) {
			sendFailed("Expected 1 argument!");
			return ActionResult.INCORRECT_USE;
		}

		BingoSession session = manager.getSession();
		if (session == null) {
			sendFailed("Could not remove players, bingo session is not set up.");
			return ActionResult.IGNORED;
		}

		String targetWorldName = args[0];
		WorldHandle world = platform.getWorld(targetWorldName);
		if (world == null) {
			sendFailed("Could not teleport players to invalid world " + targetWorldName + ".");
			return ActionResult.IGNORED;
		}

		Set<PlayerHandle> allPlayers = session.getPlayersInWorld();
		int playerCount = allPlayers.size();
		int playersLeft = playerCount;
		for (PlayerHandle player : allPlayers) {
			if (!session.ownsWorld(player.world())) {
				ConsoleMessenger.log("Player '" + player.playerName() + "' cannot be kicked from the session.");
				continue;
			}
			boolean teleportSucceeded = player.teleportBlocking(world.spawnPoint());
			if (!manager.getGameConfig().getOptionValue(BingoOptions.SAVE_PLAYER_INFORMATION) && !teleportSucceeded) {
				ConsoleMessenger.bug("Could not teleport player '" + player.playerName() + "'(" + player.uniqueId() + ") for some reason", this);
				continue;
			}
			playersLeft--;
		}

		sendSuccess("Teleported " + (playerCount - playersLeft) + " out of " + playerCount + " players to " + targetWorldName);
		return ActionResult.SUCCESS;
	}

	private ActionResult voteForPlayer(String[] args) {
		if (args.length != 3) {
			sendFailed("Expected 3 arguments!");
			return ActionResult.INCORRECT_USE;
		}

		BingoSession session = manager.getSession();
		if (session == null) {
			sendFailed("Cannot cast a vote, bingo session is not set up.");
			return ActionResult.IGNORED;
		}

		PlayerHandle player = platform.getPlayerFromName(args[0]);
		if (player == null) {
			sendFailed("Player '" + args[0] + "' does not exist!");
			return ActionResult.IGNORED;
		}

		String category = args[1];
		String voteFor = args[2];
		if (!(session.phase() instanceof PregameLobby lobby)) {
			sendFailed("Cannot vote for player, game is not in lobby phase.");
			return ActionResult.IGNORED;
		}

		BingoConfigurationData.VoteList voteList = manager.getGameConfig().getOptionValue(BingoOptions.VOTE_LIST);

		switch (category) {
			case "kits" -> {
				if (!voteList.kits().contains(voteFor)) {
					sendFailed("Cannot vote for kit " + voteFor + ", kit does not appear in vote list.");
					return ActionResult.INCORRECT_USE;
				}
				if (!PlayerKit.fromConfig(voteFor).isValid()) {
					sendFailed("Cannot vote for kit " + voteFor + ", because it does not exist.");
					return ActionResult.INCORRECT_USE;
				}
				lobby.voteKit(voteFor, player);
			}
			case "gamemodes" -> {
				if (!voteList.gamemodes().contains(voteFor)) {
					sendFailed("Cannot vote for gamemode " + voteFor + ", gamemode does not appear in vote list.");
					return ActionResult.INCORRECT_USE;
				}
				lobby.voteGamemode(voteFor, player);
			}
			case "cards" -> {
				if (!voteList.cards().contains(voteFor)) {
					sendFailed("Cannot vote for card " + voteFor + ", card does not appear in vote list.");
					return ActionResult.INCORRECT_USE;
				}
				lobby.voteCard(voteFor, player);
			}
			case "cardsizes" -> {
				if (!voteList.cardSizes().contains(voteFor)) {
					sendFailed("Cannot vote for card size " + voteFor + ", card size does not appear in vote list.");
					return ActionResult.INCORRECT_USE;
				}
				lobby.voteCardsize(voteFor, player);
			}
			default -> {
				sendFailed("Cannot vote for '" + category + "', category does not exist in the vote list!");
				return ActionResult.INCORRECT_USE;
			}
		}
		sendSuccess(player.displayName().append(Component.text(" voted for " + category + " " + voteFor)));
		return ActionResult.SUCCESS;
	}

	private ActionResult playerDataCommand(String... args) {
		if (args.length != 2) {
			return ActionResult.INCORRECT_USE;
		}

		String playerName = args[1];
		PlayerHandle player = platform.getPlayerFromName(playerName);
		if (player == null) {
			sendFailed("Cannot edit player data, player " + playerName + " not found");
			return ActionResult.IGNORED;
		}

		PlayerSerializationData playerData = manager.getPlayerData();
		return switch (args[0]) {
			case "load" -> {
				SerializablePlayer data = playerData.loadPlayer(player);
				if (data == null) {
					sendFailed("Cannot load player data, no data saved for " + playerName);
					yield ActionResult.IGNORED;
				}
				sendSuccess("Loaded player data for " + playerName);
				yield ActionResult.SUCCESS;
			}
			case "save" -> {
				SerializablePlayer data = SerializablePlayer.fromPlayer(platform, player);
				playerData.savePlayer(data, true);
				sendSuccess("Saved player data for " + playerName);
				yield ActionResult.SUCCESS;
			}
			case "remove" -> {
				playerData.removePlayer(player.uniqueId());
				sendSuccess("Removed previously saved player data for " + playerName);
				yield ActionResult.SUCCESS;
			}
			default -> ActionResult.INCORRECT_USE;
		};
	}

	private void sendSuccess(String message) {
		sendSuccess(Component.text(message));
	}

	private void sendFailed(String message) {
		sendFailed(Component.text(message));
	}

	private void sendSuccess(Component message) {
		getLastUser().sendMessage(message.color(NamedTextColor.GREEN));
	}

	private void sendFailed(Component message) {
		getLastUser().sendMessage(message.color(NamedTextColor.RED));
	}
}
