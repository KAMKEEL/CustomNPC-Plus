package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import noppes.npcs.constants.EnumAuctionSort;

import java.util.regex.Pattern;

/**
 * Filter for auction listings.
 * Supports search by item name and sorting options.
 */
public class AuctionFilter {
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);

    public String searchText;
    public EnumAuctionSort sortBy;

    // Cached normalized search for efficiency
    private String normalizedSearch;

    public AuctionFilter() {
        this.searchText = "";
        this.sortBy = EnumAuctionSort.NEWEST;
        this.normalizedSearch = "";
    }

    public boolean hasSearchText() {
        return searchText != null && !searchText.trim().isEmpty();
    }

    /** Normalize string for search (strip colors, lowercase, trim) */
    public static String normalizeForSearch(String input) {
        if (input == null || input.isEmpty()) return "";
        String stripped = COLOR_CODE_PATTERN.matcher(input).replaceAll("");
        stripped = EnumChatFormatting.getTextWithoutFormattingCodes(stripped);
        return stripped.toLowerCase().trim();
    }

    /** Check if item name matches search text */
    public boolean matchesSearch(String itemName) {
        if (!hasSearchText()) return true;
        String normalized = normalizeForSearch(itemName);
        return normalized.contains(normalizedSearch);
    }

    /** Advanced search - all words must be present */
    public boolean matchesSearchAdvanced(String itemName) {
        if (!hasSearchText()) return true;
        String normalized = normalizeForSearch(itemName);
        String[] words = normalizedSearch.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty() && !normalized.contains(word)) {
                return false;
            }
        }
        return true;
    }

    public void reset() {
        searchText = "";
        sortBy = EnumAuctionSort.NEWEST;
        normalizedSearch = "";
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("SearchText", searchText != null ? searchText : "");
        compound.setInteger("SortBy", sortBy.ordinal());
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        searchText = compound.getString("SearchText");
        normalizedSearch = normalizeForSearch(searchText);
        sortBy = EnumAuctionSort.fromOrdinal(compound.getInteger("SortBy"));
    }

    public static AuctionFilter fromNBT(NBTTagCompound compound) {
        AuctionFilter filter = new AuctionFilter();
        filter.readFromNBT(compound);
        return filter;
    }

    public AuctionFilter copy() {
        AuctionFilter copy = new AuctionFilter();
        copy.searchText = this.searchText;
        copy.normalizedSearch = this.normalizedSearch;
        copy.sortBy = this.sortBy;
        return copy;
    }

    // Getters and setters
    public String getSearchText() { return searchText; }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
        this.normalizedSearch = normalizeForSearch(searchText);
    }

    public EnumAuctionSort getSortBy() { return sortBy; }
    public void setSortBy(EnumAuctionSort sortBy) { this.sortBy = sortBy; }
}
