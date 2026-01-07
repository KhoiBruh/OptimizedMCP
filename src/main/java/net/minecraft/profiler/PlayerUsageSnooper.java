package net.minecraft.profiler;

import com.google.common.collect.Maps;
import net.minecraft.util.HttpUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public class PlayerUsageSnooper {
    private final Map<String, Object> snooperStats = Maps.newHashMap();
    private final Map<String, Object> clientStats = Maps.newHashMap();
    private final String uniqueID = UUID.randomUUID().toString();
    private final URL serverUrl;
    private final IPlayerUsage playerStatsCollector;
    private final Timer threadTrigger = new Timer("Snooper Timer", true);
    private final Object syncLock = new Object();
    private final long minecraftStartTimeMilis;
    private boolean isRunning;
    private int selfCounter;

    public PlayerUsageSnooper(String side, IPlayerUsage playerStatCollector, long startTime) {
        try {
            serverUrl = URI.create("http://snoop.minecraft.net/" + side + "?version=" + 2).toURL();
        } catch (MalformedURLException var6) {
            throw new IllegalArgumentException();
        }

        playerStatsCollector = playerStatCollector;
        minecraftStartTimeMilis = startTime;
    }

    public void startSnooper() {
        if (!isRunning) {
            isRunning = true;
            addOSData();
            threadTrigger.schedule(new TimerTask() {
                public void run() {
                    if (playerStatsCollector.isSnooperEnabled()) {
                        Map<String, Object> map;

                        synchronized (syncLock) {
                            map = Maps.newHashMap(clientStats);

                            if (selfCounter == 0) {
                                map.putAll(snooperStats);
                            }

                            map.put("snooper_count", selfCounter++);
                            map.put("snooper_token", uniqueID);
                        }

                        HttpUtil.postMap(serverUrl, map, true);
                    }
                }
            }, 0L, 900000L);
        }
    }

    private void addOSData() {
        addJvmArgsToSnooper();
        addClientStat("snooper_token", uniqueID);
        addStatToSnooper("snooper_token", uniqueID);
        addStatToSnooper("os_name", System.getProperty("os.name"));
        addStatToSnooper("os_version", System.getProperty("os.version"));
        addStatToSnooper("os_architecture", System.getProperty("os.arch"));
        addStatToSnooper("java_version", System.getProperty("java.version"));
        addClientStat("version", "1.8.9");
        playerStatsCollector.addServerTypeToSnooper(this);
    }

    private void addJvmArgsToSnooper() {
        RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
        List<String> list = runtimemxbean.getInputArguments();
        int i = 0;

        for (String s : list) {
            if (s.startsWith("-X")) {
                addClientStat("jvm_arg[" + i++ + "]", s);
            }
        }

        addClientStat("jvm_args", i);
    }

    public void addMemoryStatsToSnooper() {
        addStatToSnooper("memory_total", Runtime.getRuntime().totalMemory());
        addStatToSnooper("memory_max", Runtime.getRuntime().maxMemory());
        addStatToSnooper("memory_free", Runtime.getRuntime().freeMemory());
        addStatToSnooper("cpu_cores", Runtime.getRuntime().availableProcessors());
        playerStatsCollector.addServerStatsToSnooper(this);
    }

    public void addClientStat(String statName, Object statValue) {
        synchronized (syncLock) {
            clientStats.put(statName, statValue);
        }
    }

    public void addStatToSnooper(String statName, Object statValue) {
        synchronized (syncLock) {
            snooperStats.put(statName, statValue);
        }
    }

    public Map<String, String> getCurrentStats() {
        Map<String, String> map = Maps.newLinkedHashMap();

        synchronized (syncLock) {
            addMemoryStatsToSnooper();

            for (Entry<String, Object> entry : snooperStats.entrySet()) {
                map.put(entry.getKey(), entry.getValue().toString());
            }

            for (Entry<String, Object> entry1 : clientStats.entrySet()) {
                map.put(entry1.getKey(), entry1.getValue().toString());
            }

            return map;
        }
    }

    public boolean isSnooperRunning() {
        return isRunning;
    }

    public void stopSnooper() {
        threadTrigger.cancel();
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public long getMinecraftStartTimeMillis() {
        return minecraftStartTimeMilis;
    }
}
