package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DtsModScanner {

    private static final Pattern MOD_DTS_PATH_PATTERN = Pattern.compile("^assets/([^/]+)/api/(.+\\.d\\.ts)$");

    private static final List<String> DOMAIN_PRIORITY = Arrays.asList("customnpcs", "npcdbc");

    private DtsModScanner() {}

    static List<DtsFileRef> collectDtsFilesFromMods() {
        List<DtsFileRef> dtsFiles = new ArrayList<>();
        for (ModContainer mod : Loader.instance().getModList()) {
            File source = mod.getSource();
            if (source == null || !source.exists()) {
                continue;
            }
            if (source.isDirectory()) {
                scanDirectoryForModDts(source, mod.getModId(), dtsFiles);
            } else if (source.getName().endsWith(".jar") || source.getName().endsWith(".zip")) {
                scanJarForModDts(source, mod.getModId(), dtsFiles);
            }
        }
        return dtsFiles;
    }

    static void sortDtsFiles(List<DtsFileRef> dtsFiles) {
        Collections.sort(dtsFiles, new DtsFileRefComparator());
    }

    static void logSummary(List<DtsFileRef> dtsFiles) {
        Map<String, Integer> domainCounts = new LinkedHashMap<>();
        for (DtsFileRef ref : dtsFiles) {
            domainCounts.put(ref.getDomain(), domainCounts.getOrDefault(ref.getDomain(), 0) + 1);
        }
        System.out.println("[JSTypeRegistry] Found " + dtsFiles.size() + " .d.ts files across " + domainCounts.size() + " domains");
        for (Map.Entry<String, Integer> entry : domainCounts.entrySet()) {
            System.out.println("[JSTypeRegistry] Domain " + entry.getKey() + ": " + entry.getValue() + " files");
        }
    }

    private static void scanDirectoryForModDts(File baseDir, String modId, List<DtsFileRef> dtsFiles) {
        scanDirectoryForModDts(baseDir, baseDir, modId, dtsFiles);
    }

    private static void scanDirectoryForModDts(File baseDir, File directory, String modId, List<DtsFileRef> dtsFiles) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryForModDts(baseDir, file, modId, dtsFiles);
                continue;
            }
            if (!file.getName().endsWith(".d.ts")) {
                continue;
            }
            String relativePath = baseDir.toURI().relativize(file.toURI()).getPath();
            if (relativePath == null) {
                continue;
            }
            String normalized = relativePath.replace('\\', '/');
            Matcher matcher = MOD_DTS_PATH_PATTERN.matcher(normalized);
            if (matcher.matches()) {
                String domain = matcher.group(1);
                String apiPath = matcher.group(2);
                dtsFiles.add(DtsFileRef.forFile(modId, domain, apiPath, file));
            }
        }
    }

    private static void scanJarForModDts(File jarFile, String modId, List<DtsFileRef> dtsFiles) {
        try {
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                Matcher matcher = MOD_DTS_PATH_PATTERN.matcher(entryName);
                if (matcher.matches()) {
                    String domain = matcher.group(1);
                    String apiPath = matcher.group(2);
                    dtsFiles.add(DtsFileRef.forJar(modId, domain, apiPath, jarFile, entryName));
                }
            }
            jar.close();
        } catch (Exception e) {
            System.err.println("[JSTypeRegistry] Error scanning mod jar for .d.ts files: " + e.getMessage());
        }
    }

    static final class DtsFileRef {
        private final String modId;
        private final String domain;
        private final String relativePath;
        private final File file;
        private final File jarFile;
        private final String jarEntryName;

        private DtsFileRef(String modId, String domain, String relativePath, File file, File jarFile, String jarEntryName) {
            this.modId = modId;
            this.domain = domain;
            this.relativePath = relativePath;
            this.file = file;
            this.jarFile = jarFile;
            this.jarEntryName = jarEntryName;
        }

        static DtsFileRef forFile(String modId, String domain, String relativePath, File file) {
            return new DtsFileRef(modId, domain, relativePath, file, null, null);
        }

        static DtsFileRef forJar(String modId, String domain, String relativePath, File jarFile, String jarEntryName) {
            return new DtsFileRef(modId, domain, relativePath, null, jarFile, jarEntryName);
        }

        InputStream openStream() throws IOException {
            if (file != null) {
                return new FileInputStream(file);
            }
            if (jarFile != null && jarEntryName != null) {
                final JarFile jar = new JarFile(jarFile);
                JarEntry entry = jar.getJarEntry(jarEntryName);
                if (entry == null) {
                    jar.close();
                    return null;
                }
                InputStream is = jar.getInputStream(entry);
                return new FilterInputStream(is) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        jar.close();
                    }
                };
            }
            return null;
        }

        String getDomain() {
            return domain;
        }

        String getRelativePath() {
            return relativePath;
        }

        String getOrigin() {
            return modId + ":" + domain + ":" + relativePath;
        }
    }

    private static class DtsFileRefComparator implements Comparator<DtsFileRef> {
        @Override
        public int compare(DtsFileRef a, DtsFileRef b) {
            int domainCompare = Integer.compare(getDomainRank(a.getDomain()), getDomainRank(b.getDomain()));
            if (domainCompare != 0) {
                return domainCompare;
            }
            int domainNameCompare = a.getDomain().compareTo(b.getDomain());
            if (domainNameCompare != 0) {
                return domainNameCompare;
            }
            int filePriorityCompare = Integer.compare(getFilePriority(a.getRelativePath()), getFilePriority(b.getRelativePath()));
            if (filePriorityCompare != 0) {
                return filePriorityCompare;
            }
            return a.getRelativePath().compareTo(b.getRelativePath());
        }

        private int getDomainRank(String domain) {
            int idx = DOMAIN_PRIORITY.indexOf(domain);
            return idx >= 0 ? idx : DOMAIN_PRIORITY.size();
        }

        private int getFilePriority(String relativePath) {
            if (relativePath.endsWith("hooks.d.ts")) {
                return 0;
            }
            if (relativePath.endsWith("index.d.ts")) {
                return 1;
            }
            return 2;
        }
    }
}
