package com.hzy.ma2memory;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

import javax.inject.Inject;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Mage Arena 2 Memory"
)
public class MageArena2MemoryPlugin extends Plugin
{
	public static final String DATA_FOLDER = "mage-arena-2-memory";
	public static File BOSS_HISTORY_DATA_DIR;
	public static final String BOSS_HISTORY_DATA_FNAME = "BossData.json";
	public static Image PLUGIN_ICON;

	static
	{
		BOSS_HISTORY_DATA_DIR = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER);
		BOSS_HISTORY_DATA_DIR.mkdirs();
	}

	@Inject
	private Client client;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	private Gson gson;
	private String user;
	private String oppName;
	private ArrayList<MageArenaBoss> bosses;
	private boolean imported, completed;

	@Override
	protected void startUp() throws Exception
	{

		imported = false;
		completed = false;

		gson = new Gson();
		bosses = new ArrayList<>();
		if(client.getLocalPlayer() != null) {
			try {
				importBossHistory();
				imported = true;
			}
			catch(Exception e) {
				imported = false;
				log.warn("Error while importing boss history data: " + e.getMessage());
			}
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Mage Arena 2 Memory stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if(imported) { return; }
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			user = client.getLocalPlayer().getName();
			importBossHistory();
			imported = true;
			drawBossesOnMap();

		}
		else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
			imported = false;
			completed = false;
		}

	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if(completed || !(event.getSource() instanceof Player) || !(event.getTarget() instanceof Player)) {
			return;
		}
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
		oppName = opp.getName();
		if(Objects.equals(oppName, "Porazdir") || Objects.equals(oppName, "Justiciar Zachariah") || Objects.equals(oppName, "Derwen")) {
			if(bosses.stream().anyMatch(boss -> boss.getName().equals(oppName) && boss.getOwner().equals(user))) {return;}
			addBoss(new MageArenaBoss(user, opp.getName(), opp.getWorldLocation()));
		}
		if(bosses.size() == 3) {
			completed = true;
		}
	}


	void addBoss(MageArenaBoss boss) {
		bosses.add(boss);
		try {
			File bossHistoryData = new File(BOSS_HISTORY_DATA_DIR, BOSS_HISTORY_DATA_FNAME);
			Writer writer = new FileWriter(bossHistoryData);
			gson.toJson(bosses, writer);
			writer.flush();
			writer.close();
		}
		catch(IOException e) {
			log.warn("Error while writing to boss history data file: " + e.getMessage());
		}
	}

	void importBossHistory() {
		try
		{
			BOSS_HISTORY_DATA_DIR.mkdirs();
			File bossHistoryData = new File(BOSS_HISTORY_DATA_DIR, BOSS_HISTORY_DATA_FNAME);

			if (!bossHistoryData.exists())
			{
				Writer writer = new FileWriter(bossHistoryData);
				writer.write("[]");
				writer.close();
				return;
			}

			List<MageArenaBoss> savedBosses = Arrays.asList(
					gson.fromJson(new FileReader(bossHistoryData), MageArenaBoss[].class));
			savedBosses = savedBosses.stream().filter(boss -> Objects.equals(boss.getOwner(), user)).collect(Collectors.toList());
			bosses.clear();
			importBosses(savedBosses);
		}
		catch (Exception e)
		{
			log.warn("Error while deserializing boss history data: " + e.getMessage());
		}
	}

	void importBosses(List<MageArenaBoss> bossesToAdd) throws NullPointerException {
		if(bosses == null || bosses.size() < 1) { return; }
		bossesToAdd.removeIf(Objects::isNull);
		bosses.addAll(bossesToAdd);
	}

	void drawBossesOnMap() {
		bosses.stream().filter(b -> !b.hasDrawn()).map(boss -> {
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
