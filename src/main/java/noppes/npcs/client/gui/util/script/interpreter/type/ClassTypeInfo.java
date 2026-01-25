package noppes.npcs.client.gui.util.script.interpreter.type;

/**
 * Represents a reference to a Java Class itself (not an instance).
 * Used for patterns like: var File = Java.type("java.io.File");
 * 
 * When you have a ClassTypeInfo:
 * - Accessing members should show ONLY static members (File.listRoots())
 * - Using 'new' should return an instance of the wrapped class (new File("path"))
 * 
 * This is distinct from a regular TypeInfo which represents an instance of that type.
 */
public class ClassTypeInfo extends TypeInfo {
    
    private final TypeInfo instanceType;  // The type you get when you do 'new' on this class
    
    /**
     * Create a ClassTypeInfo wrapping a Java class.
     * 
     * @param javaClass The Java class this represents (e.g., java.io.File.class)
     */
    public ClassTypeInfo(Class<?> javaClass) {
        super(
            javaClass.getSimpleName(),
            javaClass.getName(),
            javaClass.getPackage() != null ? javaClass.getPackage().getName() : "",
            javaClass.isInterface() ? Kind.INTERFACE : (javaClass.isEnum() ? Kind.ENUM : Kind.CLASS),
            javaClass,
            true,
            null,
            true  // subclass marker
        );
        this.instanceType = TypeInfo.fromClass(javaClass);
    }
    
    /**
     * Create a ClassTypeInfo from an existing TypeInfo.
     */
    public ClassTypeInfo(TypeInfo instanceType) {
        super(
            instanceType.getSimpleName(),
            instanceType.getFullName(),
            instanceType.getPackageName(),
            instanceType.getKind(),
            instanceType.getJavaClass(),
            instanceType.isResolved(),
            instanceType.getEnclosingType(),
            true  // subclass marker
        );
        this.instanceType = instanceType;
    }
    
    /**
     * Returns true - this TypeInfo represents a class reference, not an instance.
     */
    @Override
    public boolean isClassReference() {
        return true;
    }
    
    /**
     * Get the TypeInfo for instances of this class.
     * This is what you get when you call 'new' on this class.
     */
    public TypeInfo getInstanceType() {
        return instanceType;
    }
    
    @Override
    public String toString() {
        return "Class<" + getSimpleName() + ">";
    }
}
