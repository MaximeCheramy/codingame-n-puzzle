package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.Curve;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Sprite;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
	@Inject
	private SoloGameManager<Player> gameManager;
	@Inject
	private GraphicEntityModule graphicEntityModule;

	private List<Coord> validActions;
	private List<Integer> grid;
	private Group tileGroup;
	private List<Tile> tiles;

	private static final int ROWS = 3;
	private static final int COLUMNS = 4;
	private static final int WIDTH = 1920;
	private static final int HEIGHT = 1080;
	private static final int TILE_WIDTH = Math.round((WIDTH / COLUMNS) * .8f);
	private static final int TILE_HEIGHT = Math.round((HEIGHT / ROWS) * .8f);

	private class Tile {
		private Sprite mask;
		private Sprite img;
		private Sprite notMaskedImg;
		private Group group;

		public Tile(String image) {
			mask = graphicEntityModule.createSprite().setImage("mask.png").setBaseWidth(TILE_WIDTH)
					.setBaseHeight(TILE_HEIGHT);
			img = graphicEntityModule.createSprite().setImage(image).setMask(mask).setBaseWidth(TILE_WIDTH)
					.setBaseHeight(TILE_HEIGHT);
			notMaskedImg = graphicEntityModule.createSprite().setImage(image).setAlpha(0).setBaseWidth(TILE_WIDTH)
					.setBaseHeight(TILE_HEIGHT);
			group = graphicEntityModule.createGroup(mask, img, notMaskedImg);
		}

		public Tile setPosition(int x, int y, Curve curve) {
			group.setX(x, curve).setY(y, curve);
			return this;
		}

		public Group getGroup() {
			return group;
		}

		public Tile hideMask() {
			notMaskedImg.setAlpha(1);
			return this;
		}

		public Tile hide() {
			this.group.setAlpha(0);
			return this;
		}

		public Tile show() {
			this.group.setAlpha(1);
			return this;
		}
	}

	@Override
	public void init() {
		List<String> testCase = gameManager.getTestCase();

		Image image = Image.valueOf(testCase.get(0));
		grid = Stream.of(testCase.get(1).split(" ")).map(e -> Integer.valueOf(e)).collect(Collectors.toList());

		gameManager.setFrameDuration(500);
		gameManager.setMaxTurns(50);

		drawBackground();
		createGrid(image);

		validActions = getValidActions();
		sendInputs();
	}

	private void updatePosition() {
		tileGroup.setX((1920 - (COLUMNS * TILE_WIDTH)) / 2);
		tileGroup.setY((1080 - (ROWS * TILE_HEIGHT)) / 2);
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLUMNS; c++) {
				int i = grid.get(r * COLUMNS + c);
				if (i > 0) {
					Tile tile = tiles.get(i);
					tile.setPosition(c * TILE_WIDTH, r * TILE_HEIGHT, Curve.EASE_IN_AND_OUT);
				}
			}
		}
	}

	private void drawBackground() {
		graphicEntityModule.createSprite().setImage("Background.jpg").setAnchor(0);
	}

	private void createGrid(Image image) {
		String[] tiles2 = graphicEntityModule.createSpriteSheetLoader().setName("image")
				.setSourceImage(image.getFilename()).setWidth(1920 / COLUMNS).setHeight(1080 / ROWS)
				.setImageCount(ROWS * COLUMNS).setImagesPerRow(COLUMNS).setOrigCol(0).setOrigRow(0).load();

		tiles = Stream.of(tiles2).map(i -> new Tile(i)).collect(Collectors.toList());
		tiles.get(0).hide();
		tileGroup = graphicEntityModule.createGroup();
		tiles.forEach(t -> tileGroup.add(t.getGroup()));

		updatePosition();
	}

	private void sendInputs() {
		Player player = gameManager.getPlayer();
		for (int r = 0; r < ROWS; r++) {
			String line = IntStream.range(r * COLUMNS, (r + 1) * COLUMNS).mapToObj(i -> grid.get(i).toString())
					.collect(Collectors.joining(" "));

			player.sendInputLine(line);
		}
	}

	private Coord getEmptyPosition() {
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLUMNS; c++) {
				if (grid.get(r * COLUMNS + c) == 0) {
					return new Coord(r, c);
				}
			}
		}
		return null;
	}

	private List<Coord> getValidActions() {
		List<Coord> validActions = new ArrayList<>();

		Coord empty = getEmptyPosition();

		if (empty.row > 0) {
			validActions.add(new Coord(empty.row - 1, empty.col));
		}
		if (empty.row + 1 < ROWS) {
			validActions.add(new Coord(empty.row + 1, empty.col));
		}
		if (empty.col > 0) {
			validActions.add(new Coord(empty.row, empty.col - 1));
		}
		if (empty.col + 1 < COLUMNS) {
			validActions.add(new Coord(empty.row, empty.col + 1));
		}

		return validActions;
	}

	private boolean isWinner() {
		return IntStream.range(0, ROWS * COLUMNS).allMatch(i -> grid.get(i) == i);
	}

	@Override
	public void gameTurn(int turn) {
		Player player = gameManager.getPlayer();
		player.execute();

		// Read inputs
		try {
			if (isWinner()) {
				gameManager.setFrameDuration(1000);
				updatePosition();
				tiles.get(0).show();
				tiles.forEach(tile -> tile.hideMask());
				gameManager.winGame();
				return;
			}

			final Coord action = player.getAction();
			gameManager.addToGameSummary(
					String.format("Player %s played (%d %d)", player.getNicknameToken(), action.row, action.col));

			if (!validActions.contains(action)) {
				throw new InvalidAction("Invalid action.");
			}

			Coord other = getEmptyPosition();
			Collections.swap(grid, action.row * COLUMNS + action.col, other.row * COLUMNS + other.col);

			updatePosition();

			if (isWinner()) {
				player.setExpectedOutputLines(0);
			}

			validActions = getValidActions();
		} catch (TimeoutException e) {
			gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " timeout!"));
			gameManager.loseGame();
		} catch (InvalidAction e) {
			gameManager.addToGameSummary(
					GameManager.formatErrorMessage(player.getNicknameToken() + " eliminated: " + e.getMessage()));
			gameManager.loseGame();
		}
	}
}
