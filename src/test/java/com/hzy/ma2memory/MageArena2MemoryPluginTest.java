package com.hzy.ma2memory;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MageArena2MemoryPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MageArena2MemoryPlugin.class);
		RuneLite.main(args);
	}
}