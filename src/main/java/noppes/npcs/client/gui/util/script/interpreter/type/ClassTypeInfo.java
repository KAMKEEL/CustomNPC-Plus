package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a reference to a Java Class itself (not an instance).
 * Used for patterns like: var File = Java.type("java.io.File");
 * 
 * When you have a ClassTypeInfo:
 * - Accessing members should show ONLY static members (File.listRoots())
 * - Using 'new' should return an instance of the wrapped class (new File("path"))
 * - Calling .constructor(...) should return an instance of the wrapped class
 * 
 * This is distinct from a regular TypeInfo which represents an instance of that type.
 */
public class ClassTypeInfo extends TypeInfo {
    
    private final TypeInfo instanceType;  // The type you get when you do 'new' on this class
    private List<MethodInfo> constructorMethod;
    private List<FieldInfo> classField;
    
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
    public List<MethodInfo> getSyntheticMethods() {
        List<MethodInfo> base = super.getSyntheticMethods();

        // if (constructorMethod == null) {
        constructorMethod = buildConstructorMethod();
        //}

        if (constructorMethod.isEmpty()) {
            return base;
        }

        List<MethodInfo> merged = new ArrayList<>(base.size() + constructorMethod.size());
        merged.addAll(base);
        merged.addAll(constructorMethod);
        return merged;
    }

    @Override
    public List<FieldInfo> getSyntheticFields() {
        List<FieldInfo> base = super.getSyntheticFields();

        if (classField == null) {
            classField = buildClassField();
        }

        if (classField.isEmpty()) {
            return base;
        }

        List<FieldInfo> merged = new ArrayList<>(base.size() + classField.size());
        merged.addAll(base);
        merged.addAll(classField);
        return merged;
    }

    private List<MethodInfo> buildConstructorMethod() {
        if (instanceType == null) {
            return Collections.emptyList();
        }

        FieldInfo argsParam = FieldInfo.parameter("args", TypeInfo.OBJECT, -1, null);
        argsParam.setVarArg(true);

        List<FieldInfo> params = new ArrayList<>();
        params.add(argsParam);

        MethodInfo ctor = MethodInfo.external(
                "constructor",
                instanceType,
                this,
                params,
                Modifier.PUBLIC | Modifier.STATIC,
                "Creates a new instance of " + instanceType.getSimpleName() + ".\n\n" +
                        "Equivalent to using the 'new' operator on this class reference."
        );

        return Collections.singletonList(ctor);
    }

    private List<FieldInfo> buildClassField() {
        if (getJavaClass() == null) {
            return Collections.emptyList();
        }
        try {
            TypeInfo classType = TypeInfo.fromClass(Class.class).parameterize(instanceType);
            FieldInfo classField = FieldInfo.external("class", classType,
                    "The Class object for " + getSimpleName(), Modifier.PUBLIC | Modifier.STATIC);
            return Collections.singletonList(classField);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String toString() {
        return "Class<" + getSimpleName() + ">";
    }
}
