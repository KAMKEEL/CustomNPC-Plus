package noppes.npcs.client.gui.builder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.gui.components.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import noppes.npcs.client.gui.util.ModernColors;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Modern renderer that converts a list of {@link FieldDef} definitions into Modern GUI components.
 * <p>
 * This is the modern-component equivalent of {@link GuiFieldBuilder}. Both share {@link FieldDef}
 * definitions but render to different component systems:
 * <ul>
 *   <li>{@code GuiFieldBuilder} → legacy widgets ({@code GuiNpcButton}, {@code GuiScrollWindow})</li>
 *   <li>{@code ModernFieldPanel} → modern components ({@code ModernCheckbox}, {@code ModernDropdown})</li>
 * </ul>
 * <p>
 * Uses immediate data binding via FieldDef closures — changes write directly to the data model.
 * <p>
 * Supports collapsible sections, conditional visibility, two-column rows, and custom field types
 * (AVAILABILITY_ROW, FACTION_ROW).
 */
@SideOnly(Side.CLIENT)
public class ModernFieldPanel extends Gui {

    // === Layout constants ===
    protected int rowHeight = 18;
    protected int sectionGap = 3;
    protected int indent = 4;

    // === Field definitions ===
    protected List<FieldDef> fields = new ArrayList<>();

    // === Built components ===
    // Entries correspond 1:1 with fields list (null for non-renderable types like SECTION_HEADER)
    protected Map<FieldDef, FieldEntry> entries = new LinkedHashMap<>();

    // === Section tracking ===
    // Ordered list of sections (SECTION_HEADER defs that are collapsible)
    protected List<SectionInfo> sections = new ArrayList<>();

    // === All dropdowns for z-order ===
    protected List<ModernDropdown> allDropdowns = new ArrayList<>();

    // === Listener ===
    protected ModernFieldPanelListener listener;

    // === ID counter ===
    protected int nextId = 2000;

    // === Available height for fillHeight calculation ===
    protected int availableHeight = -1;
    protected int drawStartY;

    // ═══════════════════════════════════════════════════════════════════
    // SETUP
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Set the field definitions and build all Modern components.
     * Call this when the data object changes (e.g., new dialog selected).
     */
    public void setFields(List<FieldDef> fields) {
        this.fields = fields != null ? fields : new ArrayList<>();
        buildComponents();
    }

    public void setListener(ModernFieldPanelListener listener) {
        this.listener = listener;
    }

    /**
     * Set available height for fillHeight() text areas.
     */
    public void setAvailableHeight(int height) {
        this.availableHeight = height;
    }

    /**
     * Get ordered list of collapsible sections (for preserving expand state across rebuilds).
     */
    public List<SectionInfo> getSections() {
        return sections;
    }

    // ═══════════════════════════════════════════════════════════════════
    // COMPONENT BUILDING
    // ═══════════════════════════════════════════════════════════════════

    protected void buildComponents() {
        entries.clear();
        sections.clear();
        allDropdowns.clear();
        nextId = 2000;

        SectionInfo currentSection = null;

        for (FieldDef def : fields) {
            if (def.getType() == FieldType.SECTION_HEADER) {
                if (def.isCollapsible()) {
                    String sectionTitle = def.getTitleSupplier() != null
                        ? def.getTitleSupplier().get() : def.getLabel();
                    CollapsibleSection cs = new CollapsibleSection(nextId++, sectionTitle, def.isDefaultExpanded());
                    if (def.getRemoveAction() != null) {
                        cs.setOnRemove(def.getRemoveAction());
                    }
                    SectionInfo info = new SectionInfo(def, cs);
                    sections.add(info);
                    currentSection = info;
                } else {
                    currentSection = null;
                }
                entries.put(def, null);
                continue;
            }

            if (def.getType() == FieldType.ROW) {
                FieldEntry leftEntry = buildEntry(def.getLeftChild());
                FieldEntry rightEntry = buildEntry(def.getRightChild());
                FieldEntry rowEntry = new FieldEntry(def);
                rowEntry.leftChild = leftEntry;
                rowEntry.rightChild = rightEntry;
                entries.put(def, rowEntry);
                if (currentSection != null) currentSection.fields.add(def);
                continue;
            }

            FieldEntry entry = buildEntry(def);
            entries.put(def, entry);
            if (currentSection != null) currentSection.fields.add(def);
        }
    }

