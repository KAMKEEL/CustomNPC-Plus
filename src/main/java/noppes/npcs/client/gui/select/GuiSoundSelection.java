package noppes.npcs.client.gui.select;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GuiSoundSelection extends SubGuiInterface implements ICustomScrollListener {

    private GuiCustomScroll scrollCategories;
    private GuiCustomScroll scrollSounds;

    private String selectedDomain;
    public ResourceLocation selectedResource;

    private String catSearch = "";
    private String soundSearch = "";

    // Map: domain -> list of sound paths
    private HashMap<String, List<String>> domains = new HashMap<>();

    // --- Cache static fields ---
    private static final long CACHE_DURATION = 180000L; // 3 minutes in ms
    private static long lastCacheTime = 0;
    public static HashMap<String, List<String>> cachedDomains = new HashMap<>();

    public GuiSoundSelection(String sound) {
        drawDefaultBackground = false;
        title = "";
        setBackground("menubg.png");
        xSize = 366;
        ySize = 226;

        long now = System.currentTimeMillis();
        // Use cached data if it's fresh
        if (now - lastCacheTime < CACHE_DURATION && !cachedDomains.isEmpty()) {
            domains.putAll(cachedDomains);
        } else {
            // Otherwise, load domains from the SoundRegistry
            SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
            SoundRegistry registry = ReflectionHelper.getPrivateValue(SoundHandler.class, handler, 4);
            Set<ResourceLocation> set = registry.getKeys();
            for (ResourceLocation location : set) {
                List<String> list = domains.get(location.getResourceDomain());
                if (list == null) {
                    domains.put(location.getResourceDomain(), list = new ArrayList<String>());
                }
                list.add(location.getResourcePath());
            }
            // Update the static cache
            cachedDomains.clear();
            cachedDomains.putAll(domains);
            lastCacheTime = now;
        }
        if (sound != null && !sound.isEmpty()) {
            selectedResource = new ResourceLocation(sound);
            selectedDomain = selectedResource.getResourceDomain();
            if (!domains.containsKey(selectedDomain)) {
                selectedDomain = null;
            }
        }
    }

    @Override
    public void initGui(){
        super.initGui();
        this.addButton(new GuiNpcButton(2, guiLeft + xSize - 45, guiTop + ySize - 35, 40, 20, "gui.done"));
        this.addButton(new GuiNpcButton(1, guiLeft + 4, guiTop + ySize - 35, 70, 20, "gui.play"));
        getButton(1).enabled = selectedResource != null;

        if (scrollCategories == null) {
            scrollCategories = new GuiCustomScroll(this, 0);
            scrollCategories.setSize(90, 163);
        }
        scrollCategories.setList(Lists.newArrayList(domains.keySet()));
        if (selectedDomain != null) {
            scrollCategories.setSelected(selectedDomain);
        }
        scrollCategories.guiLeft = guiLeft + 4;
        scrollCategories.guiTop = guiTop + 4;
        this.addScroll(scrollCategories);
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 4, guiTop + 169, 90, 20, catSearch));

        if (scrollSounds == null) {
            scrollSounds = new GuiCustomScroll(this, 1);
            scrollSounds.setSize(265, 163);
        }
        if (selectedDomain != null) {
            scrollSounds.setList(domains.get(selectedDomain));
        }
        if (selectedResource != null) {
            scrollSounds.setSelected(selectedResource.getResourcePath());
        }
        scrollSounds.guiLeft = guiLeft + 95;
        scrollSounds.guiTop = guiTop + 4;
        this.addScroll(scrollSounds);

        addTextField(new GuiNpcTextField(66, this, fontRendererObj, guiLeft + 95, guiTop + 169, 265, 20, soundSearch));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        super.actionPerformed(guibutton);
        if (guibutton.id == 1) {
            MusicController.Instance.stopMusic();
            MusicController.Instance.playSound(selectedResource.toString(), (float)player.posX, (float)player.posY, (float)player.posZ);
        }
        if (guibutton.id == 2) {
            close();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (selectedResource == null)
            return;
        close();
    }

    @Override
    public void close() {
        super.close();
        MusicController.Instance.stopAllSounds();
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            selectedDomain = guiCustomScroll.getSelected();
            selectedResource = null;
            scrollSounds.selected = -1;
            scrollSounds.resetScroll();
            getTextField(66).setText("");
            soundSearch = "";
        }
        if (guiCustomScroll.id == 1) {
            selectedResource = new ResourceLocation(selectedDomain, guiCustomScroll.getSelected());
        }
        initGui();
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if(getTextField(55) != null){
            if(getTextField(55).isFocused()){
                if(catSearch.equals(getTextField(55).getText()))
                    return;
                catSearch = getTextField(55).getText().toLowerCase();
                scrollCategories.resetScroll();
                scrollCategories.setList(getCatSearch());
            }
        }
        if(getTextField(66) != null){
            if(getTextField(66).isFocused()){
                if(soundSearch.equals(getTextField(66).getText()))
                    return;
                soundSearch = getTextField(66).getText().toLowerCase();
                scrollSounds.resetScroll();
                scrollSounds.setList(getSoundSearch());
            }
        }
    }

    private List<String> getCatSearch(){
        if(catSearch.isEmpty()){
            return new ArrayList<String>(this.domains.keySet());
        }
        List<String> list = new ArrayList<String>();
        for(String name : this.domains.keySet()){
            if(name.toLowerCase().contains(catSearch))
                list.add(name);
        }
        return list;
    }

    private List<String> getSoundSearch(){
        if(selectedDomain == null){
            return new ArrayList<String>();
        }
        if(soundSearch.isEmpty()){
            return new ArrayList<String>(this.domains.get(selectedDomain));
        }
        List<String> list = new ArrayList<String>();
        for(String name : this.domains.get(selectedDomain)){
            if(name.toLowerCase().contains(soundSearch))
                list.add(name);
        }
        return list;
    }
}
