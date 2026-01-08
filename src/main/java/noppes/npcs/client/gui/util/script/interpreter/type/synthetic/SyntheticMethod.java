package noppes.npcs.client.gui.util.script.interpreter.type.synthetic;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocParamTag;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocReturnTag;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SyntheticMethod {
    public final String name;
    public final String returnType;
    public final List<SyntheticParameter> parameters;
    public final String documentation;
    public final boolean isStatic;
    public final SyntheticTypeBuilder.ReturnTypeResolver returnTypeResolver;

    SyntheticMethod(String name, String returnType, List<SyntheticParameter> parameters,
                    String documentation, boolean isStatic,
                    SyntheticTypeBuilder.ReturnTypeResolver returnTypeResolver) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.documentation = documentation;
        this.isStatic = isStatic;
        this.returnTypeResolver = returnTypeResolver;
    }

    /**
     * Create a MethodInfo from this synthetic method.
     */
    public MethodInfo toMethodInfo(TypeInfo containingType) {
        List<FieldInfo> paramInfos = new ArrayList<>();
        for (SyntheticParameter param : parameters) {
            TypeInfo paramType = TypeResolver.getInstance().resolve(param.typeName);
            if (paramType == null) {
                paramType = TypeInfo.unresolved(param.typeName, param.typeName);
            }
            paramInfos.add(FieldInfo.parameter(param.name, paramType, -1, null));
        }

        TypeInfo returnTypeInfo = TypeResolver.getInstance().resolve(returnType);
        if (returnTypeInfo == null) {
            returnTypeInfo = TypeInfo.unresolved(returnType, returnType);
        }

        int modifiers = Modifier.PUBLIC;
        if (isStatic) {
            modifiers |= Modifier.STATIC;
        }

        MethodInfo methodInfo = MethodInfo.external(name, returnTypeInfo, containingType, paramInfos, modifiers, null);

        // Create JSDocInfo from documentation
        if (documentation != null && !documentation.isEmpty()) {
            JSDocInfo jsDocInfo = createJSDocInfo(returnTypeInfo);
            methodInfo.setJSDocInfo(jsDocInfo);
        }

        return methodInfo;
    }

    /**
     * Create JSDocInfo from the documentation string.
     */
    private JSDocInfo createJSDocInfo(TypeInfo returnTypeInfo) {
        // Parse the documentation to extract description and separate sections
        String[] lines = documentation.split("\\n");
        StringBuilder descBuilder = new StringBuilder();

        // Extract description (everything before @param or @returns)
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("@param") || line.startsWith("@returns") || line.startsWith("@return")) {
                break;
            }
            if (!line.isEmpty()) {
                if (descBuilder.length() > 0)
                    descBuilder.append("\n");
                descBuilder.append(line);
            }
        }

        JSDocInfo jsDocInfo = new JSDocInfo(documentation, -1, -1);
        jsDocInfo.setDescription(descBuilder.toString());

        // Add @param tags for each parameter
        for (int i = 0; i < parameters.size(); i++) {
            SyntheticParameter param = parameters.get(i);
            TypeInfo paramType = TypeResolver.getInstance().resolve(param.typeName);
            if (paramType == null) {
                paramType = TypeInfo.unresolved(param.typeName, param.typeName);
            }

            JSDocParamTag paramTag = JSDocParamTag.create(
                    -1, -1, -1,  // offsets
                    param.typeName, paramType, -1, -1,  // type info
                    param.name, -1, -1,  // param name
                    param.documentation  // description
            );
            jsDocInfo.addParamTag(paramTag);
        }

        // Add @returns tag
        JSDocReturnTag returnTag = JSDocReturnTag.create(
                "returns", -1, -1, -1,  // offsets
                returnTypeInfo.getSimpleName(), returnTypeInfo, -1, -1,  // type info
                null  // description extracted from main documentation
        );
        jsDocInfo.setReturnTag(returnTag);

        return jsDocInfo;
    }

    /**
     * Get signature string for display.
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0)
                sb.append(", ");
            SyntheticParameter p = parameters.get(i);
            sb.append(p.name).append(": ").append(p.typeName);
        }
        sb.append("): ").append(returnType);
        return sb.toString();
    }
}
