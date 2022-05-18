package com.hzy.ma2memory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "Mage Arena 2 Memory"
)
public class MageArena2MemoryPlugin extends Plugin
{
	public static final String DATA_FOLDER = "mage-arena-2-memory";
	public static File BOSS_HISTORY_DATA_DIR;
	public static final String BOSS_HISTORY_DATA_FNAME = "BossData.json";
	public static Gson GSON;
	static
	{
		BOSS_HISTORY_DATA_DIR = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER);
		BOSS_HISTORY_DATA_DIR.mkdirs();
	}

	@Inject
	private Client client;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	private ArrayList<MageArenaBoss> mageArenaBosses;
	private boolean imported, completed;

	@Override
	protected void startUp() throws Exception
	{
		GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		mageArenaBosses = new ArrayList<>();
		completed = false;
		if (client.getLocalPlayer() != null)
			importBossHistory();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Mage Arena 2 Memory stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) throws IOException {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN && !imported)
		{
			completed = false;
			importBossHistory();
		}
		else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
			imported = false;
			completed = false;
		}

	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (completed) { return; }
		Actor opp;
		if (event.getSource().equals(client.getLocalPlayer()))
		{
			opp = event.getTarget();
		}
		else if (event.getTarget().equals(client.getLocalPlayer()))
		{
			opp = event.getSource();
		}
		else
		{
			return;
		}
		String oppName = opp.getName();
		if(Objects.equals(oppName, "Porazdir") || Objects.equals(oppName, "Justiciar Zachariah") || Objects.equals(oppName, "Derwen")) {
			if(mageArenaBosses.size() > 0
					&& mageArenaBosses.stream().anyMatch(boss -> (Objects.equals(boss.getName(), oppName)
					&& Objects.equals(boss.getOwner(), client.getLocalPlayer().getName())))) {return;}

			addBoss(new MageArenaBoss(client.getLocalPlayer().getName(), oppName, opp.getWorldLocation()));
		}
		if(mageArenaBosses.size() == 3) {
			completed = true;
		}
	}


	private void addBoss(MageArenaBoss boss) {
		mageArenaBosses.add(boss);
		try {
			Writer writer = new FileWriter(new File(BOSS_HISTORY_DATA_DIR, BOSS_HISTORY_DATA_FNAME));
			GSON.toJson(mageArenaBosses.toArray(), MageArenaBoss[].class, writer);
			writer.flush();
			writer.close();
			drawBossesOnMap();
		}
		catch(IOException e) {
			log.warn("Error while writing to boss history data file: " + e.getMessage());
		}
	}

	private void importBossHistory() throws IOException {
		BOSS_HISTORY_DATA_DIR.mkdirs();
		File bossHistoryData = new File(BOSS_HISTORY_DATA_DIR, BOSS_HISTORY_DATA_FNAME);
		if (!bossHistoryData.exists())
		{
			Writer writer = new FileWriter(bossHistoryData);
			writer.write("[]");
			writer.close();
		}

		List<MageArenaBoss> savedBosses = Arrays.asList(GSON.fromJson(new FileReader(bossHistoryData), MageArenaBoss[].class));
		mageArenaBosses.clear();
		importBosses(savedBosses);
		imported = true;

	}

	private void importBosses(List<MageArenaBoss> savedBosses) throws NullPointerException {
		if(savedBosses == null || savedBosses.size() < 1) { return; }
		ArrayList<MageArenaBoss> bossesToAdd = new ArrayList<>();

		for(MageArenaBoss boss : savedBosses) {
			bossesToAdd.add(new MageArenaBoss(boss.getOwner(), boss.getName(),
					new WorldPoint(boss.getWorldLocation()[0], boss.getWorldLocation()[1], boss.getWorldLocation()[2])));
		}

		mageArenaBosses.addAll(bossesToAdd);
		if (mageArenaBosses.size() == 3) {completed = true;}
		drawBossesOnMap();
	}

	private void drawBossesOnMap() {
		mageArenaBosses.stream().filter(b -> !b.hasDrawn()).map(boss -> {
			boss.draw();
			return WorldMapPoint.builder()
					.worldPoint(boss.getWorldPoint())
					.image(boss.getMapImage())
					.tooltip(boss.getName())
					.build();
				}
		).forEach(worldMapPointManager::add);
	}

}
