package net.minecraft.client.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ChatFormat;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ResourcePackRepository {
    private static final Logger logger = LogManager.getLogger();
    private static final FileFilter resourcePackFilter = p_accept_1_ -> {
        boolean flag = p_accept_1_.isFile() && p_accept_1_.getName().endsWith(".zip");
        boolean flag1 = p_accept_1_.isDirectory() && (new File(p_accept_1_, "pack.mcmeta")).isFile();
        return flag || flag1;
    };
    public final IResourcePack rprDefaultResourcePack;
    public final IMetadataSerializer rprMetadataSerializer;
    private final File dirResourcepacks;
    private final File dirServerResourcepacks;
    private final ReentrantLock lock = new ReentrantLock();
    public List<ResourcePackRepository.Entry> repositoryEntries = new ArrayList<>();
    private IResourcePack resourcePackInstance;
    private ListenableFuture<Object> downloadingPacks;
    private List<ResourcePackRepository.Entry> repositoryEntriesAll = new ArrayList<>();

    public ResourcePackRepository(File dirResourcepacksIn, File dirServerResourcepacksIn, IResourcePack rprDefaultResourcePackIn, IMetadataSerializer rprMetadataSerializerIn, GameSettings settings) {
        dirResourcepacks = dirResourcepacksIn;
        dirServerResourcepacks = dirServerResourcepacksIn;
        rprDefaultResourcePack = rprDefaultResourcePackIn;
        rprMetadataSerializer = rprMetadataSerializerIn;
        fixDirResourcepacks();
        updateRepositoryEntriesAll();
        Iterator<String> iterator = settings.resourcePacks.iterator();

        while (iterator.hasNext()) {
            String s = iterator.next();

            for (ResourcePackRepository.Entry resourcepackrepository$entry : repositoryEntriesAll) {
                if (resourcepackrepository$entry.getResourcePackName().equals(s)) {
                    if (resourcepackrepository$entry.func_183027_f() == 1 || settings.incompatibleResourcePacks.contains(resourcepackrepository$entry.getResourcePackName())) {
                        repositoryEntries.add(resourcepackrepository$entry);
                        break;
                    }

                    iterator.remove();
                    logger.warn("Removed selected resource pack {} because it's no longer compatible", new Object[]{resourcepackrepository$entry.getResourcePackName()});
                }
            }
        }
    }

    private void fixDirResourcepacks() {
        if (dirResourcepacks.exists()) {
            if (!dirResourcepacks.isDirectory() && (!dirResourcepacks.delete() || !dirResourcepacks.mkdirs())) {
                logger.warn("Unable to recreate resourcepack folder, it exists but is not a directory: {}", dirResourcepacks);
            }
        } else if (!dirResourcepacks.mkdirs()) {
            logger.warn("Unable to create resourcepack folder: {}", dirResourcepacks);
        }
    }

    private List<File> getResourcePackFiles() {
        return dirResourcepacks.isDirectory() ? Arrays.asList(dirResourcepacks.listFiles(resourcePackFilter)) : Collections.emptyList();
    }

    public void updateRepositoryEntriesAll() {
        List<ResourcePackRepository.Entry> list = new ArrayList<>();

        for (File file1 : getResourcePackFiles()) {
            ResourcePackRepository.Entry resourcepackrepository$entry = new ResourcePackRepository.Entry(file1);

            if (!repositoryEntriesAll.contains(resourcepackrepository$entry)) {
                try {
                    resourcepackrepository$entry.updateResourcePack();
                    list.add(resourcepackrepository$entry);
                } catch (Exception var61) {
                    list.remove(resourcepackrepository$entry);
                }
            } else {
                int i = repositoryEntriesAll.indexOf(resourcepackrepository$entry);

                if (i > -1 && i < repositoryEntriesAll.size()) {
                    list.add(repositoryEntriesAll.get(i));
                }
            }
        }

        repositoryEntriesAll.removeAll(list);

        for (ResourcePackRepository.Entry resourcepackrepository$entry1 : repositoryEntriesAll) {
            resourcepackrepository$entry1.closeResourcePack();
        }

        repositoryEntriesAll = list;
    }

    public List<ResourcePackRepository.Entry> getRepositoryEntriesAll() {
        return ImmutableList.copyOf(repositoryEntriesAll);
    }

    public List<ResourcePackRepository.Entry> getRepositoryEntries() {
        return ImmutableList.copyOf(repositoryEntries);
    }

    public void setRepositories(List<ResourcePackRepository.Entry> repositories) {
        repositoryEntries.clear();
        repositoryEntries.addAll(repositories);
    }

    public File getDirResourcepacks() {
        return dirResourcepacks;
    }

    public ListenableFuture<Object> downloadResourcePack(String url, String hash) {
        String s;

        if (hash.matches("^[a-f0-9]{40}$")) {
            s = hash;
        } else {
            s = "legacy";
        }

        final File file1 = new File(dirServerResourcepacks, s);
        lock.lock();

        try {
            clearResourcePack();

            if (file1.exists() && hash.length() == 40) {
                try {
                    String s1 = Hashing.sha256().hashBytes(Files.toByteArray(file1)).toString();

                    if (s1.equals(hash)) {
                        return setResourcePackInstance(file1);
                    }

                    logger.warn("File {} had wrong hash (expected {}, found {}). Deleting it.", file1, hash, s1);
                    FileUtils.deleteQuietly(file1);
                } catch (IOException ioexception) {
                    logger.warn("File {} couldn't be hashed. Deleting it.", file1, ioexception);
                    FileUtils.deleteQuietly(file1);
                }
            }

            deleteOldServerResourcesPacks();
            final GuiScreenWorking guiscreenworking = new GuiScreenWorking();
            Map<String, String> map = Minecraft.getSessionInfo();
            final Minecraft minecraft = Minecraft.getMinecraft();
            Futures.getUnchecked(minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(guiscreenworking)));
            final SettableFuture<Object> settablefuture = SettableFuture.create();
            downloadingPacks = HttpUtil.downloadResourcePack(file1, url, map, 52428800, guiscreenworking, minecraft.getProxy());
            Futures.addCallback(downloadingPacks, new FutureCallback<>() {
                public void onSuccess(Object p_onSuccess_1_) {
                    setResourcePackInstance(file1);
                    settablefuture.set(null);
                }

                public void onFailure(Throwable p_onFailure_1_) {
                    settablefuture.setException(p_onFailure_1_);
                }
            }, Executors.newVirtualThreadPerTaskExecutor());
            return downloadingPacks;
        } finally {
            lock.unlock();
        }
    }

    private void deleteOldServerResourcesPacks() {
        List<File> list = Lists.newArrayList(FileUtils.listFiles(dirServerResourcepacks, TrueFileFilter.TRUE, null));
        list.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        int i = 0;

        for (File file1 : list) {
            if (i++ >= 10) {
                logger.info("Deleting old server resource pack {}", file1.getName());
                FileUtils.deleteQuietly(file1);
            }
        }
    }

    public ListenableFuture<Object> setResourcePackInstance(File resourceFile) {
        resourcePackInstance = new FileResourcePack(resourceFile);
        return Minecraft.getMinecraft().scheduleResourcesRefresh();
    }

    public IResourcePack getResourcePackInstance() {
        return resourcePackInstance;
    }

    public void clearResourcePack() {
        lock.lock();

        try {
            if (downloadingPacks != null) {
                downloadingPacks.cancel(true);
            }

            downloadingPacks = null;

            if (resourcePackInstance != null) {
                resourcePackInstance = null;
                Minecraft.getMinecraft().scheduleResourcesRefresh();
            }
        } finally {
            lock.unlock();
        }
    }

    public class Entry {
        private final File resourcePackFile;
        private IResourcePack reResourcePack;
        private PackMetadataSection rePackMetadataSection;
        private BufferedImage texturePackIcon;
        private ResourceLocation locationTexturePackIcon;

        private Entry(File resourcePackFileIn) {
            resourcePackFile = resourcePackFileIn;
        }

        public void updateResourcePack() throws IOException {
            reResourcePack = resourcePackFile.isDirectory() ? new FolderResourcePack(resourcePackFile) : new FileResourcePack(resourcePackFile);
            rePackMetadataSection = reResourcePack.getPackMetadata(rprMetadataSerializer, "pack");

            try {
                texturePackIcon = reResourcePack.getPackImage();
            } catch (IOException var2) {
            }

            if (texturePackIcon == null) {
                texturePackIcon = rprDefaultResourcePack.getPackImage();
            }

            closeResourcePack();
        }

        public void bindTexturePackIcon(TextureManager textureManagerIn) {
            if (locationTexturePackIcon == null) {
                locationTexturePackIcon = textureManagerIn.getDynamicTextureLocation("texturepackicon", new DynamicTexture(texturePackIcon));
            }

            textureManagerIn.bindTexture(locationTexturePackIcon);
        }

        public void closeResourcePack() {
            if (reResourcePack instanceof Closeable) {
                IOUtils.closeQuietly((Closeable) reResourcePack);
            }
        }

        public IResourcePack getResourcePack() {
            return reResourcePack;
        }

        public String getResourcePackName() {
            return reResourcePack.getPackName();
        }

        public String getTexturePackDescription() {
            return rePackMetadataSection == null ? ChatFormat.RED + "Invalid pack.mcmeta (or missing 'pack' section)" : rePackMetadataSection.packDescription().getFormattedText();
        }

        public int func_183027_f() {
            return rePackMetadataSection.packFormat();
        }

        public boolean equals(Object p_equals_1_) {
            return this == p_equals_1_ || (p_equals_1_ instanceof Entry && toString().equals(p_equals_1_.toString()));
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            return String.format("%s:%s:%d", resourcePackFile.getName(), resourcePackFile.isDirectory() ? "folder" : "zip", resourcePackFile.lastModified());
        }
    }
}
