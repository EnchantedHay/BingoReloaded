package top.chancelethay.bingo.player;

import top.chancelethay.bingo.cards.TaskCard;
import top.chancelethay.bingo.gameloop.BingoSession;
import top.chancelethay.bingo.lib.platform.item.StackHandle;
import top.chancelethay.bingo.lib.platform.player.PlayerHandle;
import top.chancelethay.bingo.player.team.BingoTeam;
import top.chancelethay.bingo.settings.PlayerKit;
import top.chancelethay.bingo.tasks.data.ItemTask;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public interface BingoParticipant extends ForwardingAudience.Single {

	BingoSession getSession();

	@Nullable
	BingoTeam getTeam();

	void setTeam(@Nullable BingoTeam team);

	UUID getId();

	Optional<PlayerHandle> sessionPlayer();

	String getName();

	Component getDisplayName();

	void showDeathMatchTask(ItemTask task);

	void showCard(ItemTask deathMatchTask);

	boolean alwaysActive();

	default int getAmountOfTaskCompleted() {
		if (getTeam() == null) {
			return 0;
		}

		Optional<TaskCard> card = getTeam().getCard();
		return card.map(taskCard -> taskCard.getCompleteCount(this)).orElse(0);
	}

	default Optional<TaskCard> getCard() {
		if (getTeam() == null) {
			return Optional.empty();
		}
		return getTeam().getCard();
	}

	void giveBingoCard(int cardSlot, @NotNull StackHandle cardItem);

	void giveEffects(EnumSet<EffectOptionFlags> effects, int gracePeriod);

	void takeEffects(boolean force);

	void giveKit(PlayerKit kit);
}
