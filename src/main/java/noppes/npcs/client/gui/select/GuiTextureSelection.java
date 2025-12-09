package noppes.npcs.client.gui.select;

import com.google.common.collect.Lists;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.NPCResourceHelper;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GuiTextureSelection extends SubGuiInterface implements ICustomScrollListener {

    private final String up = "..<" + StatCollector.translateToLocal("gui.up") + ">..";
    private GuiCustomScroll scrollCategories;
    private GuiCustomScroll scrollTextures;

    private String location = "";
    private String selectedDomain;
    public ResourceLocation selectedResource;

    public boolean setNPCSkin = true;
    // Instance maps to be populated
    private final HashMap<String, List<TextureData>> domains = new HashMap<>();
    private final HashMap<String, TextureData> textures = new HashMap<>();

    // --- Cache static fields ---
    private static final long CACHE_DURATION = 180000L; // 3 minutes in milliseconds
    private static long lastCacheTime = 0;
    private static HashMap<String, List<TextureData>> cachedDomains = new HashMap<>();
    public static HashMap<String, TextureData> cachedTextures = new HashMap<>();

    public GuiTextureSelection(EntityNPCInterface npc, String texture) {
        this.npc = npc;
        drawDefaultBackground = false;
        title = "";
        setBackground("menubg.png");
        xSize = 366;
        ySize = 226;

        // First, try to use cached domain data if it exists and is recent
        long now = System.currentTimeMillis();
        if (cachedDomains != null && now - lastCacheTime < CACHE_DURATION) {
            domains.putAll(cachedDomains);
            textures.putAll(cachedTextures);
        } else {
            // Build the domains and textures maps
            SimpleReloadableResourceManager simplemanager = (SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
            Map<String, FallbackResourceManager> map = ObfuscationReflectionHelper.getPrivateValue(SimpleReloadableResourceManager.class, simplemanager, 2);
            HashSet<String> set = new HashSet<String>();
            for (String name : map.keySet()) {
                if (!(map.get(name) instanceof FallbackResourceManager))
                    continue;
                FallbackResourceManager manager = (FallbackResourceManager) map.get(name);
                List<IResourcePack> list = ObfuscationReflectionHelper.getPrivateValue(FallbackResourceManager.class, manager, 0);
                for (IResourcePack pack : list) {
                    if (pack instanceof AbstractResourcePack) {
                        AbstractResourcePack p = (AbstractResourcePack) pack;
                        File file = NPCResourceHelper.getPackFile(p);
                        if (file != null)
                            set.add(file.getAbsolutePath());
                    }
                }
            }
            for (String file : set) {
                File f = new File(file);
                if (f.isDirectory()) {
                    checkFolder(new File(f, "assets"), f.getAbsolutePath().length());
                } else {
                    progressFile(f);
                }
            }
            for (ModContainer mod : Loader.instance().getModList()) {
                if (mod.getSource().exists())
                    progressFile(mod.getSource());
            }
            ResourcePackRepository repos = Minecraft.getMinecraft().getResourcePackRepository();
            repos.updateRepositoryEntriesAll();
            List<ResourcePackRepository.Entry> list = repos.getRepositoryEntries();
            for (ResourcePackRepository.Entry entry : list) {
                File file = new File(repos.getDirResourcepacks(), entry.getResourcePackName());
                if (file.exists()) {
                    progressFile(file);
                }
            }
            checkFolder(new File(CustomNpcs.Dir, "assets"), CustomNpcs.Dir.getAbsolutePath().length());
            URL url = DefaultResourcePack.class.getResource("/");
            if (url != null) {
                File f = decodeFile(url.getFile());
                if (f.isDirectory()) {
                    checkFolder(new File(f, "assets"), url.getFile().length());
                } else {
                    progressFile(f);
                }
            }
            url = CraftingManager.class.getResource("/assets/.mcassetsroot");
            if (url != null) {
                File f = decodeFile(url.getFile());
                if (f.isDirectory()) {
                    checkFolder(new File(f, "assets"), url.getFile().length());
                } else {
                    progressFile(f);
                }
            }
            // Update the cache
            cachedDomains = new HashMap<>(domains);
            cachedTextures = new HashMap<>(textures);
            lastCacheTime = now;
        }

        if (texture != null && !texture.isEmpty()) {
            selectedResource = new ResourceLocation(texture);
            selectedDomain = selectedResource.getResourceDomain();
            if (!domains.containsKey(selectedDomain)) {
                selectedDomain = null;
            }
            int i = selectedResource.getResourcePath().lastIndexOf('/');
            location = selectedResource.getResourcePath().substring(0, i + 1);
        }

        xOffsetNpc = 307;
        yOffsetNpc = 150;
        zoomed = 1.5f;
    }

    private File decodeFile(String url) {
        if (url.startsWith("file:")) {
            url = url.substring(5);
        }
        url = url.replace('/', File.separatorChar);
        int i = url.indexOf('!');
        if (i > 0) {
            url = url.substring(0, i);
        }
        try {
            url = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ignored) {
        }
        return new File(url);
    }

    @Override
    public void initGui() {
        super.initGui();

        if (selectedDomain != null) {
            title = selectedDomain + ":" + location;
        } else {
            title = "";
        }

        this.addButton(new GuiNpcButton(2, guiLeft + 264, guiTop + 170, 90, 20, "gui.done"));
        this.addButton(new GuiNpcButton(1, guiLeft + 264, guiTop + 190, 90, 20, "gui.cancel"));

        if (scrollCategories == null) {
            scrollCategories = new GuiCustomScroll(this, 0);
            scrollCategories.setSize(120, 200);
        }
        if (selectedDomain == null) {
            scrollCategories.setList(Lists.newArrayList(domains.keySet()));
            if (selectedDomain != null) {
                scrollCategories.setSelected(selectedDomain);
            }
        } else {
            List<String> list = new ArrayList<String>();
            list.add(up);
            List<TextureData> data = domains.get(selectedDomain);
            for (TextureData td : data) {
                if (location.isEmpty() || td.path.startsWith(location) && !td.path.equals(location)) {
                    String path = td.path.substring(location.length());
                    int i = path.indexOf('/');
                    if (i < 0)
                        continue;
                    path = path.substring(0, i);
                    if (!path.isEmpty() && !list.contains(path)) {
                        list.add(path);
                    }
                }
            }
            scrollCategories.setList(list);
        }
        scrollCategories.guiLeft = guiLeft + 4;
        scrollCategories.guiTop = guiTop + 14;
        this.addScroll(scrollCategories);

        if (scrollTextures == null) {
            scrollTextures = new GuiCustomScroll(this, 1);
            scrollTextures.setSize(130, 200);
        }
        if (selectedDomain != null) {
            textures.clear();
            List<TextureData> data = domains.get(selectedDomain);
            List<String> list = new ArrayList<String>();
            String loc = location;
            if (scrollCategories.hasSelected() && !scrollCategories.getSelected().equals(up)) {
                loc += scrollCategories.getSelected() + '/';
            }
            for (TextureData td : data) {
                if (td.path.equals(loc) && !list.contains(td.name)) {
                    list.add(td.name);
                    textures.put(td.name, td);
                }
            }
            scrollTextures.setList(list);
        }
        if (selectedResource != null) {
            scrollTextures.setSelected(selectedResource.getResourcePath());
        }
        scrollTextures.guiLeft = guiLeft + 125;
        scrollTextures.guiTop = guiTop + 14;
        this.addScroll(scrollTextures);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        super.actionPerformed(guibutton);
        if (guibutton.id == 2 && selectedResource != null) {
            if (setNPCSkin)
                npc.display.setSkinTexture(selectedResource.toString());
        }
        npc.textureLocation = null;
        close();
        parent.initGui();
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);
        drawNpc(npc, i, j, f);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll == scrollTextures) {
            if (scroll.id == 1) {
                TextureData data = textures.get(scroll.getSelected());
                selectedResource = new ResourceLocation(selectedDomain, data.absoluteName);
                if (setNPCSkin)
                    npc.textureLocation = selectedResource;
            }
        } else {
            initGui();
            scrollTextures.resetScroll();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll == scrollCategories) {
            if (selectedDomain == null) {
                selectedDomain = selection;
            } else if (selection.equals(up)) {
                int i = location.lastIndexOf('/', location.length() - 2);
                if (i < 0) {
                    if (location.isEmpty()) {
                        selectedDomain = null;
                    }
                    location = "";
                } else {
                    location = location.substring(0, i + 1);
                }
            } else {
                location = location + selection + '/';
            }
            scrollCategories.selected = -1;
            scrollTextures.selected = -1;
            initGui();
        } else {
            if (setNPCSkin)
                npc.display.setSkinTexture(selectedResource.toString());
            close();
            parent.initGui();
        }
    }

    private void progressFile(File file) {
        try {
            if (!file.isDirectory() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))) {
                ZipFile zip = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipentry = entries.nextElement();
                    String entryName = zipentry.getName();
                    addFile(entryName);
                }
                zip.close();
            } else if (file.isDirectory()) {
                int length = file.getAbsolutePath().length();
                checkFolder(file, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkFolder(File file, int length) {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            String name = null;
            try {
                name = f.getAbsolutePath().substring(length);
                name = name.replace("\\", "/");
                if (!name.startsWith("/"))
                    name = "/" + name;
                if (f.isDirectory()) {
                    addFile(name + "/");
                    checkFolder(f, length);
                } else
                    addFile(name);
            } catch (Throwable e) {
                LogWriter.error("error with: " + name);
            }
        }
    }

    private void addFile(String name) {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (!name.startsWith("assets/") || !name.endsWith(".png")) {
            return;
        }
        name = name.substring(7);
        int i = name.indexOf('/');
        String domain = name.substring(0, i);
        name = name.substring(i + 1);

        List<TextureData> list = domains.get(domain);
        if (list == null) {
            domains.put(domain, list = new ArrayList<TextureData>());
        }
        boolean contains = false;
        for (TextureData data : list) {
            if (data.absoluteName.equals(name)) {
                contains = true;
                break;
            }
        }
        if (!contains)
            list.add(new TextureData(domain, name));
    }

    public void setLocation(String domain, String location) {
        selectedDomain = domain;
        this.location = location;
    }
    static class TextureData {
        String domain;
        String absoluteName;
        String name;
        String path;

        public TextureData(String domain, String absoluteName) {
            this.domain = domain;
            int i = absoluteName.lastIndexOf('/');
            name = absoluteName.substring(i + 1);
            path = absoluteName.substring(0, i + 1);
            this.absoluteName = absoluteName;
        }
    }
}
