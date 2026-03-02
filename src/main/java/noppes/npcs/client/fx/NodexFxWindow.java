package noppes.npcs.client.fx;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import noppes.npcs.LogWriter;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Nodex-style JavaFX IDE window for script file editing.
 * This window intentionally runs side-by-side with the legacy in-game GUI.
 */
public final class NodexFxWindow {

    private static final NodexFxWindow INSTANCE = new NodexFxWindow();
    private static final DataFormat TREE_DRAG_FORMAT = new DataFormat("customnpcs.nodex.tree.path");
    private static final double DEFAULT_DIVIDER = 0.24D;
    private static final String VIRTUAL_PREFIX = "__virtual__";

    private boolean isVirtualPath(Path path) {
        return path != null && path.toString().startsWith(VIRTUAL_PREFIX);
    }

    private Stage stage;
    private SplitPane contentSplit;
    private TreeView<Path> fileTree;
    private TabPane tabPane;

    private Label rootLabel;
    private Label cursorLabel;
    private Label encodingLabel;
    private Label statusLabel;

    private Scene scene;
    private Path rootPath;
    private double lastDividerPosition = DEFAULT_DIVIDER;
    private NodexTheme currentTheme = NodexTheme.DARK_BLUE;

    private final Map<String, EditorTab> openEditors = new HashMap<String, EditorTab>();
    private final Set<Path> watchedDirectories = new HashSet<Path>();
    private NodexScriptBinding activeBinding;
    private Path cssTempFile;
    private VBox scriptPanel;
    private javafx.scene.control.ListView<String> scriptListView;
    private HBox scriptButtonBar;
    private List<NodexScriptBinding.ScriptTabData> currentTabs = new ArrayList<NodexScriptBinding.ScriptTabData>();

    private WatchService watchService;
    private Thread watchThread;
    private final AtomicBoolean watcherRunning = new AtomicBoolean(false);
    private final AtomicBoolean pendingRefresh = new AtomicBoolean(false);

    private enum LanguageKind {
        JAVA,
        JS,
        JSON,
        PLAIN
    }

    enum NodexTheme {
        DARK_BLUE("Dark Blue"),
        DARK_GRAY("Dark Gray"),
        MONOKAI("Monokai"),
        LIGHT("Light");

        final String displayName;
        NodexTheme(String displayName) { this.displayName = displayName; }

        static NodexTheme fromName(String name) {
            for (NodexTheme t : values()) {
                if (t.displayName.equals(name)) return t;
            }
            return DARK_BLUE;
        }
    }

    private static final class EditorTab {
        Path path;
        final Tab tab;
        final CodeArea area;
        final PauseTransition highlightDebounce;
        final ChangeListener<Number> caretListener;
        boolean dirty;
        long lastModifiedMillis;

        // Virtual tab fields
        String virtualId;
        String displayName;
        LanguageKind languageOverride;
        int virtualTabIndex = -1;

        EditorTab(Path path, Tab tab, CodeArea area, PauseTransition debounce, ChangeListener<Number> caretListener) {
            this.path = path;
            this.tab = tab;
            this.area = area;
            this.highlightDebounce = debounce;
            this.caretListener = caretListener;
            this.dirty = false;
            this.lastModifiedMillis = 0L;
        }

        boolean isVirtual() {
            return path == null && virtualId != null;
        }

        String editorKey() {
            if (virtualId != null) return virtualId;
            return path != null ? path.toAbsolutePath().normalize().toString() : "";
        }
    }

    private enum EditorAction {
        UNDO,
        REDO,
        CUT,
        COPY,
        PASTE,
        SELECT_ALL
    }

    private static final String[] JAVA_KEYWORDS = new String[]{
        "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "new", "package",
        "return", "short", "strictfp", "super", "switch", "this",
        "throw", "throws", "try", "void", "while", "true", "false", "null"
    };

    private static final String[] JAVA_MODIFIERS = new String[]{
        "abstract", "final", "native", "private", "protected", "public", "static",
        "synchronized", "transient", "volatile"
    };

    private static final String[] JS_KEYWORDS = new String[]{
        "await", "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete", "do", "else",
        "extends", "finally", "for", "function", "if", "import", "in", "instanceof", "let", "new",
        "return", "super", "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield",
        "true", "false", "null", "undefined"
    };

    private static final String[] JS_MODIFIERS = new String[]{
        "export", "static", "async"
    };

    private static final String JAVA_KEYWORD_PATTERN = "\\b(" + String.join("|", JAVA_KEYWORDS) + ")\\b";
    private static final String JAVA_MODIFIER_PATTERN = "\\b(" + String.join("|", JAVA_MODIFIERS) + ")\\b";
    private static final String JS_KEYWORD_PATTERN = "\\b(" + String.join("|", JS_KEYWORDS) + ")\\b";
    private static final String JS_MODIFIER_PATTERN = "\\b(" + String.join("|", JS_MODIFIERS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String NUMBER_PATTERN = "\\b\\d+(?:\\.\\d+)?\\b";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
    private static final String COMMENT_PATTERN = "//[^\\n]*|/\\*(.|\\R)*?\\*/";
    private static final String ANNOTATION_PATTERN = "@[A-Za-z_][a-zA-Z0-9_.]*";
    private static final String TYPE_REF_PATTERN = "\\b[A-Z][a-zA-Z0-9_]*\\b";
    private static final String METHOD_CALL_PATTERN = "[a-z_][a-zA-Z0-9_]*(?=\\s*\\()";

    private static final Pattern JAVA_PATTERN = Pattern.compile(
        "(?<COMMENT>" + COMMENT_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
            + "|(?<MODIFIER>" + JAVA_MODIFIER_PATTERN + ")"
            + "|(?<KEYWORD>" + JAVA_KEYWORD_PATTERN + ")"
            + "|(?<TYPEREF>" + TYPE_REF_PATTERN + ")"
            + "|(?<METHODCALL>" + METHOD_CALL_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
    );

    private static final Pattern JS_PATTERN = Pattern.compile(
        "(?<COMMENT>" + COMMENT_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
            + "|(?<MODIFIER>" + JS_MODIFIER_PATTERN + ")"
            + "|(?<KEYWORD>" + JS_KEYWORD_PATTERN + ")"
            + "|(?<TYPEREF>" + TYPE_REF_PATTERN + ")"
            + "|(?<METHODCALL>" + METHOD_CALL_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
    );

    private static final Pattern JSON_PATTERN = Pattern.compile(
        "(?<PROPERTY>\"([^\"\\\\]|\\\\.)*\"(?=\\s*:))"
            + "|(?<STRING>\"([^\"\\\\]|\\\\.)*\")"
            + "|(?<NUMBER>-?\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b)"
            + "|(?<BOOLEAN>\\b(true|false)\\b)"
            + "|(?<NULL>\\bnull\\b)"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
    );

    private NodexFxWindow() {
    }

    public static NodexFxWindow get() {
        return INSTANCE;
    }

    public void open(Path root) {
        open(root, null);
    }

    public void open(Path root, NodexScriptBinding binding) {
        if (root == null) {
            return;
        }

        Path normalizedRoot = root.toAbsolutePath().normalize();
        ensureRootExists(normalizedRoot);

        if (stage == null) {
            buildStage();
        }

        boolean rootChanged = rootPath == null || !rootPath.equals(normalizedRoot);
        rootPath = normalizedRoot;
        rootLabel.setText("Root: " + rootPath.toString());

        // Close old virtual tabs when a new binding replaces the old one
        if (binding != null && activeBinding != null) {
            closeVirtualTabs();
        }
        activeBinding = binding;

        // Update title based on binding context
        if (binding != null) {
            stage.setTitle("Nodex IDE - " + binding.label);
        } else {
            stage.setTitle("Nodex IDE - " + getPathName(rootPath));
        }

        // Populate script panel from binding
        currentTabs.clear();
        if (binding != null && binding.tabs != null) {
            currentTabs.addAll(binding.tabs);
        }
        refreshScriptPanel();

        if (rootChanged) {
            reopenExistingTabsWithinRoot();
            refreshTree(null);
            startWatcher();
        } else if (!watcherRunning.get()) {
            startWatcher();
        } else {
            refreshTree(null);
        }

        // Auto-open virtual script tabs from the binding
        if (binding != null && binding.tabs != null) {
            for (NodexScriptBinding.ScriptTabData tabData : binding.tabs) {
                openVirtualTab(tabData, binding.saveCallback);
            }
            // Select the first tab
            if (!binding.tabs.isEmpty()) {
                String firstVirtualId = "virtual#" + binding.contextId + "#" + binding.tabs.get(0).index;
                EditorTab firstEditor = openEditors.get(firstVirtualId);
                if (firstEditor != null) {
                    tabPane.getSelectionModel().select(firstEditor.tab);
                    firstEditor.area.requestFocus();
                }
            }
        }

        stage.show();
        stage.toFront();
        stage.requestFocus();

        EditorTab active = getActiveEditor();
        if (active != null) {
            active.area.requestFocus();
        }
    }

    private void closeVirtualTabs() {
        List<EditorTab> toClose = new ArrayList<EditorTab>();
        for (EditorTab editorTab : openEditors.values()) {
            if (editorTab.isVirtual()) {
                toClose.add(editorTab);
            }
        }
        for (EditorTab editorTab : toClose) {
            tabPane.getTabs().remove(editorTab.tab);
            removeEditor(editorTab);
        }
    }

    private void openVirtualTab(NodexScriptBinding.ScriptTabData tabData, NodexScriptBinding.SaveCallback saveCallback) {
        String virtualId = "virtual#" + (activeBinding != null ? activeBinding.contextId : "UNKNOWN") + "#" + tabData.index;

        EditorTab existing = openEditors.get(virtualId);
        if (existing != null) {
            tabPane.getSelectionModel().select(existing.tab);
            existing.area.requestFocus();
            return;
        }

        CodeArea area = new CodeArea();
        area.replaceText(tabData.content);
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.getStyleClass().add("code-area");

        String tabDisplayName = tabData.name;
        PauseTransition debounce = new PauseTransition(Duration.millis(120));
        Tab tab = new Tab(tabDisplayName);
        tab.setGraphic(createIconLabel("S", "#55ff55"));
        ChangeListener<Number> caretListener = (obs, oldVal, newVal) -> updateCursorStatus();
        EditorTab editorTab = new EditorTab(null, tab, area, debounce, caretListener);
        editorTab.virtualId = virtualId;
        editorTab.displayName = tabDisplayName;
        editorTab.virtualTabIndex = tabData.index;

        // Determine language kind from the binding's language field
        if ("Java".equalsIgnoreCase(tabData.language) || "Janino".equalsIgnoreCase(tabData.language)) {
            editorTab.languageOverride = LanguageKind.JAVA;
        } else {
            editorTab.languageOverride = LanguageKind.JS;
        }

        area.textProperty().addListener((obs, oldText, newText) -> {
            if (!editorTab.dirty) {
                editorTab.dirty = true;
                refreshTabTitle(editorTab);
            }
            debounce.playFromStart();
        });
        area.caretPositionProperty().addListener(caretListener);
        debounce.setOnFinished(evt -> applyHighlighting(editorTab));

        tab.setOnCloseRequest(evt -> {
            if (!confirmCloseEditor(editorTab)) {
                evt.consume();
            }
        });
        tab.setOnClosed(evt -> removeEditor(editorTab));
        tab.setContent(new VirtualizedScrollPane<CodeArea>(area));

        tabPane.getTabs().add(tab);
        openEditors.put(virtualId, editorTab);
        applyHighlighting(editorTab);
        updateCursorStatus();
        updateBottomStatus();
    }

    private void buildStage() {
        stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setMinWidth(960);
        stage.setMinHeight(620);
        stage.setOnCloseRequest(evt -> {
            evt.consume();
            if (confirmCloseAllDirtyEditors()) {
                // Fire close callback to sync all tab contents back to the handler
                fireCloseCallback();
                stage.hide();
            }
        });
        stage.setOnHidden(evt -> stopWatcher());

        BorderPane root = new BorderPane();
        root.getStyleClass().add("window");

        MenuBar menuBar = buildMenuBar();
        ToolBar topBar = buildTopBar();
        VBox topContainer = new VBox(menuBar, topBar);
        root.setTop(topContainer);

        scriptPanel = buildScriptPanel();
        fileTree = buildFileTree();

        tabPane = new TabPane();
        tabPane.getStyleClass().add("text-editor-container");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateCursorStatus();
            updateBottomStatus();
        });