    protected FieldEntry buildEntry(FieldDef def) {
        if (def == null) return null;
        FieldEntry entry = new FieldEntry(def);

        switch (def.getType()) {
            case STRING: {
                ModernTextField tf = new ModernTextField(nextId++, 0, 0, 100, 16);
                if (def.getMaxLength() > 0) tf.setMaxLength(def.getMaxLength());
                if (def.getPlaceholder() != null && !def.getPlaceholder().isEmpty())
                    tf.setPlaceholder(def.getPlaceholder());
                String val = def.getValue() != null ? def.getValue().toString() : "";
                tf.setText(val);
                entry.textField = tf;
                break;
            }
            case INT: {
                int val = def.getValue() instanceof Number ? ((Number) def.getValue()).intValue() : 0;
                ModernNumberField nf = new ModernNumberField(nextId++, 0, 0, 50, 16, val);
                if (def.hasRange()) nf.setIntegerBounds((int) def.getMin(), (int) def.getMax(), val);
                entry.textField = nf;
                break;
            }
            case FLOAT: {
                float val = def.getValue() instanceof Number ? ((Number) def.getValue()).floatValue() : 0f;
                ModernNumberField nf = new ModernNumberField(nextId++, 0, 0, 50, 16, val);
                if (def.hasRange()) nf.setFloatBounds(def.getMin(), def.getMax(), val);
                entry.textField = nf;
                break;
            }
            case BOOLEAN: {
                boolean val = def.getValue() instanceof Boolean ? (Boolean) def.getValue() : false;
                ModernCheckbox cb = new ModernCheckbox(nextId++, 0, 0, val);
                entry.checkbox = cb;
                break;
            }
            case ENUM: {
                Class<? extends Enum<?>> ec = def.getEnumClass();
                if (ec != null) {
                    Enum<?>[] constants = ec.getEnumConstants();
                    List<String> names = new ArrayList<>();
                    for (Enum<?> c : constants) names.add(c.toString());
                    ModernDropdown dd = new ModernDropdown(nextId++, 0, 0, 80, 16);
                    dd.setOptions(names);
                    int selected = def.getValue() instanceof Enum ? ((Enum<?>) def.getValue()).ordinal() : 0;
                    dd.setSelectedIndex(selected);
                    entry.dropdown = dd;
                    allDropdowns.add(dd);
                }
                break;
            }
            case STRING_ENUM: {
                String[] values = def.getStringEnumValues();
                if (values != null && values.length > 0) {
                    ModernDropdown dd = new ModernDropdown(nextId++, 0, 0, 80, 16);
                    dd.setOptions(Arrays.asList(values));
                    String curVal = def.getStringEnumValue();
                    int selected = 0;
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].equals(curVal)) { selected = i; break; }
                    }
                    dd.setSelectedIndex(selected);
                    entry.dropdown = dd;
                    allDropdowns.add(dd);
                }
                break;
            }
            case TEXT_AREA: {
                ModernTextArea ta = new ModernTextArea(nextId++, 0, 0, 100, def.getTextAreaHeight());
                if (def.getMaxLength() > 0) ta.setMaxStringLength(def.getMaxLength());
                String val = def.getValue() != null ? def.getValue().toString() : "";
                ta.setText(val);
                entry.textArea = ta;
                break;
            }
            case COLOR: {
                int val = def.getValue() instanceof Number ? ((Number) def.getValue()).intValue() : 0xFFFFFF;
                ModernColorButton cb = new ModernColorButton(nextId++, 0, 0, 50, 16, val);
                entry.colorButton = cb;
                break;
            }
            case SELECT: {
                String display = def.getValue() != null ? def.getValue().toString() : "Select...";
                ModernSelectButton sb = new ModernSelectButton(nextId++, 0, 0, 100, 16, display);
                sb.setDisplayText(display);
                entry.selectButton = sb;
                break;
            }
            case AVAILABILITY_ROW: {
                buildAvailabilityRow(entry, def);
                break;
            }
            case FACTION_ROW: {
                buildFactionRow(entry, def);
                break;
            }
            case ACTION_BUTTON: {
                ModernButton btn = new ModernButton(nextId++, 0, 0, 80, 18, def.getLabel());
                entry.actionButton = btn;
                break;
            }
            default:
                break;
        }

        return entry;
    }

    protected void buildAvailabilityRow(FieldEntry entry, FieldDef def) {
        String[] conditions = def.getConditionValues();
        if (conditions == null) conditions = new String[]{"Always"};

        ModernDropdown condDd = new ModernDropdown(nextId++, 0, 0, 70, 16);
        condDd.setOptions(Arrays.asList(conditions));
        int condIdx = def.getConditionGetter() != null ? def.getConditionGetter().get() : 0;
        condDd.setSelectedIndex(condIdx);
        entry.dropdown = condDd;
        allDropdowns.add(condDd);

        String display = def.getDisplayNameGetter() != null ? def.getDisplayNameGetter().get() : "Select...";
        ModernSelectButton sb = new ModernSelectButton(nextId++, 0, 0, 80, 16);
        sb.setDisplayText(display != null && !display.isEmpty() ? display : "");
        entry.selectButton = sb;

        ModernButton clearBtn = new ModernButton(nextId++, 0, 0, 16, 16, "X");
        entry.clearButton = clearBtn;
    }

    protected void buildFactionRow(FieldEntry entry, FieldDef def) {
        String[] factionConds = {"Always", "Is", "Is Not"};
        ModernDropdown condDd = new ModernDropdown(nextId++, 0, 0, 55, 16);
        condDd.setOptions(Arrays.asList(factionConds));
        int condIdx = def.getConditionGetter() != null ? def.getConditionGetter().get() : 0;
        condDd.setSelectedIndex(condIdx);
        entry.dropdown = condDd;
        allDropdowns.add(condDd);

        String[] stances = {"Friendly", "Neutral", "Unfriendly"};
        ModernDropdown stanceDd = new ModernDropdown(nextId++, 0, 0, 60, 16);
        stanceDd.setOptions(Arrays.asList(stances));
        int stanceIdx = def.getStanceGetter() != null ? def.getStanceGetter().get() : 0;
        stanceDd.setSelectedIndex(stanceIdx);
        entry.stanceDropdown = stanceDd;
        allDropdowns.add(stanceDd);

        String display = def.getDisplayNameGetter() != null ? def.getDisplayNameGetter().get() : "";
        ModernSelectButton sb = new ModernSelectButton(nextId++, 0, 0, 60, 16);
        sb.setDisplayText(display != null && !display.isEmpty() ? display : "");
        entry.selectButton = sb;

        ModernButton clearBtn = new ModernButton(nextId++, 0, 0, 16, 16, "X");
        entry.clearButton = clearBtn;
    }

    // ═══════════════════════════════════════════════════════════════════
    // DRAWING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Draw all fields. Returns total content height.
     *
     * @param cx Content X position
     * @param cw Content width
     * @param startY Starting Y position
     * @param mouseX Mouse X (scroll-adjusted)
     * @param mouseY Mouse Y (scroll-adjusted)
     * @param fr FontRenderer
     * @return Total height drawn
     */
    public int draw(int cx, int cw, int startY, int mouseX, int mouseY, FontRenderer fr) {
        int y = startY;
        drawStartY = startY;
        SectionInfo currentSection = null;
        int sectionIdx = 0;

        for (FieldDef def : fields) {
            // Section header
            if (def.getType() == FieldType.SECTION_HEADER) {
                if (def.isCollapsible() && sectionIdx < sections.size()) {
                    currentSection = sections.get(sectionIdx++);
                    CollapsibleSection cs = currentSection.section;
                    // Sync dynamic title
                    if (def.getTitleSupplier() != null) {
                        cs.setTitle(def.getTitleSupplier().get());
                    }
                    cs.setPosition(cx, y, cw);

                    // Calculate content height for this section
                    int contentH = calculateSectionContentHeight(currentSection, cw, fr);
                    cs.setContentHeight(contentH);

                    if (cs.draw(mouseX, mouseY)) {
                        // Content is visible - draw fields inside
                        int dy = cs.getContentY();
                        dy = drawSectionFields(currentSection, cx, cw, dy, mouseX, mouseY, fr);
                    }
                    y += cs.getTotalHeight() + sectionGap;
                } else {
                    // Non-collapsible section header (plain label)
                    currentSection = null;
                    y += 3;
                    fr.drawString(def.getLabel(), cx + indent, y + 2, 0xFFFF55);
                    y += 15;
                }
                continue;
            }

            // Skip fields that belong to a collapsible section (drawn by the section)
            if (currentSection != null && currentSection.fields.contains(def)) {
                continue;
            }

            // Top-level field (not in any section)
            if (!def.isVisible()) continue;

            y = drawField(def, cx, cw, y, mouseX, mouseY, fr);
        }

        return y - startY;
    }

    protected int drawSectionFields(SectionInfo section, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        for (FieldDef def : section.fields) {
            if (!def.isVisible()) continue;
            y = drawField(def, cx, cw, y, mouseX, mouseY, fr);
        }
        return y;
    }

    protected int drawField(FieldDef def, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        FieldEntry entry = entries.get(def);
        if (entry == null) return y;

        switch (def.getType()) {
            case ROW:
                return drawRow(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case BOOLEAN:
                return drawBooleanField(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case STRING:
                return drawTextField(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case INT:
            case FLOAT:
                return drawNumberField(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case ENUM:
            case STRING_ENUM:
                return drawDropdownField(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case TEXT_AREA:
                return drawTextAreaField(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case COLOR:
                return drawColorField(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case SELECT:
                return drawSelectField(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case AVAILABILITY_ROW:
                return drawAvailabilityRow(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case FACTION_ROW:
                return drawFactionRow(def, entry, cx, cw, y, mouseX, mouseY, fr);
            case LABEL:
                return drawLabelField(def, cx, cw, y, fr);
            case ACTION_BUTTON:
                return drawActionButton(def, entry, cx, cw, y, mouseX, mouseY, fr);
            default: {
                int customH = drawCustomField(def, entry, cx, cw, y, mouseX, mouseY, fr);
                return customH >= 0 ? customH : y;
            }
        }
    }

    protected int drawBooleanField(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.checkbox == null) return y;
        // Sync value from data
        boolean val = def.getValue() instanceof Boolean ? (Boolean) def.getValue() : false;
        entry.checkbox.setValue(val);
        entry.checkbox.setEnabled(def.isEnabled());

        // Label on left, checkbox on right
        fr.drawString(def.getLabel(), cx + indent, y + 4, ModernColors.TEXT_LIGHT);
        int checkX = cx + cw - entry.checkbox.getSize() - 4;
        entry.checkbox.setPosition(checkX, y);
        entry.checkbox.draw(mouseX, mouseY);
        return y + rowHeight;
    }

    protected int drawTextField(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.textField == null) return y;
        entry.textField.setEnabled(def.isEnabled());

        boolean hasLabel = def.getLabel() != null && !def.getLabel().isEmpty();
        if (hasLabel) {
            fr.drawString(def.getLabel(), cx + indent, y + 4, ModernColors.TEXT_LIGHT);
            int labelW = fr.getStringWidth(def.getLabel()) + 8;
            entry.textField.setBounds(cx + indent + labelW, y, cw - indent - labelW - 4, 16);
        } else {
            entry.textField.setBounds(cx + indent, y, cw - indent * 2, 16);
        }
        entry.textField.draw(mouseX, mouseY);
        return y + rowHeight;
    }

    protected int drawNumberField(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.textField == null) return y;
        entry.textField.setEnabled(def.isEnabled());

        boolean hasLabel = def.getLabel() != null && !def.getLabel().isEmpty();
        if (hasLabel) {
            fr.drawString(def.getLabel(), cx + indent, y + 4, ModernColors.TEXT_LIGHT);
            int labelW = fr.getStringWidth(def.getLabel()) + 8;
            entry.textField.setBounds(cx + indent + labelW, y, 50, 16);
        } else {
            entry.textField.setBounds(cx + indent, y, 50, 16);
        }
        entry.textField.draw(mouseX, mouseY);
        return y + rowHeight;
    }

    protected int drawDropdownField(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.dropdown == null) return y;
        entry.dropdown.setEnabled(def.isEnabled());

        // Sync dropdown selection from data
        syncDropdownFromData(def, entry.dropdown);

        boolean hasLabel = def.getLabel() != null && !def.getLabel().isEmpty();
        if (hasLabel) {
            fr.drawString(def.getLabel(), cx + indent, y + 4, ModernColors.TEXT_LIGHT);
            entry.dropdown.setBounds(cx + cw - 80, y, 80, 16);
        } else {
            entry.dropdown.setBounds(cx + indent, y, cw - indent * 2, 16);
        }
        entry.dropdown.drawBase(mouseX, mouseY);
        return y + rowHeight;
    }

    protected int drawTextAreaField(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.textArea == null) return y;
        entry.textArea.setEnabled(def.isEnabled());

        boolean hasLabel = def.getLabel() != null && !def.getLabel().isEmpty();
        if (hasLabel) {
            fr.drawString(def.getLabel(), cx + indent, y, ModernColors.TEXT_LIGHT);
            y += 10;
        }

        int areaH = def.getTextAreaHeight();
        if (def.shouldFillHeight() && availableHeight > 0) {
            int consumed = y - drawStartY; // height used by fields above
            int overhead = (hasLabel ? 10 : 0) + (def.showsCharCount() ? 16 : 4);
            areaH = Math.max(80, availableHeight - consumed - overhead);
        }

        entry.textArea.setBounds(cx + indent, y, cw - indent * 2, areaH);
        entry.textArea.draw(mouseX, mouseY);
        y += areaH + 4;

        // Character count
        if (def.showsCharCount() && def.getMaxLength() > 0) {
            String charCount = entry.textArea.getCharacterCount() + " / " + entry.textArea.getMaxLength();
            int countW = fr.getStringWidth(charCount);
            int limit = def.getMaxLength();
            int count = entry.textArea.getCharacterCount();
            int countColor = count > (int)(limit * 0.9) ? ModernColors.ACCENT_RED :
                            count > (int)(limit * 0.75) ? ModernColors.ACCENT_ORANGE : ModernColors.TEXT_GRAY;
            fr.drawString(charCount, cx + cw - countW - indent, y, countColor);
            y += 12;
        }

        return y;
    }

    protected int drawColorField(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.colorButton == null) return y;
        // Sync color from data
        int val = def.getValue() instanceof Number ? ((Number) def.getValue()).intValue() : 0xFFFFFF;
        entry.colorButton.setColor(val);
        entry.colorButton.setEnabled(def.isEnabled());

        boolean hasLabel = def.getLabel() != null && !def.getLabel().isEmpty();
        if (hasLabel) {
            fr.drawString(def.getLabel(), cx + indent, y + 4, ModernColors.TEXT_LIGHT);
            int labelW = fr.getStringWidth(def.getLabel()) + 8;
            entry.colorButton.setBounds(cx + indent + labelW, y, 50, 16);
        } else {
            entry.colorButton.setBounds(cx + indent, y, 50, 16);
        }
        entry.colorButton.draw(mouseX, mouseY);
        return y + rowHeight;
    }

    protected int drawSelectField(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.selectButton == null) return y;
        entry.selectButton.setEnabled(def.isEnabled());

        // Sync display from data
        String display = def.getValue() != null ? def.getValue().toString() : "";
        entry.selectButton.setDisplayText(display);

        boolean hasLabel = def.getLabel() != null && !def.getLabel().isEmpty();
        int fieldX, fieldW;
        if (hasLabel) {
            fr.drawString(def.getLabel(), cx + indent, y + 4, ModernColors.TEXT_LIGHT);
            int labelW = fr.getStringWidth(def.getLabel()) + 8;
            fieldX = cx + indent + labelW;
            fieldW = cw - indent - labelW - 4;
        } else {
            fieldX = cx + indent;
            fieldW = cw - indent * 2;
        }

        // Account for clear button
        if (def.hasClearAction()) {
            entry.selectButton.setBounds(fieldX, y, fieldW - 20, 16);
            entry.selectButton.draw(mouseX, mouseY);
            // Draw clear button
            if (entry.clearButton == null) {
                entry.clearButton = new ModernButton(nextId++, 0, 0, 16, 16, "X");
            }
            entry.clearButton.xPosition = fieldX + fieldW - 16;
            entry.clearButton.yPosition = y;
            entry.clearButton.width = 16;
            entry.clearButton.height = 16;
            entry.clearButton.enabled = def.isEnabled();
            entry.clearButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        } else {
            entry.selectButton.setBounds(fieldX, y, fieldW, 16);
            entry.selectButton.draw(mouseX, mouseY);
        }
        return y + rowHeight;
    }

    protected int drawAvailabilityRow(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.dropdown == null || entry.selectButton == null) return y;

        // Sync condition from data
        int condIdx = def.getConditionGetter() != null ? def.getConditionGetter().get() : 0;
        entry.dropdown.setSelectedIndex(condIdx);
        boolean enabled = condIdx != 0;

        // Sync display name
        String display = def.getDisplayNameGetter() != null ? def.getDisplayNameGetter().get() : "";
        entry.selectButton.setDisplayText(display != null && !display.isEmpty() ? display : "");

        int ix = cx + indent;
        int iw = cw - indent - 4;

        // Condition dropdown
        entry.dropdown.setBounds(ix, y, 70, 16);
        entry.dropdown.drawBase(mouseX, mouseY);

        // Select button
        entry.selectButton.setEnabled(enabled);
        entry.selectButton.setBounds(ix + 74, y, iw - 94, 16);
        entry.selectButton.draw(mouseX, mouseY);

        // Clear button
        if (entry.clearButton != null) {
            entry.clearButton.xPosition = ix + iw - 16;
            entry.clearButton.yPosition = y;
            entry.clearButton.width = 16;
            entry.clearButton.height = 16;
            entry.clearButton.enabled = enabled;
            entry.clearButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        }

        return y + rowHeight + 2;
    }

    protected int drawFactionRow(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.dropdown == null || entry.selectButton == null) return y;

        // Sync condition from data
        int condIdx = def.getConditionGetter() != null ? def.getConditionGetter().get() : 0;
        entry.dropdown.setSelectedIndex(condIdx);
        boolean enabled = condIdx != 0;

        // Sync stance from data
        if (entry.stanceDropdown != null) {
            int stanceIdx = def.getStanceGetter() != null ? def.getStanceGetter().get() : 0;
            entry.stanceDropdown.setSelectedIndex(stanceIdx);
            entry.stanceDropdown.setEnabled(enabled);
        }

        // Sync display name
        String display = def.getDisplayNameGetter() != null ? def.getDisplayNameGetter().get() : "";
        entry.selectButton.setDisplayText(display != null && !display.isEmpty() ? display : "");

        int ix = cx + indent;
        int iw = cw - indent - 4;

        // Condition dropdown
        entry.dropdown.setBounds(ix, y, 55, 16);
        entry.dropdown.drawBase(mouseX, mouseY);

        // Stance dropdown
        if (entry.stanceDropdown != null) {
            entry.stanceDropdown.setBounds(ix + 58, y, 60, 16);
            entry.stanceDropdown.drawBase(mouseX, mouseY);
        }

        // Select button
        entry.selectButton.setEnabled(enabled);
        entry.selectButton.setBounds(ix + 122, y, iw - 142, 16);
        entry.selectButton.draw(mouseX, mouseY);

        // Clear button
        if (entry.clearButton != null) {
            entry.clearButton.xPosition = ix + iw - 16;
            entry.clearButton.yPosition = y;
            entry.clearButton.width = 16;
            entry.clearButton.height = 16;
            entry.clearButton.enabled = enabled;
            entry.clearButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        }

        return y + rowHeight + 2;
    }

    protected int drawLabelField(FieldDef def, int cx, int cw, int y, FontRenderer fr) {
        String text = def.getValue() != null ? def.getValue().toString() : "";
        boolean hasLabel = def.getLabel() != null && !def.getLabel().isEmpty();
        if (hasLabel) {
            fr.drawString(def.getLabel(), cx + indent, y + 4, ModernColors.TEXT_LIGHT);
            int labelW = fr.getStringWidth(def.getLabel()) + 8;
            fr.drawString(text, cx + indent + labelW, y + 4, ModernColors.TEXT_GRAY);
        } else {
            fr.drawString(text, cx + indent, y + 4, ModernColors.TEXT_GRAY);
        }
        return y + rowHeight;
    }

    protected int drawRow(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        FieldDef left = def.getLeftChild();
        FieldDef right = def.getRightChild();
        boolean leftVis = left != null && left.isVisible();
        boolean rightVis = right != null && right.isVisible();

        if (leftVis && rightVis) {
            int halfW = cw / 2;
            drawField(left, cx, halfW - 2, y, mouseX, mouseY, fr);
            drawField(right, cx + halfW + 2, halfW - 2, y, mouseX, mouseY, fr);
        } else if (leftVis) {
            drawField(left, cx, cw, y, mouseX, mouseY, fr);
        } else if (rightVis) {
            drawField(right, cx, cw, y, mouseX, mouseY, fr);
        }

        if (leftVis || rightVis) return y + rowHeight;
        return y;
    }

    protected int drawActionButton(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        if (entry.actionButton == null) return y;
        entry.actionButton.enabled = def.isEnabled();
        entry.actionButton.displayString = def.getLabel();

        int btnW = Math.min(100, cw - indent * 2);
        entry.actionButton.xPosition = cx + indent;
        entry.actionButton.yPosition = y;
        entry.actionButton.width = btnW;
        entry.actionButton.height = 18;
        entry.actionButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        return y + 22;
    }

    protected void syncDropdownFromData(FieldDef def, ModernDropdown dd) {
        if (def.getType() == FieldType.STRING_ENUM) {
            String curVal = def.getStringEnumValue();
            String[] values = def.getStringEnumValues();
            if (values != null && curVal != null) {
                for (int i = 0; i < values.length; i++) {
                    if (values[i].equals(curVal)) { dd.setSelectedIndex(i); break; }
                }
            }
        } else if (def.getType() == FieldType.ENUM) {
            int idx = def.getValue() instanceof Enum ? ((Enum<?>) def.getValue()).ordinal() : 0;
            dd.setSelectedIndex(idx);
        }
    }

    protected int calculateSectionContentHeight(SectionInfo section, int cw, FontRenderer fr) {
        int h = 0;
        for (FieldDef def : section.fields) {
            if (!def.isVisible()) continue;
            h += getFieldHeight(def, cw, fr);
        }
        return h + 4; // padding
    }

    protected int getFieldHeight(FieldDef def, int cw, FontRenderer fr) {
        switch (def.getType()) {
            case TEXT_AREA: {
                int h = def.getTextAreaHeight() + 4;
                if (def.getLabel() != null && !def.getLabel().isEmpty()) h += 10;
                if (def.showsCharCount()) h += 12;
                return h;
            }
            case AVAILABILITY_ROW:
            case FACTION_ROW:
                return rowHeight + 2;
            case ROW:
                return rowHeight;
            default:
                return rowHeight;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // EVENT HANDLING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Handle mouse click. Returns true if handled.
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        // Check section header clicks
        for (SectionInfo section : sections) {
            if (section.section.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Check fields
        for (Map.Entry<FieldDef, FieldEntry> e : entries.entrySet()) {
            FieldDef def = e.getKey();
            FieldEntry entry = e.getValue();
            if (entry == null || !def.isVisible()) continue;

            // Skip fields in collapsed sections
            if (isInCollapsedSection(def)) continue;

            if (handleFieldClick(def, entry, mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected boolean handleFieldClick(FieldDef def, FieldEntry entry, int mouseX, int mouseY, int button) {
        switch (def.getType()) {
            case ROW: {
                if (entry.leftChild != null && def.getLeftChild() != null && def.getLeftChild().isVisible()) {
                    if (handleFieldClick(def.getLeftChild(), entry.leftChild, mouseX, mouseY, button)) return true;
                }
                if (entry.rightChild != null && def.getRightChild() != null && def.getRightChild().isVisible()) {
                    if (handleFieldClick(def.getRightChild(), entry.rightChild, mouseX, mouseY, button)) return true;
                }
                return false;
            }
            case BOOLEAN: {
                if (entry.checkbox != null && entry.checkbox.mouseClicked(mouseX, mouseY, button)) {
                    def.setValue(entry.checkbox.getValue());
                    notifyChanged();
                    return true;
                }
                return false;
            }
            case STRING: {
                if (entry.textField != null && entry.textField.handleClick(mouseX, mouseY, button)) return true;
                return false;
            }
            case INT:
            case FLOAT: {
                if (entry.textField != null && entry.textField.handleClick(mouseX, mouseY, button)) return true;
                return false;
            }
            case ENUM: {
                if (entry.dropdown != null) {
                    int prev = entry.dropdown.getSelectedIndex();
                    if (entry.dropdown.mouseClicked(mouseX, mouseY, button)) {
                        if (entry.dropdown.getSelectedIndex() != prev) {
                            Class<? extends Enum<?>> ec = def.getEnumClass();
                            if (ec != null) {
                                Enum<?>[] constants = ec.getEnumConstants();
                                int idx = entry.dropdown.getSelectedIndex();
                                if (idx >= 0 && idx < constants.length) {
                                    def.setValue(constants[idx]);
                                }
                            }
                            notifyChanged();
                        }
                        return true;
                    }
                }
                return false;
            }
            case STRING_ENUM: {
                if (entry.dropdown != null) {
                    int prev = entry.dropdown.getSelectedIndex();
                    if (entry.dropdown.mouseClicked(mouseX, mouseY, button)) {
                        if (entry.dropdown.getSelectedIndex() != prev) {
                            String[] values = def.getStringEnumValues();
                            int idx = entry.dropdown.getSelectedIndex();
                            if (values != null && idx >= 0 && idx < values.length) {
                                def.setStringEnumValue(values[idx]);
                            }
                            notifyChanged();
                        }
                        return true;
                    }
                }
                return false;
            }
            case TEXT_AREA: {
                if (entry.textArea != null && entry.textArea.handleClick(mouseX, mouseY, button)) return true;
                return false;
            }
            case COLOR: {
                if (entry.colorButton != null && entry.colorButton.mouseClicked(mouseX, mouseY, button)) {
                    if (listener != null) {
                        int currentColor = def.getValue() instanceof Number ? ((Number) def.getValue()).intValue() : 0xFFFFFF;
                        listener.onColorSelect(def.getCallbackSlot(), currentColor);
                    }
                    return true;
                }
                return false;
            }
            case SELECT: {
                // Handle clear button first
                if (def.hasClearAction() && entry.clearButton != null) {
                    if (entry.clearButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                        def.getClearAction().run();
                        notifyChanged();
                        return true;
                    }
                }
                if (entry.selectButton != null && entry.selectButton.mouseClicked(mouseX, mouseY, button)) {
                    if (def.getOnActionCallback() != null) {
                        def.getOnActionCallback().run();
                        notifyChanged();
                    } else if (listener != null && !def.getCallbackAction().isEmpty()) {
                        listener.onSelectAction(def.getCallbackAction(), def.getCallbackSlot());
                    }
                    return true;
                }
                return false;
            }
            case AVAILABILITY_ROW: {
                return handleAvailabilityRowClick(def, entry, mouseX, mouseY, button);
            }
            case FACTION_ROW: {
                return handleFactionRowClick(def, entry, mouseX, mouseY, button);
            }
            case ACTION_BUTTON: {
                if (entry.actionButton != null && entry.actionButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                    if (def.getActionRunnable() != null) def.getActionRunnable().run();
                    notifyChanged();
                    return true;
                }
                return false;
            }
            default:
                return handleCustomFieldClick(def, entry, mouseX, mouseY, button);
        }
    }

    protected boolean handleAvailabilityRowClick(FieldDef def, FieldEntry entry, int mouseX, int mouseY, int button) {
        if (entry.dropdown != null) {
            int prev = entry.dropdown.getSelectedIndex();
            if (entry.dropdown.mouseClicked(mouseX, mouseY, button)) {
                if (entry.dropdown.getSelectedIndex() != prev) {
                    if (def.getConditionSetter() != null)
                        def.getConditionSetter().accept(entry.dropdown.getSelectedIndex());
                    notifyChanged();
                }
                return true;
            }
        }
        if (entry.selectButton != null && entry.selectButton.mouseClicked(mouseX, mouseY, button)) {
            if (listener != null && !def.getCallbackAction().isEmpty()) {
                listener.onSelectAction(def.getCallbackAction(), def.getCallbackSlot());
            }
            return true;
        }
        if (entry.clearButton != null && entry.clearButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            if (def.hasClearAction()) def.getClearAction().run();
            notifyChanged();
            return true;
        }
        return false;
    }

    protected boolean handleFactionRowClick(FieldDef def, FieldEntry entry, int mouseX, int mouseY, int button) {
        if (entry.dropdown != null) {
            int prev = entry.dropdown.getSelectedIndex();
            if (entry.dropdown.mouseClicked(mouseX, mouseY, button)) {
                if (entry.dropdown.getSelectedIndex() != prev) {
                    if (def.getConditionSetter() != null)
                        def.getConditionSetter().accept(entry.dropdown.getSelectedIndex());
                    notifyChanged();
                }
                return true;
            }
        }
        if (entry.stanceDropdown != null) {
            int prev = entry.stanceDropdown.getSelectedIndex();
            if (entry.stanceDropdown.mouseClicked(mouseX, mouseY, button)) {
                if (entry.stanceDropdown.getSelectedIndex() != prev) {
                    if (def.getStanceSetter() != null)
                        def.getStanceSetter().accept(entry.stanceDropdown.getSelectedIndex());
                    notifyChanged();
                }
                return true;
            }
        }
        if (entry.selectButton != null && entry.selectButton.mouseClicked(mouseX, mouseY, button)) {
            if (listener != null && !def.getCallbackAction().isEmpty()) {
                listener.onSelectAction(def.getCallbackAction(), def.getCallbackSlot());
            }
            return true;
        }
        if (entry.clearButton != null && entry.clearButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            if (def.hasClearAction()) def.getClearAction().run();
            notifyChanged();
            return true;
        }
        return false;
    }

    /**
     * Handle key typed. Returns true if handled.
     */
    public boolean keyTyped(char c, int keyCode) {
        for (Map.Entry<FieldDef, FieldEntry> e : entries.entrySet()) {
            FieldDef def = e.getKey();
            FieldEntry entry = e.getValue();
            if (entry == null || !def.isVisible()) continue;
            if (isInCollapsedSection(def)) continue;

            if (handleFieldKeyTyped(def, entry, c, keyCode)) return true;
        }
        return false;
    }

    protected boolean handleFieldKeyTyped(FieldDef def, FieldEntry entry, char c, int keyCode) {
        switch (def.getType()) {
            case ROW: {
                if (entry.leftChild != null && def.getLeftChild() != null && def.getLeftChild().isVisible()) {
                    if (handleFieldKeyTyped(def.getLeftChild(), entry.leftChild, c, keyCode)) return true;
                }
                if (entry.rightChild != null && def.getRightChild() != null && def.getRightChild().isVisible()) {
                    if (handleFieldKeyTyped(def.getRightChild(), entry.rightChild, c, keyCode)) return true;
                }
                return false;
            }
            case STRING: {
                if (entry.textField != null && entry.textField.keyTyped(c, keyCode)) {
                    def.setValue(entry.textField.getText());
                    notifyChanged();
                    return true;
                }
                return false;
            }
            case INT: {
                if (entry.textField != null && entry.textField.keyTyped(c, keyCode)) {
                    try {
                        def.setValue(Integer.parseInt(entry.textField.getText()));
                    } catch (NumberFormatException ignored) {}
                    notifyChanged();
                    return true;
                }
                return false;
            }
            case FLOAT: {
                if (entry.textField != null && entry.textField.keyTyped(c, keyCode)) {
                    try {
                        def.setValue(Float.parseFloat(entry.textField.getText()));
                    } catch (NumberFormatException ignored) {}
                    notifyChanged();
                    return true;
                }
                return false;
            }
            case TEXT_AREA: {
                if (entry.textArea != null && entry.textArea.keyTyped(c, keyCode)) {
                    def.setValue(entry.textArea.getText());
                    notifyChanged();
                    return true;
                }
                return false;
            }
            default:
                return handleCustomFieldKeyTyped(def, entry, c, keyCode);
        }
    }

    /**
     * Update cursor blink state for text fields.
     */
    public void updateScreen() {
        for (Map.Entry<FieldDef, FieldEntry> e : entries.entrySet()) {
            FieldEntry entry = e.getValue();
            if (entry == null) continue;
            updateEntryScreen(entry);
        }
    }

    protected void updateEntryScreen(FieldEntry entry) {
        if (entry.textField != null) entry.textField.updateCursorCounter();
        if (entry.textArea != null) entry.textArea.updateCursorCounter();
        if (entry.leftChild != null) updateEntryScreen(entry.leftChild);
        if (entry.rightChild != null) updateEntryScreen(entry.rightChild);
    }

    /**
     * Get all dropdowns for z-order rendering by parent.
     */
    public List<ModernDropdown> getDropdowns() {
        return allDropdowns;
    }

    /**
     * Get all text areas for mouseReleased/mouseDragged delegation.
     */
    public List<ModernTextArea> getTextAreas() {
        List<ModernTextArea> areas = new ArrayList<>();
        for (FieldEntry entry : entries.values()) {
            if (entry != null) collectTextAreas(entry, areas);
        }
        return areas;
    }

    protected void collectTextAreas(FieldEntry entry, List<ModernTextArea> areas) {
        if (entry.textArea != null) areas.add(entry.textArea);
        if (entry.leftChild != null) collectTextAreas(entry.leftChild, areas);
        if (entry.rightChild != null) collectTextAreas(entry.rightChild, areas);
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════════════
    // EXTENSIBILITY HOOKS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Override to draw a custom field type. Return total Y after drawing, or -1 if not handled.
     */
    protected int drawCustomField(FieldDef def, FieldEntry entry, int cx, int cw, int y, int mouseX, int mouseY, FontRenderer fr) {
        return -1;
    }

    /**
     * Override to handle clicks for a custom field type. Return true if handled.
     */
    protected boolean handleCustomFieldClick(FieldDef def, FieldEntry entry, int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Override to handle key events for a custom field type. Return true if handled.
     */
    protected boolean handleCustomFieldKeyTyped(FieldDef def, FieldEntry entry, char c, int keyCode) {
        return false;
    }

    /**
     * Handle clicks on expanded dropdowns in SCREEN space (no scroll offset).
     * Writes the selected value back to the FieldDef data model.
     */
    public boolean handleExpandedDropdownScreenClick(int mouseX, int mouseY, int button) {
        for (Map.Entry<FieldDef, FieldEntry> e : entries.entrySet()) {
            FieldDef def = e.getKey();
            FieldEntry entry = e.getValue();
            if (entry == null) continue;
            if (tryDropdownScreenClick(def, entry, mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected boolean tryDropdownScreenClick(FieldDef def, FieldEntry entry, int mouseX, int mouseY, int button) {
        // Check main dropdown
        if (entry.dropdown != null && entry.dropdown.isExpanded()) {
            int prev = entry.dropdown.getSelectedIndex();
            if (entry.dropdown.mouseClickedScreenSpace(mouseX, mouseY, button)) {
                if (entry.dropdown.getSelectedIndex() != prev) {
                    writeDropdownToData(def, entry.dropdown);
                    notifyChanged();
                }
                return true;
            }
        }
        // Check stance dropdown (faction rows)
        if (entry.stanceDropdown != null && entry.stanceDropdown.isExpanded()) {
            int prev = entry.stanceDropdown.getSelectedIndex();
            if (entry.stanceDropdown.mouseClickedScreenSpace(mouseX, mouseY, button)) {
                if (entry.stanceDropdown.getSelectedIndex() != prev) {
                    if (def.getStanceSetter() != null)
                        def.getStanceSetter().accept(entry.stanceDropdown.getSelectedIndex());
                    notifyChanged();
                }
                return true;
            }
        }
        // Check ROW children
        if (def.getType() == FieldType.ROW) {
            if (entry.leftChild != null && def.getLeftChild() != null)
                if (tryDropdownScreenClick(def.getLeftChild(), entry.leftChild, mouseX, mouseY, button)) return true;
            if (entry.rightChild != null && def.getRightChild() != null)
                if (tryDropdownScreenClick(def.getRightChild(), entry.rightChild, mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void writeDropdownToData(FieldDef def, ModernDropdown dd) {
        int idx = dd.getSelectedIndex();
        switch (def.getType()) {
            case STRING_ENUM: {
                String[] vals = def.getStringEnumValues();
                if (vals != null && idx >= 0 && idx < vals.length)
                    def.setStringEnumValue(vals[idx]);
                break;
            }
            case ENUM: {
                Class<? extends Enum<?>> ec = def.getEnumClass();
                if (ec != null) {
                    Enum<?>[] constants = ec.getEnumConstants();
                    if (idx >= 0 && idx < constants.length)
                        def.setValue(constants[idx]);
                }
                break;
            }
            case AVAILABILITY_ROW:
            case FACTION_ROW: {
                if (def.getConditionSetter() != null)
                    def.getConditionSetter().accept(idx);
                break;
            }
        }
    }

    protected boolean isInCollapsedSection(FieldDef def) {
        for (SectionInfo section : sections) {
            if (section.fields.contains(def) && !section.section.isExpanded()) {
                return true;
            }
        }
        return false;
    }

    protected void notifyChanged() {
        if (listener != null) listener.onFieldChanged();
    }

    /**
     * Reload component values from data. Call after external changes (e.g., selector callbacks).
     */
    public void refresh() {
        for (Map.Entry<FieldDef, FieldEntry> e : entries.entrySet()) {
            FieldDef def = e.getKey();
            FieldEntry entry = e.getValue();
            if (entry == null) continue;
            refreshEntry(def, entry);
        }
    }

    protected void refreshEntry(FieldDef def, FieldEntry entry) {
        switch (def.getType()) {
            case STRING:
                if (entry.textField != null) {
                    String val = def.getValue() != null ? def.getValue().toString() : "";
                    entry.textField.setText(val);
                }
                break;
            case INT:
                if (entry.textField != null) {
                    int ival = def.getValue() instanceof Number ? ((Number) def.getValue()).intValue() : 0;
                    entry.textField.setText(String.valueOf(ival));
                }
                break;
            case FLOAT:
                if (entry.textField != null) {
                    float fval = def.getValue() instanceof Number ? ((Number) def.getValue()).floatValue() : 0f;
                    entry.textField.setText(String.valueOf(fval));
                }
                break;
            case TEXT_AREA:
                if (entry.textArea != null) {
                    String tval = def.getValue() != null ? def.getValue().toString() : "";
                    entry.textArea.setText(tval);
                }
                break;
            case ROW:
                if (entry.leftChild != null && def.getLeftChild() != null) refreshEntry(def.getLeftChild(), entry.leftChild);
                if (entry.rightChild != null && def.getRightChild() != null) refreshEntry(def.getRightChild(), entry.rightChild);
                break;
            default:
                // BOOLEAN, ENUM, STRING_ENUM, COLOR, SELECT, AVAILABILITY_ROW, FACTION_ROW
                // are synced from data every draw() call
                break;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // INNER CLASSES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Holds the Modern component(s) created for a single FieldDef.
     */
    protected static class FieldEntry {
        final FieldDef def;

        // One of these is populated based on FieldType
        ModernTextField textField;
        ModernTextArea textArea;
        ModernCheckbox checkbox;
        ModernDropdown dropdown;
        ModernColorButton colorButton;
        ModernSelectButton selectButton;
        ModernButton clearButton;
        ModernButton actionButton;

        // Faction row extra
        ModernDropdown stanceDropdown;

        // Row children
        FieldEntry leftChild;
        FieldEntry rightChild;

        FieldEntry(FieldDef def) {
            this.def = def;
        }
    }

    /**
     * Groups a CollapsibleSection with its child FieldDefs.
     */
    public static class SectionInfo {
        public final FieldDef headerDef;
        public final CollapsibleSection section;
        public final List<FieldDef> fields = new ArrayList<>();

        SectionInfo(FieldDef headerDef, CollapsibleSection section) {
            this.headerDef = headerDef;
            this.section = section;
        }
    }
}
