package noppes.npcs.client.gui.util.script;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * PackageFinder builds an index of package names found on the provided ClassLoader
 * (scanning jar files and directories). After construction queries via {@link #contains}
 * are O(1) HashSet lookups.
 */
public class PackageFinder {
    private static  PackageFinder INSTANCE = null;
    private final Set<String> packages = new HashSet<>();

    public static PackageFinder Instance() throws IOException {
        if (INSTANCE == null) 
            INSTANCE = new PackageFinder(Thread.currentThread().getContextClassLoader());
        
        return INSTANCE;
    }

    private PackageFinder(ClassLoader cl) throws IOException {
        if (cl == null)
            cl = Thread.currentThread().getContextClassLoader();

        // Try URLClassLoader first (common on most JVM versions used here)
        if (cl instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) cl).getURLs();
            for (URL url : urls) {
                try {
                    String protocol = url.getProtocol();
                    if ("file".equals(protocol)) {
                        File f;
                        try {
                            URI u = url.toURI();
                            f = new File(u);
                        } catch (Exception ex) {
                            f = new File(url.getPath());
                        }
                        if (f.exists()) {
                            if (f.isDirectory())
                                scanDirectory(f);
                            else if (f.isFile() && f.getName().endsWith(".jar"))
                                scanJar(f);
                        }
                    } else if ("jar".equals(protocol)) {
                        // jar:file:/path/to/jar.jar!/...
                        String s = url.getPath();
                        int excl = s.indexOf('!');
                        String jarPath = (excl >= 0) ? s.substring(0, excl) : s;
                        if (jarPath.startsWith("file:"))
                            jarPath = jarPath.substring(5);
                        File jf = new File(jarPath);
                        if (jf.exists())
                            scanJar(jf);
                    }
                } catch (IOException ignored) {
                    // ignore individual entries
                }
            }
        } else {
            // Fallback: scan system classpath as a best-effort
            String cp = System.getProperty("java.class.path", "");
            String[] entries = cp.split(File.pathSeparator);
            for (String e : entries) {
                if (e == null || e.isEmpty())
                    continue;
                File f = new File(e);
                if (!f.exists())
                    continue;
                if (f.isDirectory())
                    scanDirectory(f);
                else if (f.isFile() && f.getName().endsWith(".jar"))
                    scanJar(f);
            }
        }
    }

    private void scanJar(File jarFile) throws IOException {
        try (JarFile jf = new JarFile(jarFile)) {
            Enumeration<JarEntry> en = jf.entries();
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                String name = je.getName();
                if (name.endsWith(".class")) {
                    int idx = name.lastIndexOf('/');
                    if (idx > 0) {
                        String pkg = name.substring(0, idx).replace('/', '.');
                        addPackageHierarchy(pkg);
                    }
                }
            }
        }
    }

    private void scanDirectory(File dir) throws IOException {
        final Path root = dir.toPath();
        if (!Files.exists(root))
            return;
        Files.walk(root).forEach(p -> {
            try {
                if (Files.isRegularFile(p) && p.toString().endsWith(".class")) {
                    Path parent = p.getParent();
                    if (parent == null)
                        return;
                    Path rel = root.relativize(parent);
                    String rp = rel.toString();
                    if (rp.isEmpty())
                        return;
                    // Normalize separators to dot
                    String pkg = rp.replace(File.separatorChar, '.');
                    addPackageHierarchy(pkg);
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void addPackageHierarchy(String pkg) {
        if (pkg == null || pkg.isEmpty())
            return;
        String cur = pkg;
        while (true) {
            if (!cur.isEmpty())
                packages.add(cur);
            int idx = cur.lastIndexOf('.');
            if (idx < 0)
                break;
            cur = cur.substring(0, idx);
        }
    }

    public boolean contains(String pkg) {
        if (pkg == null || pkg.isEmpty())
            return false;
        return packages.contains(pkg);
    }

    // Convenience factory using the context classloader
    public static PackageFinder fromContext() throws IOException {
        return new PackageFinder(Thread.currentThread().getContextClassLoader());
    }

    // Demo main that checks the given package (or 'kamkeel.npcdbc' by default)
    public static void main(String[] args) throws Exception {
        PackageFinder pf = PackageFinder.fromContext();
        String pkg = (args != null && args.length > 0) ? args[0] : "kamkeel.npcdbc";
        System.out.println(pkg + " exists? " + pf.contains(pkg));
    }
}
