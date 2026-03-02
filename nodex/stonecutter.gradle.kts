plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.13-SNAPSHOT" apply false

}

stonecutter active "1.21.10"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
}