        StackPane editorContainer = new StackPane(tabPane);
        HBox.setHgrow(editorContainer, Priority.ALWAYS);

        contentSplit = new SplitPane();
        contentSplit.setOrientation(Orientation.HORIZONTAL);
        // Default: show script panel on left, editor on right
        contentSplit.getItems().addAll(scriptPanel, editorContainer);
        contentSplit.setDividerPositions(DEFAULT_DIVIDER);
        root.setCenter(contentSplit);

        ToolBar bottomBar = buildBottomBar();
        root.setBottom(bottomBar);

        currentTheme = loadThemePreference();
        scene = new Scene(root, 1240, 820);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcut);
        String cssUri = buildThemeCssUri(currentTheme);
        if (!cssUri.isEmpty()) {
            scene.getStylesheets().add(cssUri);
        }
        stage.setScene(scene);
    }

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
            createMenuItem("New File", evt -> createFileInSelectedDirectory()),
            createMenuItem("New Folder", evt -> createDirectoryInSelectedDirectory()),
            new SeparatorMenuItem(),
            createMenuItem("Save", evt -> saveActive()),
            createMenuItem("Save All", evt -> saveAll()),
            new SeparatorMenuItem(),
            createMenuItem("Rename", evt -> renameSelected()),
            createMenuItem("Delete", evt -> deleteSelected()),
            new SeparatorMenuItem(),
            createMenuItem("Refresh Tree", evt -> refreshTree(null))
        );

        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
            createMenuItem("Undo", evt -> runEditorAction(EditorAction.UNDO)),
            createMenuItem("Redo", evt -> runEditorAction(EditorAction.REDO)),
            new SeparatorMenuItem(),
            createMenuItem("Cut", evt -> runEditorAction(EditorAction.CUT)),
            createMenuItem("Copy", evt -> runEditorAction(EditorAction.COPY)),
            createMenuItem("Paste", evt -> runEditorAction(EditorAction.PASTE)),
            createMenuItem("Select All", evt -> runEditorAction(EditorAction.SELECT_ALL))
        );

        Menu viewMenu = new Menu("View");
        Menu themeMenu = new Menu("Theme");
        for (NodexTheme theme : NodexTheme.values()) {
            MenuItem themeItem = new MenuItem(theme.displayName);
            themeItem.setOnAction(evt -> applyTheme(theme));
            themeMenu.getItems().add(themeItem);
        }
        viewMenu.getItems().addAll(
            createMenuItem("Toggle Script Panel", evt -> toggleScriptPanel()),
            createMenuItem("Toggle File Tree", evt -> toggleFileTree()),
            createMenuItem("Focus Editor", evt -> {
                EditorTab active = getActiveEditor();
                if (active != null) {
                    active.area.requestFocus();
                }
            }),
            new SeparatorMenuItem(),
            themeMenu
        );

        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().add(createMenuItem("About Nodex Port", evt -> showInfo(
            "Nodex Popup IDE",
            "Ported Nodex-style popup editor for CustomNPC+.",
            "JavaFX + RichTextFX bridge for script and source editing."
        )));

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);
        return menuBar;
    }

    private ToolBar buildTopBar() {
        ToolBar bar = new ToolBar();
        bar.getStyleClass().add("top-bar-view");
        bar.setPadding(new Insets(6, 8, 6, 8));

        Button saveButton = createFlatButton("Save", evt -> saveActive());
        Button saveAllButton = createFlatButton("Save All", evt -> saveAll());
        Button toggleScriptsButton = createFlatButton("Scripts", evt -> toggleScriptPanel());
        Button toggleFilesButton = createFlatButton("Files", evt -> toggleFileTree());

        rootLabel = new Label("Root:");
        rootLabel.getStyleClass().add("meta-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getItems().addAll(
            saveButton,
            saveAllButton,
            new Separator(),
            toggleScriptsButton,
            toggleFilesButton,
            new Separator(),
            rootLabel,
            spacer
        );
        return bar;
    }

    private VBox buildScriptPanel() {
        VBox panel = new VBox(4);
        panel.getStyleClass().add("script-panel");
        panel.setPadding(new Insets(8));
        panel.setMinWidth(160);
        panel.setPrefWidth(220);

        Label header = new Label("Scripts");
        header.getStyleClass().add("script-panel-header");

        scriptListView = new javafx.scene.control.ListView<String>();
        scriptListView.getStyleClass().add("script-list");
        VBox.setVgrow(scriptListView, Priority.ALWAYS);

        // Single click opens the tab in the editor
        scriptListView.setOnMouseClicked(evt -> {
            int idx = scriptListView.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < currentTabs.size() && activeBinding != null) {
                openVirtualTab(currentTabs.get(idx), activeBinding.saveCallback);
            }
        });

        Button addButton = new Button("+");
        addButton.getStyleClass().add("script-btn");
        addButton.setPrefWidth(36);
        addButton.setOnAction(evt -> addScriptTab());

        Button deleteButton = new Button("-");
        deleteButton.getStyleClass().add("script-btn");
        deleteButton.setPrefWidth(36);
        deleteButton.setOnAction(evt -> deleteScriptTab());

        scriptButtonBar = new HBox(4, addButton, deleteButton);
        scriptButtonBar.setAlignment(Pos.CENTER_LEFT);
        scriptButtonBar.setPadding(new Insets(4, 0, 0, 0));

        panel.getChildren().addAll(header, scriptListView, scriptButtonBar);
        return panel;
    }

    private void refreshScriptPanel() {
        if (scriptListView == null) return;

        ObservableList<String> items = FXCollections.observableArrayList();
        for (NodexScriptBinding.ScriptTabData tab : currentTabs) {
            items.add(tab.name);
        }
        scriptListView.setItems(items);

        // Show/hide add/delete buttons
        if (scriptButtonBar != null) {
            scriptButtonBar.setVisible(activeBinding != null && activeBinding.canModifyTabs);
            scriptButtonBar.setManaged(activeBinding != null && activeBinding.canModifyTabs);
        }

        // Update the panel header
        if (scriptPanel != null && !scriptPanel.getChildren().isEmpty()) {
            javafx.scene.Node firstChild = scriptPanel.getChildren().get(0);
            if (firstChild instanceof Label) {
                String headerText = activeBinding != null ? activeBinding.label : "Scripts";
                ((Label) firstChild).setText(headerText);
            }
        }
    }

    private ToolBar buildBottomBar() {
        ToolBar bar = new ToolBar();
        bar.getStyleClass().add("status-bar");
        bar.setPadding(new Insets(4, 8, 4, 8));

        cursorLabel = new Label("Ln 1, Col 1");
        cursorLabel.getStyleClass().add("status-label");

        encodingLabel = new Label("UTF-8");
        encodingLabel.getStyleClass().add("status-label");

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getItems().addAll(cursorLabel, new Separator(), encodingLabel, new Separator(), spacer, statusLabel);
        return bar;
    }

    private TreeView<Path> buildFileTree() {
        TreeView<Path> tree = new TreeView<Path>();
        tree.getStyleClass().add("file-tree-container");
        tree.setShowRoot(true);

        tree.setCellFactory(v -> new TreeCell<Path>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                    return;
                }

                String display = getPathName(item);
                if (display.length() == 0) {
                    display = item.toString();
                }
                setText(display);
                if (Files.isDirectory(item)) {
                    TreeItem<Path> treeItem = getTreeItem();
                    boolean expanded = treeItem != null && treeItem.isExpanded();
                    setGraphic(createIconLabel(expanded ? "V" : ">", "#E8A838"));
                } else {
                    setGraphic(createFileIconLabel(display));
                }
                setContextMenu(buildTreeContextMenu(item));
            }
        });

        tree.setOnMouseClicked(evt -> {
            if (evt.getButton() != MouseButton.PRIMARY || evt.getClickCount() != 2) {
                return;
            }
            TreeItem<Path> selected = tree.getSelectionModel().getSelectedItem();
            if (selected == null || selected.getValue() == null) {
                return;
            }
            Path selectedPath = selected.getValue();

            if (Files.isRegularFile(selectedPath)) {
                openFile(selectedPath);
            }
        });

        tree.setOnDragOver(this::handleTreeDragOver);
        tree.setOnDragDropped(this::handleTreeDragDropped);
        tree.setOnDragDetected(evt -> {
            TreeItem<Path> selected = tree.getSelectionModel().getSelectedItem();
            if (selected == null || selected.getValue() == null || rootPath == null) {
                return;
            }
            Path path = selected.getValue();
            if (path.equals(rootPath)) {
                return;
            }
            ClipboardContent content = new ClipboardContent();
            content.put(TREE_DRAG_FORMAT, path.toString());
            tree.startDragAndDrop(TransferMode.MOVE).setContent(content);
            evt.consume();
        });

        return tree;
    }

    private ContextMenu buildTreeContextMenu(Path item) {
        ContextMenu menu = new ContextMenu();

        MenuItem open = new MenuItem("Open");
        open.setDisable(Files.isDirectory(item));
        open.setOnAction(evt -> openFile(item));

        MenuItem newFile = new MenuItem("New File");
        newFile.setOnAction(evt -> createFileInDirectory(Files.isDirectory(item) ? item : item.getParent()));

        MenuItem newFolder = new MenuItem("New Folder");
        newFolder.setOnAction(evt -> createDirectoryInDirectory(Files.isDirectory(item) ? item : item.getParent()));

        MenuItem rename = new MenuItem("Rename");
        rename.setDisable(rootPath != null && rootPath.equals(item));
        rename.setOnAction(evt -> renamePath(item));

        MenuItem delete = new MenuItem("Delete");
        delete.setDisable(rootPath != null && rootPath.equals(item));
        delete.setOnAction(evt -> deletePath(item));

        MenuItem refresh = new MenuItem("Refresh");
        refresh.setOnAction(evt -> refreshTree(item));

        menu.getItems().addAll(open, new SeparatorMenuItem(), newFile, newFolder, new SeparatorMenuItem(), rename, delete, new SeparatorMenuItem(), refresh);
        return menu;
    }

    private static Label createIconLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold; "
            + "-fx-min-width: 20px; -fx-max-width: 20px; -fx-alignment: center;");
        return label;
    }

    private static Label createFileIconLabel(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".js"))     return createIconLabel("JS", "#F0D73E");
        if (lower.endsWith(".java"))   return createIconLabel("J", "#5382A1");
        if (lower.endsWith(".json") || lower.endsWith(".mcmeta")) return createIconLabel("{}", "#8BC34A");
        if (lower.endsWith(".groovy")) return createIconLabel("G", "#619CBB");
        if (lower.endsWith(".lua"))    return createIconLabel("L", "#000080");
        if (lower.endsWith(".txt"))    return createIconLabel("T", "#8ea0b5");
        if (lower.endsWith(".cfg") || lower.endsWith(".properties")) return createIconLabel("C", "#9E9E9E");
        return createIconLabel("f", "#8ea0b5");
    }

    private Button createFlatButton(String text, javafx.event.EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setOnAction(action);
        return button;
    }

    private MenuItem createMenuItem(String text, javafx.event.EventHandler<ActionEvent> action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(action);
        return item;
    }

    private void handleShortcut(KeyEvent event) {
        if (!event.isControlDown()) {
            return;
        }

        KeyCode key = event.getCode();
        if (key == KeyCode.S) {
            if (event.isShiftDown()) {
                saveAll();
            } else {
                saveActive();
            }
            event.consume();
            return;
        }

        if (key == KeyCode.R) {
            refreshTree(null);
            event.consume();
            return;
        }

        if (key == KeyCode.N) {
            createFileInSelectedDirectory();
            event.consume();
            return;
        }

        if (key == KeyCode.W) {
            closeActiveTabWithPrompt();
            event.consume();
            return;
        }

        if (key == KeyCode.E) {
            toggleFileTree();
            event.consume();
        }
    }

    private void runEditorAction(EditorAction action) {
        EditorTab active = getActiveEditor();
        if (active == null) {
            return;
        }

        if (action == EditorAction.UNDO) {
            active.area.undo();
        } else if (action == EditorAction.REDO) {
            active.area.redo();
        } else if (action == EditorAction.CUT) {
            active.area.cut();
        } else if (action == EditorAction.COPY) {
            active.area.copy();
        } else if (action == EditorAction.PASTE) {
            active.area.paste();
        } else if (action == EditorAction.SELECT_ALL) {
            active.area.selectAll();
        }

        active.area.requestFocus();
    }

    private void applyTheme(NodexTheme theme) {
        currentTheme = theme;
        if (scene != null) {
            scene.getStylesheets().clear();
            String uri = buildThemeCssUri(theme);
            if (!uri.isEmpty()) {
                scene.getStylesheets().add(uri);
            }
            // Force re-highlight all open editors for new theme colors
            for (EditorTab editorTab : openEditors.values()) {
                applyHighlighting(editorTab);
            }
        }
        saveThemePreference(theme);
        setStatus("Theme: " + theme.displayName);
    }

    private String buildThemeCssUri(NodexTheme theme) {
        String css = buildThemeCss(theme);
        try {
            if (cssTempFile != null) {
                Files.deleteIfExists(cssTempFile);
            }
            cssTempFile = Files.createTempFile("nodex-theme-", ".css");
            cssTempFile.toFile().deleteOnExit();
            Files.write(cssTempFile, css.getBytes(StandardCharsets.UTF_8));
            return cssTempFile.toUri().toString();
        } catch (Exception e) {
            LogWriter.error("Failed to write CSS temp file for Nodex IDE", asException(e));
            return "";
        }
    }

    private String buildThemeCss(NodexTheme theme) {
        switch (theme) {
            case DARK_GRAY: return buildDarkGrayCss();
            case MONOKAI:   return buildMonokaiCss();
            case LIGHT:     return buildLightCss();
            case DARK_BLUE:
            default:        return buildDarkBlueCss();
        }
    }

    private static String buildChromeCss(String windowBg, String menuBg, String topBarBg, String leftBarBg,
                                         String statusBg, String statusText, String treeBg, String treeText,
                                         String treeSelectedBg, String treeSelectedText,
                                         String tabBg, String tabHeaderBg, String tabActiveBg, String tabText,
                                         String editorBg, String gutterBg, String gutterText, String caretColor, String defaultText) {
        return ".window { -fx-background-color: " + windowBg + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 14, 0.35, 0, 4); }"
            + ".menu-bar { -fx-background-color: " + menuBg + "; }"
            + ".top-bar-view { -fx-background-color: " + topBarBg + "; }"
            + ".left-bar { -fx-background-color: " + leftBarBg + "; }"
            + ".status-bar { -fx-background-color: " + statusBg + "; }"
            + ".status-label { -fx-text-fill: " + statusText + "; }"
            + ".meta-label { -fx-text-fill: " + statusText + "; -fx-padding: 0 0 0 8px; }"
            + ".file-tree-container { -fx-background-color: " + treeBg + "; -fx-control-inner-background: " + treeBg + "; }"
            + ".file-tree-container .tree-cell { -fx-cell-size: 24px; -fx-text-fill: " + treeText + "; }"
            + ".file-tree-container .tree-cell:selected { -fx-background-color: " + treeSelectedBg + "; -fx-text-fill: " + treeSelectedText + "; }"
            + ".tab-pane { -fx-background-color: " + tabBg + "; }"
            + ".tab-pane .tab-header-background { -fx-background-color: " + tabHeaderBg + "; }"
            + ".tab-pane .tab { -fx-background-color: " + tabBg + "; }"
            + ".tab-pane .tab:selected { -fx-background-color: " + tabActiveBg + "; }"
            + ".tab-pane .tab-label { -fx-text-fill: " + tabText + "; }"
            + ".code-area { -fx-background-color: " + editorBg + "; -fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-text-fill: " + defaultText + "; }"
            + ".code-area .lineno { -fx-background-color: " + gutterBg + "; -fx-text-fill: " + gutterText + "; }"
            + ".code-area .caret { -fx-stroke: " + caretColor + "; }"
            + ".script-panel { -fx-background-color: " + treeBg + "; }"
            + ".script-panel-header { -fx-text-fill: " + treeText + "; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 4 0; }"
            + ".script-list { -fx-background-color: " + treeBg + "; -fx-control-inner-background: " + treeBg + "; }"
            + ".script-list .list-cell { -fx-text-fill: " + treeText + "; -fx-cell-size: 28px; -fx-background-color: transparent; }"
            + ".script-list .list-cell:selected { -fx-background-color: " + treeSelectedBg + "; -fx-text-fill: " + treeSelectedText + "; }"
            + ".script-btn { -fx-background-color: " + tabBg + "; -fx-text-fill: " + tabText + "; -fx-padding: 4 12; -fx-cursor: hand; }"
            + ".script-btn:hover { -fx-background-color: " + treeSelectedBg + "; }";
    }

    private static String buildSyntaxCss(String keyword, String modifier, String annotation, String typeRef,
                                         String methodCall, String paren, String brace, String bracket,
                                         String semicolon, String string, String comment, String number,
                                         String jsonProp, String jsonString, String jsonNumber,
                                         String jsonBool, String jsonNull) {
        return ".code-area .keyword { -fx-fill: " + keyword + "; }"
            + ".code-area .modifier { -fx-fill: " + modifier + "; }"
            + ".code-area .annotation { -fx-fill: " + annotation + "; }"
            + ".code-area .type-ref { -fx-fill: " + typeRef + "; }"
            + ".code-area .method-call { -fx-fill: " + methodCall + "; }"
            + ".code-area .paren { -fx-fill: " + paren + "; }"
            + ".code-area .brace { -fx-fill: " + brace + "; }"
            + ".code-area .bracket { -fx-fill: " + bracket + "; }"
            + ".code-area .semicolon { -fx-fill: " + semicolon + "; }"
            + ".code-area .string { -fx-fill: " + string + "; }"
            + ".code-area .comment { -fx-fill: " + comment + "; }"
            + ".code-area .number { -fx-fill: " + number + "; }"
            + ".code-area .json-property { -fx-fill: " + jsonProp + "; }"
            + ".code-area .json-string { -fx-fill: " + jsonString + "; }"
            + ".code-area .json-number { -fx-fill: " + jsonNumber + "; }"
            + ".code-area .json-boolean { -fx-fill: " + jsonBool + "; }"
            + ".code-area .json-null { -fx-fill: " + jsonNull + "; }";
    }

    private static String buildDarkBlueCss() {
        return buildChromeCss(
            "#121821", "#151d29", "#1a2433", "#101722", "#151d29", "#8ea0b5",
            "#0f1621", "#a8b7c8", "#263348", "#f0f4fa",
            "#182234", "#101a28", "#253349", "#d9e4f0",
            "#0f1720", "#0b1218", "#5f7387", "#dbe5f0", "#dbe5f0"
        ) + buildSyntaxCss(
            "#70c4ff", "#ffaa00", "#cc9933", "#00aaaa", "#55ff55",
            "#dbe5f0", "#8ed0ff", "#8ed0ff", "#dbe5f0",
            "#8fd66a", "#6a7a88", "#f6b17a",
            "#f0d58a", "#8fd66a", "#f6b17a", "#70c4ff", "#d88ec7"
        );
    }

    private static String buildDarkGrayCss() {
        return buildChromeCss(
            "#1e1e1e", "#252526", "#333333", "#252526", "#007acc", "#cccccc",
            "#252526", "#cccccc", "#094771", "#ffffff",
            "#2d2d2d", "#252526", "#1e1e1e", "#cccccc",
            "#1e1e1e", "#1e1e1e", "#858585", "#aeafad", "#d4d4d4"
        ) + buildSyntaxCss(
            "#569cd6", "#569cd6", "#dcdcaa", "#4ec9b0", "#dcdcaa",
            "#d4d4d4", "#d4d4d4", "#d4d4d4", "#d4d4d4",
            "#ce9178", "#6a9955", "#b5cea8",
            "#9cdcfe", "#ce9178", "#b5cea8", "#569cd6", "#569cd6"
        );
    }

    private static String buildMonokaiCss() {
        return buildChromeCss(
            "#272822", "#1e1f1c", "#3e3d32", "#1e1f1c", "#414339", "#75715e",
            "#2f3129", "#f8f8f2", "#49483e", "#f8f8f0",
            "#3e3d32", "#1e1f1c", "#272822", "#f8f8f2",
            "#272822", "#2f3129", "#75715e", "#f8f8f0", "#f8f8f2"
        ) + buildSyntaxCss(
            "#f92672", "#f92672", "#e6db74", "#66d9ef", "#a6e22e",
            "#f8f8f2", "#f8f8f2", "#f8f8f2", "#f8f8f2",
            "#e6db74", "#75715e", "#ae81ff",
            "#f92672", "#e6db74", "#ae81ff", "#66d9ef", "#ae81ff"
        );
    }

    private static String buildLightCss() {
        return buildChromeCss(
            "#f5f5f5", "#e8e8e8", "#e0e0e0", "#e8e8e8", "#f5f5f5", "#616161",
            "#f0f0f0", "#333333", "#c8ddf0", "#000000",
            "#ececec", "#e0e0e0", "#ffffff", "#333333",
            "#ffffff", "#f0f0f0", "#999999", "#000000", "#333333"
        ) + buildSyntaxCss(
            "#0000ff", "#7f0055", "#808000", "#267f99", "#795e26",
            "#333333", "#333333", "#333333", "#333333",
            "#008000", "#808080", "#098658",
            "#a31515", "#008000", "#098658", "#0000ff", "#0000ff"
        );
    }

    private NodexTheme loadThemePreference() {
        try {
            Path configDir = getConfigDir();
            if (configDir == null) return NodexTheme.DARK_BLUE;
            Path configFile = configDir.resolve("config.properties");
            if (!Files.exists(configFile)) return NodexTheme.DARK_BLUE;
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream in = Files.newInputStream(configFile)) {
                props.load(in);
            }
            String themeName = props.getProperty("theme", "Dark Blue");
            return NodexTheme.fromName(themeName);
        } catch (Exception ignored) {
            return NodexTheme.DARK_BLUE;
        }
    }

    private void saveThemePreference(NodexTheme theme) {
        try {
            Path configDir = getConfigDir();
            if (configDir == null) return;
            if (!Files.exists(configDir)) Files.createDirectories(configDir);
            Path configFile = configDir.resolve("config.properties");
            java.util.Properties props = new java.util.Properties();
            if (Files.exists(configFile)) {
                try (java.io.InputStream in = Files.newInputStream(configFile)) {
                    props.load(in);
                }
            }
            props.setProperty("theme", theme.displayName);
            try (java.io.OutputStream out = Files.newOutputStream(configFile)) {
                props.store(out, "Nodex IDE Configuration");
            }
        } catch (Exception ignored) {
        }
    }

    private Path getConfigDir() {
        if (rootPath == null) return null;
        return rootPath.resolve(".nodex");
    }

    private void refreshTree(Path selectedPath) {
        if (fileTree == null || rootPath == null) {
            return;
        }

        Set<Path> expanded = collectExpandedPaths(fileTree.getRoot(), new LinkedHashSet<Path>());

        Path fallbackSelect = selectedPath;
        if (fallbackSelect == null) {
            TreeItem<Path> currentSelection = fileTree.getSelectionModel().getSelectedItem();
            fallbackSelect = currentSelection != null ? currentSelection.getValue() : rootPath;
        }
        if (fallbackSelect == null) {
            fallbackSelect = rootPath;
        }

        TreeItem<Path> rootItem = createNode(rootPath);
        rootItem.setExpanded(true);

        fileTree.setRoot(rootItem);
        restoreExpandedPaths(rootItem, expanded);

        if (fallbackSelect != null) {
            revealPath(fallbackSelect);
        }
    }

    private TreeItem<Path> createNode(Path path) {
        TreeItem<Path> node = new TreeItem<Path>(path);

        if (Files.isDirectory(path)) {
            node.getChildren().add(new TreeItem<Path>());
            node.expandedProperty().addListener((obs, oldValue, expanded) -> {
                if (!expanded) {
                    return;
                }
                if (node.getChildren().size() == 1 && node.getChildren().get(0).getValue() == null) {
                    node.getChildren().setAll(loadChildren(path));
                }
            });
        }
        return node;
    }

    private ObservableList<TreeItem<Path>> loadChildren(Path directory) {
        List<Path> children = new ArrayList<Path>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path child : stream) {
                children.add(child);
            }
        } catch (Exception e) {
            LogWriter.error("Failed loading directory in Nodex IDE: " + directory, asException(e));
        }

        Collections.sort(children, PATH_COMPARATOR);
        ObservableList<TreeItem<Path>> nodes = FXCollections.observableArrayList();
        for (Path child : children) {
            nodes.add(createNode(child));
        }
        return nodes;
    }

    private void revealPath(Path target) {
        if (target == null || fileTree.getRoot() == null) {
            return;
        }
        TreeItem<Path> item = ensurePathVisible(fileTree.getRoot(), target.toAbsolutePath().normalize());
        if (item != null) {
            fileTree.getSelectionModel().select(item);
            fileTree.scrollTo(fileTree.getRow(item));
        }
    }

    private TreeItem<Path> ensurePathVisible(TreeItem<Path> current, Path target) {
        if (current == null || current.getValue() == null) {
            return null;
        }

        Path currentPath = current.getValue().toAbsolutePath().normalize();
        if (currentPath.equals(target)) {
            return current;
        }

        if (!target.startsWith(currentPath)) {
            return null;
        }

        if (Files.isDirectory(currentPath)) {
            current.setExpanded(true);
            if (current.getChildren().size() == 1 && current.getChildren().get(0).getValue() == null) {
                current.getChildren().setAll(loadChildren(currentPath));
            }
        }

        for (TreeItem<Path> child : current.getChildren()) {
            TreeItem<Path> found = ensurePathVisible(child, target);
            if (found != null) {
                return found;
            }
        }
        return currentPath.equals(target) ? current : null;
    }

    private Set<Path> collectExpandedPaths(TreeItem<Path> node, Set<Path> collector) {
        if (node == null || node.getValue() == null) {
            return collector;
        }
        if (node.isExpanded()) {
            collector.add(node.getValue().toAbsolutePath().normalize());
        }
        for (TreeItem<Path> child : node.getChildren()) {
            collectExpandedPaths(child, collector);
        }
        return collector;
    }

    private void restoreExpandedPaths(TreeItem<Path> node, Set<Path> expanded) {
        if (node == null || node.getValue() == null || expanded == null || expanded.isEmpty()) {
            return;
        }

        Path nodePath = node.getValue().toAbsolutePath().normalize();
        if (expanded.contains(nodePath) && Files.isDirectory(nodePath)) {
            node.setExpanded(true);
            if (node.getChildren().size() == 1 && node.getChildren().get(0).getValue() == null) {
                node.getChildren().setAll(loadChildren(nodePath));
            }
        }
        for (TreeItem<Path> child : node.getChildren()) {
            restoreExpandedPaths(child, expanded);
        }
    }

    private void openFile(Path filePath) {
        if (filePath == null || Files.isDirectory(filePath)) {
            return;
        }

        Path normalized = filePath.toAbsolutePath().normalize();
        String key = normalized.toString();
        EditorTab existing = openEditors.get(key);
        if (existing != null) {
            tabPane.getSelectionModel().select(existing.tab);
            existing.area.requestFocus();
            updateCursorStatus();
            updateBottomStatus();
            return;
        }

        String text;
        try {
            text = readFileUtf8(normalized);
        } catch (Exception e) {
            LogWriter.error("Failed to open script file in Nodex IDE: " + normalized, asException(e));
            setStatus("Failed to open: " + getPathName(normalized));
            return;
        }

        CodeArea area = new CodeArea();
        area.replaceText(text);
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.getStyleClass().add("code-area");

        PauseTransition debounce = new PauseTransition(Duration.millis(120));
        Tab tab = new Tab(getPathName(normalized));
        ChangeListener<Number> caretListener = (obs, oldVal, newVal) -> updateCursorStatus();
        EditorTab editorTab = new EditorTab(normalized, tab, area, debounce, caretListener);
        editorTab.lastModifiedMillis = getLastModifiedMillis(normalized);

        area.textProperty().addListener((obs, oldText, newText) -> {
            if (!editorTab.dirty) {
                editorTab.dirty = true;
                refreshTabTitle(editorTab);
            }
            debounce.playFromStart();
        });
        area.caretPositionProperty().addListener(caretListener);
        debounce.setOnFinished(evt -> applyHighlighting(editorTab));

        tab.setOnCloseRequest(evt -> {
            if (!confirmCloseEditor(editorTab)) {
                evt.consume();
            }
        });
        tab.setOnClosed(evt -> removeEditor(editorTab));
        tab.setContent(new VirtualizedScrollPane<CodeArea>(area));

        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        int insertAt = selected != null ? tabPane.getTabs().indexOf(selected) + 1 : tabPane.getTabs().size();
        insertAt = Math.max(0, Math.min(insertAt, tabPane.getTabs().size()));
        tabPane.getTabs().add(insertAt, tab);

        openEditors.put(key, editorTab);
        applyHighlighting(editorTab);
        tabPane.getSelectionModel().select(tab);
        area.requestFocus();
        updateCursorStatus();
        updateBottomStatus();
        setStatus("Opened: " + normalized);
    }

    private void saveActive() {
        EditorTab active = getActiveEditor();
        if (active != null) {
            saveEditor(active);
        }
    }

    private void saveAll() {
        List<EditorTab> editors = new ArrayList<EditorTab>(openEditors.values());
        for (EditorTab editor : editors) {
            saveEditor(editor);
        }
        updateBottomStatus();
    }

    private void saveEditor(EditorTab editor) {
        if (editor == null) {
            return;
        }

        if (editor.isVirtual()) {
            if (activeBinding != null && activeBinding.saveCallback != null) {
                try {
                    activeBinding.saveCallback.onSave(editor.virtualTabIndex, editor.area.getText());
                    editor.dirty = false;
                    refreshTabTitle(editor);
                    setStatus("Saved script: " + editor.displayName);
                } catch (Exception e) {
                    LogWriter.error("Failed to save virtual script in Nodex IDE: " + editor.displayName, asException(e));
                    setStatus("Failed saving: " + editor.displayName);
                }
            }
            return;
        }

        try {
            Path parent = editor.path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.write(editor.path, editor.area.getText().getBytes(StandardCharsets.UTF_8));
            editor.lastModifiedMillis = getLastModifiedMillis(editor.path);
            editor.dirty = false;
            refreshTabTitle(editor);
            setStatus("Saved: " + editor.path);
            refreshTree(editor.path);
        } catch (Exception e) {
            LogWriter.error("Failed to save script file in Nodex IDE: " + editor.path, asException(e));
            setStatus("Failed saving: " + getPathName(editor.path));
        }
    }

    private void closeActiveTabWithPrompt() {
        EditorTab active = getActiveEditor();
        if (active == null) {
            return;
        }
        if (confirmCloseEditor(active)) {
            tabPane.getTabs().remove(active.tab);
            removeEditor(active);
        }
    }

    private boolean confirmCloseAllDirtyEditors() {
        boolean hasDirty = false;
        for (EditorTab editorTab : openEditors.values()) {
            if (editorTab.dirty) {
                hasDirty = true;
                break;
            }
        }
        if (!hasDirty) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Unsaved Files");
        alert.setHeaderText("There are unsaved files.");
        alert.setContentText("Save all changes before closing?");

        ButtonType saveAll = new ButtonType("Save All");
        ButtonType discard = new ButtonType("Discard");
        alert.getButtonTypes().setAll(saveAll, discard, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
            return false;
        }

        if (result.get() == saveAll) {
            saveAll();
            for (EditorTab editorTab : openEditors.values()) {
                if (editorTab.dirty) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean confirmCloseEditor(EditorTab editorTab) {
        if (editorTab == null || !editorTab.dirty) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Close unsaved file?");
        String displayName = editorTab.displayName != null ? editorTab.displayName : getPathName(editorTab.path);
        alert.setContentText(displayName + " has unsaved changes.");

        ButtonType save = new ButtonType("Save");
        ButtonType discard = new ButtonType("Discard");
        alert.getButtonTypes().setAll(save, discard, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
            return false;
        }

        if (result.get() == save) {
            saveEditor(editorTab);
            return !editorTab.dirty;
        }

        return true;
    }

    private void removeEditor(EditorTab editorTab) {
        if (editorTab == null) {
            return;
        }
        editorTab.area.caretPositionProperty().removeListener(editorTab.caretListener);
        openEditors.remove(editorTab.editorKey());
        updateCursorStatus();
        updateBottomStatus();
    }

    private void refreshTabTitle(EditorTab editorTab) {
        if (editorTab == null) {
            return;
        }
        String base = editorTab.displayName != null ? editorTab.displayName : getPathName(editorTab.path);
        editorTab.tab.setText(editorTab.dirty ? base + " *" : base);
    }

    private EditorTab getActiveEditor() {
        if (tabPane == null) {
            return null;
        }
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return null;
        }
        for (EditorTab editorTab : openEditors.values()) {
            if (editorTab.tab == selected) {
                return editorTab;
            }
        }
        return null;
    }

    private void updateCursorStatus() {
        if (cursorLabel == null) {
            return;
        }
        EditorTab active = getActiveEditor();
        if (active == null) {
            cursorLabel.setText("Ln 1, Col 1");
            return;
        }
        int line = active.area.getCurrentParagraph() + 1;
        int col = active.area.getCaretColumn() + 1;
        cursorLabel.setText("Ln " + line + ", Col " + col);
    }

    private void updateBottomStatus() {
        EditorTab active = getActiveEditor();
        if (active == null) {
            setStatus("Ready");
            return;
        }
        LanguageKind language = active.languageOverride != null ? active.languageOverride : inferLanguage(active.path);
        String name = active.isVirtual() ? active.displayName : String.valueOf(active.path);
        setStatus(name + " | " + language.name() + (active.dirty ? " | Unsaved" : " | Saved"));
    }

    private void setStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }

    private void applyHighlighting(EditorTab editorTab) {
        if (editorTab == null) {
            return;
        }

        LanguageKind language = editorTab.languageOverride != null ? editorTab.languageOverride : inferLanguage(editorTab.path);
        Pattern pattern = getPattern(language);
        if (pattern == null) {
            clearHighlighting(editorTab.area);
            return;
        }

        try {
            StyleSpans<Collection<String>> spans = computeHighlighting(editorTab.area.getText(), pattern, language);
            editorTab.area.setStyleSpans(0, spans);
        } catch (Exception ignored) {
        }
    }

    private void clearHighlighting(CodeArea area) {
        String text = area.getText();
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<Collection<String>>();
        builder.add(Collections.<String>emptyList(), text.length());
        area.setStyleSpans(0, builder.create());
    }

    private Pattern getPattern(LanguageKind language) {
        if (language == LanguageKind.JAVA) {
            return JAVA_PATTERN;
        }
        if (language == LanguageKind.JS) {
            return JS_PATTERN;
        }
        if (language == LanguageKind.JSON) {
            return JSON_PATTERN;
        }
        return null;
    }

    private LanguageKind inferLanguage(Path path) {
        if (path == null || path.getFileName() == null) {
            return LanguageKind.PLAIN;
        }
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".java")) {
            return LanguageKind.JAVA;
        }
        if (name.endsWith(".js")) {
            return LanguageKind.JS;
        }
        if (name.endsWith(".json") || name.endsWith(".mcmeta")) {
            return LanguageKind.JSON;
        }
        return LanguageKind.PLAIN;
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text, Pattern pattern, LanguageKind language) {
        Matcher matcher = pattern.matcher(text);
        int last = 0;
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<Collection<String>>();

        while (matcher.find()) {
            String styleClass = pickStyleClass(matcher, language);
            if (styleClass == null) {
                continue;
            }

            builder.add(Collections.<String>emptyList(), matcher.start() - last);
            builder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            last = matcher.end();
        }

        builder.add(Collections.<String>emptyList(), text.length() - last);
        return builder.create();
    }

    private String pickStyleClass(Matcher matcher, LanguageKind language) {
        if (language == LanguageKind.JSON) {
            if (matcher.group("PROPERTY") != null) {
                return "json-property";
            }
            if (matcher.group("STRING") != null) {
                return "json-string";
            }
            if (matcher.group("NUMBER") != null) {
                return "json-number";
            }
            if (matcher.group("BOOLEAN") != null) {
                return "json-boolean";
            }
            if (matcher.group("NULL") != null) {
                return "json-null";
            }
            if (matcher.group("BRACE") != null) {
                return "brace";
            }
            if (matcher.group("BRACKET") != null) {
                return "bracket";
            }
            return null;
        }

        if (matcher.group("COMMENT") != null) {
            return "comment";
        }
        if (matcher.group("STRING") != null) {
            return "string";
        }
        if (matcher.group("ANNOTATION") != null) {
            return "annotation";
        }
        if (matcher.group("MODIFIER") != null) {
            return "modifier";
        }
        if (matcher.group("KEYWORD") != null) {
            return "keyword";
        }
        if (matcher.group("TYPEREF") != null) {
            return "type-ref";
        }
        if (matcher.group("METHODCALL") != null) {
            return "method-call";
        }
        if (matcher.group("PAREN") != null) {
            return "paren";
        }
        if (matcher.group("BRACE") != null) {
            return "brace";
        }
        if (matcher.group("BRACKET") != null) {
            return "bracket";
        }
        if (matcher.group("SEMICOLON") != null) {
            return "semicolon";
        }
        if (matcher.group("NUMBER") != null) {
            return "number";
        }

        return null;
    }

    private void toggleScriptPanel() {
        if (contentSplit == null || scriptPanel == null) return;

        if (contentSplit.getItems().contains(scriptPanel)) {
            if (!contentSplit.getDividers().isEmpty()) {
                lastDividerPosition = contentSplit.getDividers().get(0).getPosition();
            }
            contentSplit.getItems().remove(scriptPanel);
            setStatus("Script panel hidden");
        } else {
            // Remove file tree if showing, then add script panel
            contentSplit.getItems().remove(fileTree);
            contentSplit.getItems().add(0, scriptPanel);
            contentSplit.setDividerPositions(lastDividerPosition > 0.02D ? lastDividerPosition : DEFAULT_DIVIDER);
            setStatus("Script panel shown");
        }
    }

    private void toggleFileTree() {
        if (contentSplit == null || fileTree == null) {
            return;
        }

        if (contentSplit.getItems().contains(fileTree)) {
            if (!contentSplit.getDividers().isEmpty()) {
                lastDividerPosition = contentSplit.getDividers().get(0).getPosition();
            }
            contentSplit.getItems().remove(fileTree);
            // Restore script panel if it was hidden
            if (!contentSplit.getItems().contains(scriptPanel)) {
                contentSplit.getItems().add(0, scriptPanel);
                contentSplit.setDividerPositions(lastDividerPosition > 0.02D ? lastDividerPosition : DEFAULT_DIVIDER);
            }
            setStatus("File tree hidden");
        } else {
            // Remove script panel and show file tree
            contentSplit.getItems().remove(scriptPanel);
            contentSplit.getItems().add(0, fileTree);
            contentSplit.setDividerPositions(lastDividerPosition > 0.02D ? lastDividerPosition : DEFAULT_DIVIDER);
            refreshTree(null);
            setStatus("File tree shown");
        }
    }

    private void addScriptTab() {
        if (activeBinding == null || !activeBinding.canModifyTabs) return;

        // Determine next tab index and name
        int nextIndex = 0;
        for (NodexScriptBinding.ScriptTabData tab : currentTabs) {
            if (tab.index >= nextIndex) nextIndex = tab.index + 1;
        }
        String tabName = "Tab " + (currentTabs.size() + 1);
        String language = currentTabs.isEmpty() ? "ECMAScript" : currentTabs.get(0).language;

        NodexScriptBinding.ScriptTabData newTab = new NodexScriptBinding.ScriptTabData(nextIndex, tabName, "", language);
        currentTabs.add(newTab);
        refreshScriptPanel();

        // Open the new tab in the editor
        openVirtualTab(newTab, activeBinding.saveCallback);
        setStatus("Added: " + tabName);
    }

    private void deleteScriptTab() {
        if (activeBinding == null || !activeBinding.canModifyTabs) return;
        if (currentTabs.isEmpty()) return;

        int selectedIdx = scriptListView != null ? scriptListView.getSelectionModel().getSelectedIndex() : -1;
        if (selectedIdx < 0 || selectedIdx >= currentTabs.size()) return;

        NodexScriptBinding.ScriptTabData tabToDelete = currentTabs.get(selectedIdx);

        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.setTitle("Delete Tab");
        confirm.setHeaderText("Delete \"" + tabToDelete.name + "\"?");
        confirm.setContentText("This will remove the script tab.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) return;

        // Close the corresponding editor tab if open
        String virtualId = "virtual#" + activeBinding.contextId + "#" + tabToDelete.index;
        EditorTab editorTab = openEditors.get(virtualId);
        if (editorTab != null) {
            tabPane.getTabs().remove(editorTab.tab);
            removeEditor(editorTab);
        }

        currentTabs.remove(selectedIdx);
        refreshScriptPanel();
        setStatus("Deleted: " + tabToDelete.name);
    }

    private void fireCloseCallback() {
        if (activeBinding == null || activeBinding.closeCallback == null) return;

        // Collect current state of all tabs
        List<NodexScriptBinding.ScriptTabData> snapshot = new ArrayList<NodexScriptBinding.ScriptTabData>();
        for (NodexScriptBinding.ScriptTabData tab : currentTabs) {
            String virtualId = "virtual#" + activeBinding.contextId + "#" + tab.index;
            EditorTab editor = openEditors.get(virtualId);
            String content = editor != null ? editor.area.getText() : tab.content;
            snapshot.add(new NodexScriptBinding.ScriptTabData(tab.index, tab.name, content, tab.language));
        }

        try {
            activeBinding.closeCallback.onClose(snapshot);
        } catch (Exception e) {
            LogWriter.error("Failed to fire Nodex close callback", asException(e));
        }
    }

    private void createFileInSelectedDirectory() {
        Path dir = resolveSelectedDirectory();
        createFileInDirectory(dir);
    }

    private void createDirectoryInSelectedDirectory() {
        Path dir = resolveSelectedDirectory();
        createDirectoryInDirectory(dir);
    }

    private Path resolveSelectedDirectory() {
        TreeItem<Path> selected = fileTree != null ? fileTree.getSelectionModel().getSelectedItem() : null;
        if (selected == null || selected.getValue() == null) {
            return rootPath;
        }
        Path selectedPath = selected.getValue();
        if (isVirtualPath(selectedPath)) {
            return rootPath;
        }
        if (Files.isDirectory(selectedPath)) {
            return selectedPath;
        }
        return selectedPath.getParent() != null ? selectedPath.getParent() : rootPath;
    }

    private void createFileInDirectory(Path directory) {
        if (directory == null) {
            return;
        }
        Optional<String> name = promptText("New File", "Create file in " + directory, "script.js");
        if (!name.isPresent()) {
            return;
        }

        String raw = sanitizeName(name.get());
        if (raw.length() == 0) {
            return;
        }

        Path filePath = directory.resolve(raw).toAbsolutePath().normalize();
        try {
            if (Files.exists(filePath)) {
                showError("File already exists", filePath.toString());
                return;
            }
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.createFile(filePath);
            refreshTree(filePath);
            openFile(filePath);
            setStatus("Created file: " + filePath);
        } catch (Exception e) {
            LogWriter.error("Failed creating file in Nodex IDE: " + filePath, asException(e));
            showError("Create File Failed", e.getMessage());
        }
    }

    private void createDirectoryInDirectory(Path directory) {
        if (directory == null) {
            return;
        }
        Optional<String> name = promptText("New Folder", "Create folder in " + directory, "new_folder");
        if (!name.isPresent()) {
            return;
        }

        String raw = sanitizeName(name.get());
        if (raw.length() == 0) {
            return;
        }

        Path folder = directory.resolve(raw).toAbsolutePath().normalize();
        try {
            if (Files.exists(folder)) {
                showError("Folder already exists", folder.toString());
                return;
            }
            Files.createDirectories(folder);
            refreshTree(folder);
            revealPath(folder);
            setStatus("Created folder: " + folder);
        } catch (Exception e) {
            LogWriter.error("Failed creating folder in Nodex IDE: " + folder, asException(e));
            showError("Create Folder Failed", e.getMessage());
        }
    }

    private void renameSelected() {
        TreeItem<Path> selected = fileTree != null ? fileTree.getSelectionModel().getSelectedItem() : null;
        if (selected == null || selected.getValue() == null) {
            return;
        }
        renamePath(selected.getValue());
    }

    private void renamePath(Path originalPath) {
        if (originalPath == null || (rootPath != null && rootPath.equals(originalPath))) {
            return;
        }
        String currentName = getPathName(originalPath);
        Optional<String> newName = promptText("Rename", "Rename " + currentName, currentName);
        if (!newName.isPresent()) {
            return;
        }
        String sanitized = sanitizeName(newName.get());
        if (sanitized.length() == 0 || sanitized.equals(currentName)) {
            return;
        }

        Path newPath = originalPath.resolveSibling(sanitized).toAbsolutePath().normalize();
        try {
            if (Files.exists(newPath)) {
                showError("Target already exists", newPath.toString());
                return;
            }
            Files.move(originalPath, newPath);
            handlePathMove(originalPath.toAbsolutePath().normalize(), newPath);
            refreshTree(newPath);
            setStatus("Renamed to: " + newPath);
        } catch (Exception e) {
            LogWriter.error("Failed renaming in Nodex IDE: " + originalPath + " -> " + newPath, asException(e));
            showError("Rename Failed", e.getMessage());
        }
    }

    private void deleteSelected() {
        TreeItem<Path> selected = fileTree != null ? fileTree.getSelectionModel().getSelectedItem() : null;
        if (selected == null || selected.getValue() == null) {
            return;
        }
        deletePath(selected.getValue());
    }

    private void deletePath(Path path) {
        if (path == null || (rootPath != null && rootPath.equals(path))) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(stage);
        confirmation.setTitle("Delete");
        confirmation.setHeaderText("Delete " + getPathName(path) + "?");
        confirmation.setContentText(Files.isDirectory(path) ? "This will delete folder contents recursively." : "This cannot be undone.");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            closeEditorsInside(path);
            deleteRecursively(path);
            refreshTree(path.getParent());
            setStatus("Deleted: " + path);
        } catch (Exception e) {
            LogWriter.error("Failed deleting in Nodex IDE: " + path, asException(e));
            showError("Delete Failed", e.getMessage());
        }
    }

    private void closeEditorsInside(Path basePath) {
        Path normalizedBase = basePath.toAbsolutePath().normalize();
        List<EditorTab> toClose = new ArrayList<EditorTab>();
        for (EditorTab editorTab : openEditors.values()) {
            if (editorTab.isVirtual() || editorTab.path == null) continue;
            if (editorTab.path.startsWith(normalizedBase)) {
                toClose.add(editorTab);
            }
        }
        for (EditorTab editorTab : toClose) {
            tabPane.getTabs().remove(editorTab.tab);
            removeEditor(editorTab);
        }
    }

    private void handlePathMove(Path oldPath, Path newPath) {
        Map<String, EditorTab> updated = new HashMap<String, EditorTab>();
        for (EditorTab editorTab : openEditors.values()) {
            if (editorTab.isVirtual()) {
                updated.put(editorTab.editorKey(), editorTab);
                continue;
            }
            Path editorPath = editorTab.path.toAbsolutePath().normalize();
            if (editorPath.equals(oldPath) || editorPath.startsWith(oldPath)) {
                Path relative = oldPath.relativize(editorPath);
                editorTab.path = newPath.resolve(relative).toAbsolutePath().normalize();
                refreshTabTitle(editorTab);
            }
            updated.put(editorTab.editorKey(), editorTab);
        }

        openEditors.clear();
        openEditors.putAll(updated);
    }

    private void handleTreeDragOver(DragEvent event) {
        if (!event.getDragboard().hasContent(TREE_DRAG_FORMAT)) {
            return;
        }

        TreeItem<Path> targetItem = fileTree.getSelectionModel().getSelectedItem();
        if (targetItem == null || targetItem.getValue() == null) {
            return;
        }

        Path target = targetItem.getValue();
        if (!Files.isDirectory(target)) {
            target = target.getParent();
        }
        if (target == null) {
            return;
        }

        Object sourceObj = event.getDragboard().getContent(TREE_DRAG_FORMAT);
        if (!(sourceObj instanceof String)) {
            return;
        }

        Path source = Paths.get((String) sourceObj).toAbsolutePath().normalize();
        if (target.equals(source) || target.startsWith(source)) {
            return;
        }

        event.acceptTransferModes(TransferMode.MOVE);
        event.consume();
    }

    private void handleTreeDragDropped(DragEvent event) {
        boolean success = false;
        try {
            if (!event.getDragboard().hasContent(TREE_DRAG_FORMAT)) {
                return;
            }

            TreeItem<Path> targetItem = fileTree.getSelectionModel().getSelectedItem();
            if (targetItem == null || targetItem.getValue() == null) {
                return;
            }

            Path targetDir = targetItem.getValue();
            if (!Files.isDirectory(targetDir)) {
                targetDir = targetDir.getParent();
            }
            if (targetDir == null) {
                return;
            }

            Object sourceObj = event.getDragboard().getContent(TREE_DRAG_FORMAT);
            if (!(sourceObj instanceof String)) {
                return;
            }
            Path source = Paths.get((String) sourceObj).toAbsolutePath().normalize();
            Path destination = targetDir.resolve(source.getFileName()).toAbsolutePath().normalize();

            if (destination.equals(source) || destination.startsWith(source)) {
                return;
            }
            if (Files.exists(destination)) {
                showError("Move failed", "Target already exists: " + destination);
                return;
            }

            Files.move(source, destination);
            handlePathMove(source, destination);
            refreshTree(destination);
            setStatus("Moved: " + source + " -> " + destination);
            success = true;
        } catch (Exception e) {
            LogWriter.error("Failed moving path in Nodex IDE", asException(e));
            showError("Move Failed", e.getMessage());
        } finally {
            event.setDropCompleted(success);
            event.consume();
        }
    }

    private void reopenExistingTabsWithinRoot() {
        List<Path> reopenPaths = new ArrayList<Path>();
        for (EditorTab editorTab : openEditors.values()) {
            if (editorTab.isVirtual()) continue;
            if (rootPath != null && editorTab.path != null && editorTab.path.startsWith(rootPath)) {
                reopenPaths.add(editorTab.path);
            }
        }

        openEditors.clear();
        if (tabPane != null) {
            tabPane.getTabs().clear();
        }

        for (Path path : reopenPaths) {
            if (Files.exists(path) && Files.isRegularFile(path)) {
                openFile(path);
            }
        }
    }

    private void startWatcher() {
        stopWatcher();
        if (rootPath == null || !Files.exists(rootPath)) {
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            watchedDirectories.clear();
            registerDirectoryTree(rootPath);
            watcherRunning.set(true);

            watchThread = new Thread(this::watchLoop, "CNPC-Nodex-Watcher");
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (Exception e) {
            LogWriter.error("Failed to start filesystem watcher for Nodex IDE", asException(e));
            stopWatcher();
        }
    }

    private void stopWatcher() {
        watcherRunning.set(false);
        if (watchThread != null) {
            watchThread.interrupt();
            watchThread = null;
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
            watchService = null;
        }
        watchedDirectories.clear();
    }

    private void watchLoop() {
        while (watcherRunning.get()) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            } catch (ClosedWatchServiceException ignored) {
                return;
            }

            boolean changed = false;
            Path watchRoot = (Path) key.watchable();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                changed = true;

                Object context = event.context();
                if (context instanceof Path) {
                    Path child = watchRoot.resolve((Path) context).toAbsolutePath().normalize();
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(child)) {
                        registerDirectoryTree(child);
                    }
                }
            }

            key.reset();
            if (changed) {
                scheduleExternalRefresh();
            }
        }
    }

    private void scheduleExternalRefresh() {
        if (!pendingRefresh.compareAndSet(false, true)) {
            return;
        }
        Platform.runLater(() -> {
            pendingRefresh.set(false);
            refreshTree(null);
            syncOpenEditorsFromDisk();
        });
    }

    private void syncOpenEditorsFromDisk() {
        List<EditorTab> editors = new ArrayList<EditorTab>(openEditors.values());
        for (EditorTab editorTab : editors) {
            if (editorTab.isVirtual()) continue;
            try {
                if (!Files.exists(editorTab.path)) {
                    if (!editorTab.dirty) {
                        tabPane.getTabs().remove(editorTab.tab);
                        removeEditor(editorTab);
                    }
                    continue;
                }

                long diskTimestamp = getLastModifiedMillis(editorTab.path);
                if (diskTimestamp <= 0L || diskTimestamp == editorTab.lastModifiedMillis) {
                    continue;
                }

                if (!editorTab.dirty) {
                    String fresh = readFileUtf8(editorTab.path);
                    if (!fresh.equals(editorTab.area.getText())) {
                        editorTab.area.replaceText(fresh);
                        editorTab.lastModifiedMillis = diskTimestamp;
                        editorTab.dirty = false;
                        refreshTabTitle(editorTab);
                        applyHighlighting(editorTab);
                    }
                }
            } catch (Exception e) {
                LogWriter.error("Failed syncing file changes in Nodex IDE: " + editorTab.path, asException(e));
            }
        }
        updateBottomStatus();
    }

    private void registerDirectoryTree(Path rootDirectory) {
        if (rootDirectory == null || !Files.isDirectory(rootDirectory) || watchService == null) {
            return;
        }

        try {
            Files.walkFileTree(rootDirectory, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    registerWatchDirectory(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            LogWriter.error("Failed registering directory tree in Nodex IDE: " + rootDirectory, asException(e));
        }
    }

    private void registerWatchDirectory(Path dir) {
        Path normalized = dir.toAbsolutePath().normalize();
        if (watchedDirectories.contains(normalized)) {
            return;
        }
        try {
            normalized.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
            watchedDirectories.add(normalized);
        } catch (Exception ignored) {
        }
    }

    private Optional<String> promptText(String title, String header, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.initOwner(stage);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("Name:");
        return dialog.showAndWait();
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Nodex IDE");
        alert.setHeaderText(header);
        alert.setContentText(content != null ? content : "");
        alert.showAndWait();
    }

    private void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void deleteRecursively(Path rootToDelete) throws IOException {
        if (!Files.exists(rootToDelete)) {
            return;
        }
        Files.walkFileTree(rootToDelete, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void ensureRootExists(Path normalizedRoot) {
        if (Files.exists(normalizedRoot)) {
            return;
        }
        try {
            Files.createDirectories(normalizedRoot);
        } catch (Exception e) {
            LogWriter.error("Failed to create script root for Nodex IDE: " + normalizedRoot, asException(e));
        }
    }

    private long getLastModifiedMillis(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private String readFileUtf8(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String sanitizeName(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.length() == 0) {
            return "";
        }
        return trimmed.replace('\\', '_').replace('/', '_');
    }

    private String getPathName(Path path) {
        if (path == null) {
            return "";
        }
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : path.toString();
    }

    private Exception asException(Throwable t) {
        if (t instanceof Exception) {
            return (Exception) t;
        }
        return new Exception(t);
    }

    private static final Comparator<Path> PATH_COMPARATOR = new Comparator<Path>() {
        @Override
        public int compare(Path left, Path right) {
            boolean leftDir = Files.isDirectory(left);
            boolean rightDir = Files.isDirectory(right);
            if (leftDir != rightDir) {
                return leftDir ? -1 : 1;
            }
            return naturalCompare(getNameForCompare(left), getNameForCompare(right));
        }
    };

    private static String getNameForCompare(Path path) {
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : path.toString();
    }

    private static int naturalCompare(String a, String b) {
        int i = 0;
        int j = 0;

        while (i < a.length() && j < b.length()) {
            char ca = a.charAt(i);
            char cb = b.charAt(j);

            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                int startI = i;
                int startJ = j;

                while (i < a.length() && Character.isDigit(a.charAt(i))) {
                    i++;
                }
                while (j < b.length() && Character.isDigit(b.charAt(j))) {
                    j++;
                }

                String numA = a.substring(startI, i);
                String numB = b.substring(startJ, j);

                if (numA.length() != numB.length()) {
                    return numA.length() - numB.length();
                }

                int cmp = numA.compareTo(numB);
                if (cmp != 0) {
                    return cmp;
                }
            } else {
                int cmp = Character.toLowerCase(ca) - Character.toLowerCase(cb);
                if (cmp != 0) {
                    return cmp;
                }
                i++;
                j++;
            }
        }
        return a.length() - b.length();
    }
}
