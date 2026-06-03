package top.chancelethay.bingo.item;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.data.BingoMessage;
import top.chancelethay.bingo.data.BingoSound;
import top.chancelethay.bingo.data.BingoStatType;
import top.chancelethay.bingo.data.config.BingoConfigurationData;
import top.chancelethay.bingo.data.config.BingoOptions;
import top.chancelethay.bingo.gameloop.phase.BingoGame;
import top.chancelethay.bingo.lib.platform.PotionEffectInstance;
import top.chancelethay.bingo.lib.platform.StatusEffectType;
import top.chancelethay.bingo.lib.platform.WorldPosition;
import top.chancelethay.bingo.lib.platform.item.ItemType;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.lib.events.EventResult;
import top.chancelethay.bingo.lib.item.ItemTemplate;
import top.chancelethay.bingo.player.BingoParticipant;
import top.chancelethay.bingo.player.BingoPlayer;
import top.chancelethay.bingo.settings.PlayerKit;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class GoUpWand extends GameItem {

	public static Key ID = BingoReloaded.resourceKey("go_up_wand");

	public GoUpWand() {
		super(ID);
	}

	@Override
	public ItemTemplate defaultTemplate() {
		return new ItemTemplate(
				ItemType.of(Key.key("warped_fungus_on_a_stick")),
				BingoMessage.WAND_ITEM_NAME.asPhrase().color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD, TextDecoration.ITALIC),
				BingoMessage.WAND_ITEM_DESC.asMultiline())
				.addEnchantment(Key.key("minecraft:unbreaking"), 3);
	}

	@Override
	public EventResult<?> use(StackHandle stack, BingoParticipant participant, BingoConfigurationData config) {
		if (participant instanceof BingoPlayer player) {
			player.sessionPlayer().ifPresent(sessionPlayer -> {
				useGoUpWand(sessionPlayer, stack,
						config.getOptionValue(BingoOptions.GO_UP_WAND_COOLDOWN),
						config.getOptionValue(BingoOptions.GO_UP_WAND_DOWN_DISTANCE),
						config.getOptionValue(BingoOptions.GO_UP_WAND_UP_DISTANCE),
						config.getOptionValue(BingoOptions.GO_UP_WAND_PLATFORM_LIFETIME));
			});
		}

		return EventResult.CONSUME;
	}

	private void useGoUpWand(PlayerHandle player, StackHandle wand, double wandCooldownSeconds, int downDistance, int upDistance, int platformLifetimeSeconds) {
		if (player.hasCooldown(wand)) {
			return;
		}

		wand.setCooldown(PlayerKit.WAND_COOLDOWN_GROUP, wandCooldownSeconds);
		player.setCooldown(wand, (int)(wandCooldownSeconds * 20));

		BingoReloaded.runtime().getServerSoftware().runTask(task -> {
			double distance;
			double fallDistance;
			// Use the wand
			if (player.isSneaking()) {
				distance = -downDistance;
				fallDistance = 0.0;
			} else {
				distance = upDistance;
				fallDistance = 2.0;
			}

			WorldPosition teleportLocation = player.position();
			WorldPosition platformLocation = teleportLocation.clone().floor();
			teleportLocation.setY(teleportLocation.y() + distance + fallDistance);
			platformLocation.setY(platformLocation.y() + distance);

			BingoGame.spawnPlatform(platformLocation, 1, true);
			BingoReloaded.runtime().getServerSoftware().runTask((long) Math.max(0, platformLifetimeSeconds) * BingoReloaded.ONE_SECOND, laterTask -> {
				BingoGame.removePlatform(platformLocation, 1);
			});

			player.teleportBlocking(teleportLocation);
			player.playSound(BingoSound.GO_UP_WAND_USED.builder().build());

			player.addEffect(new PotionEffectInstance(StatusEffectType.of("minecraft:resistance"),
					BingoReloaded.ONE_SECOND * (platformLifetimeSeconds + 4))
					.setAmplifier(100)
					.setParticles(false));

			BingoReloaded.incrementPlayerStat(player, BingoStatType.WAND_USES);
		});
	}
}
