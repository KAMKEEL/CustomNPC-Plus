package noppes.npcs.client.gui.util.script.interpreter.expression;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.InnerCallableScope;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.ClassTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.OverloadSelector;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.ArrayList;
import java.util.List;

public class ExpressionTypeResolver {
    private final ExpressionNode.TypeResolverContext context;
    private final ScriptDocument document;  // Access to document for inner scope lookups
    private final int basePosition;  // Document offset for this expression
    
    /**
     * Static field to track the expected/desired type for the current expression being resolved.
     * This allows TypeRules to validate type compatibility in context (e.g., ternary operator branches
     * must be compatible with the assignment target type).
     * Should be set before calling resolve() and cleared after.
     */
    public static TypeInfo CURRENT_EXPECTED_TYPE = null;
    
    public ExpressionTypeResolver(ExpressionNode.TypeResolverContext context) {
        this.context = context;
        this.document = null;
        this.basePosition = 0;
    }
    
    public ExpressionTypeResolver(ExpressionNode.TypeResolverContext context, ScriptDocument document) {
        this(context, document, 0);
    }
    
    public ExpressionTypeResolver(ExpressionNode.TypeResolverContext context, ScriptDocument document, int basePosition) {
        this.context = context;
        this.document = document;
        this.basePosition = basePosition;
    }
    
    public TypeInfo resolve(String expression) {
        if (expression == null || expression.trim().isEmpty()) return null;
        
        try {
            List<ExpressionToken> tokens = ExpressionTokenizer.tokenize(expression);
            if (tokens.isEmpty()) return null;
            
            ExpressionParser parser = new ExpressionParser(tokens);
            ExpressionNode ast = parser.parse();
            if (ast == null) return null;
            
            return resolveNodeType(ast);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean containsOperators(String expression) {
        if (expression == null) return false;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            switch (c) {
                case '+': case '-': case '*': case '/': case '%':
                case '&': case '|': case '^': case '~':
                case '<': case '>': case '=': case '!':
                case '?': case ':': return true;
            }
        }
        return false;
    }
    
    private TypeInfo resolveNodeType(ExpressionNode node) {
        if (node == null) return null;
        
        if (node instanceof ExpressionNode.IntLiteralNode) {
            if (CURRENT_EXPECTED_TYPE != null) {
                TypeInfo narrowed = TypeChecker.narrowLiteralToExpectedType(
                        ((ExpressionNode.IntLiteralNode) node).getValue(), CURRENT_EXPECTED_TYPE);
                if (narrowed != null) return narrowed;
            }
            return TypeInfo.fromPrimitive("int");
        }
        if (node instanceof ExpressionNode.LongLiteralNode) return TypeInfo.fromPrimitive("long");
        if (node instanceof ExpressionNode.FloatLiteralNode) return TypeInfo.fromPrimitive("float");
        if (node instanceof ExpressionNode.DoubleLiteralNode) return TypeInfo.fromPrimitive("double");
        if (node instanceof ExpressionNode.BooleanLiteralNode) return TypeInfo.fromPrimitive("boolean");
        if (node instanceof ExpressionNode.CharLiteralNode) return TypeInfo.fromPrimitive("char");
        if (node instanceof ExpressionNode.StringLiteralNode) return TypeInfo.fromClass(String.class);
        if (node instanceof ExpressionNode.NullLiteralNode) return TypeInfo.unresolved("null", "<null>");
        
        if (node instanceof ExpressionNode.IdentifierNode) {
            return context.resolveIdentifier(((ExpressionNode.IdentifierNode) node).getName());
        }
        
        if (node instanceof ExpressionNode.MemberAccessNode) {
            ExpressionNode.MemberAccessNode ma = (ExpressionNode.MemberAccessNode) node;
            TypeInfo targetType = resolveNodeType(ma.getTarget());
            if (targetType == null || !targetType.isResolved()) return null;
            if (targetType == TypeInfo.ANY || "any".equals(targetType.getFullName())) return TypeInfo.ANY;
            return context.resolveMemberAccess(targetType, ma.getMemberName());
        }
        
        if (node instanceof ExpressionNode.MethodCallNode) {
            ExpressionNode.MethodCallNode mc = (ExpressionNode.MethodCallNode) node;
            TypeInfo targetType = mc.getTarget() == null ? context.resolveIdentifier("this") : resolveNodeType(mc.getTarget());
            if (targetType == null || !targetType.isResolved()) return null;
            if (targetType == TypeInfo.ANY || "any".equals(targetType.getFullName())) return TypeInfo.ANY;
            TypeInfo[] argTypes = new TypeInfo[mc.getArguments().size()];
            for (int i = 0; i < argTypes.length; i++) argTypes[i] = resolveNodeType(mc.getArguments().get(i));
            return context.resolveMethodCall(targetType, mc.getMethodName(), argTypes);
        }
        
        if (node instanceof ExpressionNode.ArrayAccessNode) {
            ExpressionNode.ArrayAccessNode aa = (ExpressionNode.ArrayAccessNode) node;
            TypeInfo arrayType = resolveNodeType(aa.getArray());
            if (arrayType == null || !arrayType.isResolved()) return null;
            if (arrayType == TypeInfo.ANY || "any".equals(arrayType.getFullName())) return TypeInfo.ANY;
            String typeName = arrayType.getFullName();
            if (typeName.endsWith("[]")) {
                String elementTypeName = typeName.substring(0, typeName.length() - 2);
                return context.resolveTypeName(elementTypeName);
            }
            return context.resolveArrayAccess(arrayType);
        }
        
        if (node instanceof ExpressionNode.NewNode) {
            return context.resolveTypeName(((ExpressionNode.NewNode) node).getTypeName());
        }
        
        if (node instanceof ExpressionNode.BinaryOpNode) {
            ExpressionNode.BinaryOpNode bin = (ExpressionNode.BinaryOpNode) node;
            TypeInfo leftType = resolveNodeType(bin.getLeft());
            TypeInfo rightType = resolveNodeType(bin.getRight());
            return TypeRules.resolveBinaryOperatorType(bin.getOperator(), leftType, rightType);
        }
        
        if (node instanceof ExpressionNode.UnaryOpNode) {
            ExpressionNode.UnaryOpNode un = (ExpressionNode.UnaryOpNode) node;
            // Special case: negation of an IntLiteralNode can narrow to byte/short
            if (un.getOperator() == OperatorType.UNARY_MINUS
                    && un.getOperand() instanceof ExpressionNode.IntLiteralNode
                    && CURRENT_EXPECTED_TYPE != null) {
                String negatedText = "-" + ((ExpressionNode.IntLiteralNode) un.getOperand()).getValue();
                TypeInfo narrowed = TypeChecker.narrowLiteralToExpectedType(negatedText, CURRENT_EXPECTED_TYPE);
                if (narrowed != null) return narrowed;
            }
            TypeInfo operandType = resolveNodeType(un.getOperand());
            return TypeRules.resolveUnaryOperatorType(un.getOperator(), operandType);
        }
        
        if (node instanceof ExpressionNode.TernaryNode) {
            ExpressionNode.TernaryNode tern = (ExpressionNode.TernaryNode) node;
            TypeInfo thenType = resolveNodeType(tern.getThenExpr());
            TypeInfo elseType = resolveNodeType(tern.getElseExpr());
            return TypeRules.resolveTernaryType(thenType, elseType);
        }
        
        if (node instanceof ExpressionNode.CastNode) {
            return context.resolveTypeName(((ExpressionNode.CastNode) node).getTypeName());
        }
        
        if (node instanceof ExpressionNode.InstanceofNode) {
            return TypeInfo.fromPrimitive("boolean");
        }
        
        if (node instanceof ExpressionNode.AssignmentNode) {
            return resolveNodeType(((ExpressionNode.AssignmentNode) node).getTarget());
        }
        
        if (node instanceof ExpressionNode.ParenthesizedNode) {
            return resolveNodeType(((ExpressionNode.ParenthesizedNode) node).getInner());
        }
        
        if (node instanceof ExpressionNode.LambdaNode) {
            return resolveLambdaType((ExpressionNode.LambdaNode) node);
        }
        
        if (node instanceof ExpressionNode.JSFunctionNode) {
            return resolveJSFunctionType((ExpressionNode.JSFunctionNode) node);
        }
        
        if (node instanceof ExpressionNode.MethodReferenceNode) {
            return resolveMethodReferenceType((ExpressionNode.MethodReferenceNode) node);
        }
        
        return null;
    }
    
    private TypeInfo resolveLambdaType(ExpressionNode.LambdaNode lambda) {
        // Lambda type is determined by the expected functional interface
        TypeInfo expectedType = CURRENT_EXPECTED_TYPE;
        
        if (expectedType == null) {
            // No expected type context
            // Return a generic Object type
            return TypeInfo.fromClass(Object.class);
        }
        
        // Get SAM method from the expected functional interface
        MethodInfo sam = expectedType.getSingleAbstractMethod();
        if (sam != null && document != null) {
            // Find the InnerCallableScope for this lambda by matching position and parameters
            InnerCallableScope scope = findLambdaScopeByPosition(lambda.getStart(), lambda.getParameterNames());
            
            if (scope != null) {
                scope.setExpectedType(expectedType);
                
                // Extract and apply parameter types from SAM to lambda parameters
                List<FieldInfo> samParams = sam.getParameters();
                List<FieldInfo> lambdaParams = scope.getParameters();
                
                if (samParams.size() == lambdaParams.size()) {
                    for (int i = 0; i < lambdaParams.size(); i++) {
                        FieldInfo lambdaParam = lambdaParams.get(i);
                        TypeInfo inferredType = samParams.get(i).getTypeInfo();
                        
                        // Set inferred type on the parameter
                        if (inferredType != null) {
                            lambdaParam.setInferredType(inferredType);
                        }
                    }
                }
            }
            
            // Type check lambda body (if expression lambda)
            if (!lambda.isBlock() && lambda.getBody() != null) {
                // Resolve body type for validation
                TypeInfo bodyType = resolveNodeType(lambda.getBody());
                // The actual compatibility check will be done during marking phase
            }
            
            return expectedType;
        }
        
        // Fallback: check if it's a recognized functional interface by name
        String typeName = expectedType.getFullName();
        if (typeName != null && (typeName.contains("Function") || typeName.contains("Consumer") || 
                                 typeName.contains("Predicate") || typeName.contains("Supplier") ||
                                 typeName.equals("java.lang.Runnable"))) {
            // Update the scope reference if available
            if (lambda.getScopeRef() != null) {
                lambda.getScopeRef().setExpectedType(expectedType);
            }
            
            return expectedType;
        }
        
        // Not a recognized functional interface, return Object
        return TypeInfo.fromClass(Object.class);
    }
    
    /**
     * Find the InnerCallableScope for a lambda by matching position and parameter names.
     * 
     * @param expressionRelativePos The start position of the lambda (relative to expression string)
     * @param paramNames The parameter names of the lambda
     * @return The matching InnerCallableScope, or null if not found
     */
    private InnerCallableScope findLambdaScopeByPosition(int expressionRelativePos, List<String> paramNames) {
        if (document == null) {
            return null;
        }
        
        // Convert expression-relative position to absolute document position
        int absolutePos = basePosition + expressionRelativePos;
        
        for (InnerCallableScope scope : document.getInnerScopes()) {
            if (scope.getKind() == InnerCallableScope.Kind.JAVA_LAMBDA) {
                // Check if absolute position is in range
                if (absolutePos >= scope.getHeaderStart() && absolutePos < scope.getFullEnd()) {
                    // Verify parameter names match
                    if (scope.getParameters().size() == paramNames.size()) {
                        boolean match = true;
                        for (int i = 0; i < paramNames.size(); i++) {
                            if (!scope.getParameters().get(i).getName().equals(paramNames.get(i))) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            return scope;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private TypeInfo resolveJSFunctionType(ExpressionNode.JSFunctionNode functionNode) {
        // JS function type is determined by the expected functional interface (if any)
        TypeInfo expectedType = CURRENT_EXPECTED_TYPE;
        
        if (expectedType == null) {
            // No expected type - return generic Function type or Object
            // Try to resolve "Function" as a type from the context
            TypeInfo functionType = context.resolveTypeName("Function");
            if (functionType != null && functionType.isResolved()) {
                return functionType;
            }
            return TypeInfo.fromClass(Object.class); // Fallback
        }
        
        // Get SAM method from the expected functional interface
        MethodInfo sam = expectedType.getSingleAbstractMethod();
        if (sam != null && document != null) {
            // Find the InnerCallableScope for this JS function by matching position and parameters
            InnerCallableScope scope = findJSFunctionScopeByPosition(functionNode.getStart(), functionNode.getParameterNames());
            
            if (scope != null) {
                scope.setExpectedType(expectedType);
                
                // Extract and apply parameter types from SAM to function parameters
                List<FieldInfo> samParams = sam.getParameters();
                List<FieldInfo> functionParams = scope.getParameters();
                
                if (samParams.size() == functionParams.size()) {
                    for (int i = 0; i < functionParams.size(); i++) {
                        FieldInfo functionParam = functionParams.get(i);
                        TypeInfo inferredType = samParams.get(i).getTypeInfo();
                        
                        // Set inferred type on the parameter
                        if (inferredType != null) {
                            functionParam.setInferredType(inferredType);
                        }
                    }
                }
            }
            
            return expectedType;
        }
        
        // Fallback: check if expected type is compatible with a function
        String typeName = expectedType.getFullName();
        if (typeName != null && (typeName.contains("Function") || typeName.contains("Consumer") || 
                                 typeName.contains("Predicate") || typeName.contains("Supplier") ||
                                 typeName.equals("java.lang.Runnable"))) {
            // This is likely a functional interface
            
            // Update the InnerCallableScope with the expected type
            if (functionNode.getScopeRef() != null) {
                functionNode.getScopeRef().setExpectedType(expectedType);
            }
            
            return expectedType;
        }
        
        // Expected type is not a recognized functional interface
        // Check if it's a generic JS "Function" type expectation
        if (typeName != null && typeName.equals("Function")) {
            return expectedType;
        }
        
        return TypeInfo.fromClass(Object.class); // Fallback
    }
    
    /**
     * Find the InnerCallableScope for a JS function by matching position and parameter names.
     * 
     * @param expressionRelativePos The start position of the function (relative to expression string)
     * @param paramNames The parameter names of the function
     * @return The matching InnerCallableScope, or null if not found
     */
    private InnerCallableScope findJSFunctionScopeByPosition(int expressionRelativePos, List<String> paramNames) {
        if (document == null) {
            return null;
        }
        
        // Convert expression-relative position to absolute document position
        int absolutePos = basePosition + expressionRelativePos;
        
        for (InnerCallableScope scope : document.getInnerScopes()) {
            if (scope.getKind() == InnerCallableScope.Kind.JS_FUNCTION_EXPR) {
                // Check if absolute position is in range
                if (absolutePos >= scope.getHeaderStart() && absolutePos < scope.getFullEnd()) {
                    // Verify parameter names match
                    if (scope.getParameters().size() == paramNames.size()) {
                        boolean match = true;
                        for (int i = 0; i < paramNames.size(); i++) {
                            if (!scope.getParameters().get(i).getName().equals(paramNames.get(i))) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            return scope;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    // ==================== Method Reference Resolution ====================
    
    /**
     * Resolve the type of a method reference expression (target::methodName).
     * 
     * Supports all Java method reference forms:
     * - this::method       - Instance method on current object
     * - super::method      - Instance method on superclass
     * - variable::method   - Instance method on a variable's value
     * - ClassName::method  - Static method OR unbound instance method
     * - pkg.ClassName::method - Fully qualified class reference
     * - ClassName::new     - Constructor reference
     * - Type[]::new        - Array constructor reference
     * 
     * @param methodRef The method reference AST node
     * @return The resolved type (functional interface type if valid, Object if invalid)
     */
    private TypeInfo resolveMethodReferenceType(ExpressionNode.MethodReferenceNode methodRef) {
        TypeInfo expectedType = CURRENT_EXPECTED_TYPE;
        
        // Must have expected functional interface context
        if (expectedType == null || !expectedType.isFunctionalInterface()) {
            return TypeInfo.fromClass(Object.class);
        }
        
        MethodInfo sam = expectedType.getSingleAbstractMethod();
        if (sam == null) {
            return TypeInfo.fromClass(Object.class);
        }
        
        // Resolve target and determine if it's a class reference (static context)
        MethodReferenceTarget resolvedTarget = resolveMethodReferenceTarget(methodRef);
        if (resolvedTarget == null || resolvedTarget.type == null) {
            // Unresolved target - return expected type to allow forward references
            return expectedType;
        }
        
        String methodName = methodRef.getMethodName();
        
        // Handle constructor references (ClassName::new or Type[]::new)
        if ("new".equals(methodName)) {
            return resolveConstructorReference(resolvedTarget, sam, expectedType);
        }
        
        // Handle method references
        return resolveMethodReference(resolvedTarget, methodName, sam, expectedType);
    }
    
    /**
     * Encapsulates the resolved target of a method reference.
     */
    private static class MethodReferenceTarget {
        final TypeInfo type;           // The resolved type
        final boolean isClassRef;      // True if target is a class reference (for static access)
        
        MethodReferenceTarget(TypeInfo type, boolean isClassRef) {
            this.type = type;
            this.isClassRef = isClassRef;
        }
    }
    
    /**
     * Resolve the target (left side of ::) for a method reference.
     * Returns both the type and whether it's a class reference.
     */
    private MethodReferenceTarget resolveMethodReferenceTarget(ExpressionNode.MethodReferenceNode methodRef) {
        ExpressionNode target = methodRef.getTarget();
        if (target == null) {
            return null;
        }
        
        // Handle simple identifiers: this, super, variable, ClassName
        if (target instanceof ExpressionNode.IdentifierNode) {
            String name = ((ExpressionNode.IdentifierNode) target).getName();
            return resolveSimpleTarget(name);
        }
        
        // Handle qualified names: pkg.ClassName or outer.Inner
        if (target instanceof ExpressionNode.MemberAccessNode) {
            return resolveQualifiedTarget((ExpressionNode.MemberAccessNode) target);
        }
        
        // Handle array type targets: Type[]
        if (target instanceof ExpressionNode.ArrayAccessNode) {
            TypeInfo arrayType = resolveNodeType(target);
            if (arrayType != null && arrayType.isResolved()) {
                // Array type is always a class reference (for Type[]::new)
                return new MethodReferenceTarget(arrayType, true);
            }
        }
        
        // Fallback: resolve as expression (e.g., (expr)::method)
        TypeInfo exprType = resolveNodeType(target);
        if (exprType != null && exprType.isResolved()) {
            // Expression results are always instance references
            return new MethodReferenceTarget(exprType, false);
        }
        
        return null;
    }
    
    /**
     * Resolve a simple identifier as method reference target.
     */
    private MethodReferenceTarget resolveSimpleTarget(String name) {
        // this::method - instance method on current object
        if ("this".equals(name)) {
            TypeInfo thisType = context.resolveIdentifier("this");
            return thisType != null ? new MethodReferenceTarget(thisType, false) : null;
        }
        
        // super::method - instance method on superclass
        if ("super".equals(name)) {
            TypeInfo superType = context.resolveIdentifier("super");
            return superType != null ? new MethodReferenceTarget(superType, false) : null;
        }
        
        // Try as type name first (ClassName::method)
        TypeInfo typeInfo = context.resolveTypeName(name);
        if (typeInfo != null && typeInfo.isResolved()) {
            // Wrap as ClassTypeInfo to indicate class reference
            TypeInfo classRef = new ClassTypeInfo(typeInfo);
            return new MethodReferenceTarget(classRef, true);
        }
        
        // Try as variable (instance::method)
        TypeInfo varType = context.resolveIdentifier(name);
        if (varType != null && varType.isResolved()) {
            // Check if variable holds a Class reference (e.g., var File = Java.type("java.io.File"))
            boolean isClassRef = varType.isClassReference();
            return new MethodReferenceTarget(varType, isClassRef);
        }
        
        return null;
    }
    
    /**
     * Resolve a qualified name (a.b.c) as method reference target.
     * Handles both package-qualified class names and nested class access.
     */
    private MethodReferenceTarget resolveQualifiedTarget(ExpressionNode.MemberAccessNode memberAccess) {
        // Try to build qualified name and resolve as type
        String qualifiedName = buildQualifiedName(memberAccess);
        if (qualifiedName != null) {
            TypeInfo typeInfo = context.resolveTypeName(qualifiedName);
            if (typeInfo != null && typeInfo.isResolved()) {
                TypeInfo classRef = new ClassTypeInfo(typeInfo);
                return new MethodReferenceTarget(classRef, true);
            }
        }
        
        // Try resolving as expression chain (object.field::method)
        TypeInfo exprType = resolveNodeType(memberAccess);
        if (exprType != null && exprType.isResolved()) {
            boolean isClassRef = exprType.isClassReference();
            return new MethodReferenceTarget(exprType, isClassRef);
        }
        
        return null;
    }
    
    /**
     * Build a qualified name string from a MemberAccessNode chain.
     * Example: a.b.c.ClassName -> "a.b.c.ClassName"
     */
    private String buildQualifiedName(ExpressionNode node) {
        if (node instanceof ExpressionNode.IdentifierNode) {
            return ((ExpressionNode.IdentifierNode) node).getName();
        }
        
        if (node instanceof ExpressionNode.MemberAccessNode) {
            ExpressionNode.MemberAccessNode ma = (ExpressionNode.MemberAccessNode) node;
            String baseName = buildQualifiedName(ma.getTarget());
            if (baseName != null) {
                return baseName + "." + ma.getMemberName();
            }
        }
        
        return null;
    }
    
    /**
     * Resolve a method reference (target::methodName).
     */
    private TypeInfo resolveMethodReference(MethodReferenceTarget target, String methodName, 
                                            MethodInfo sam, TypeInfo expectedType) {
        TypeInfo targetType = target.isClassRef && target.type instanceof ClassTypeInfo
                ? ((ClassTypeInfo) target.type).getInstanceType()
                : target.type;
        
        if (targetType == null || !targetType.hasMethod(methodName)) {
            return TypeInfo.fromClass(Object.class);
        }
        
        // Find best matching method using OverloadSelector
        MethodInfo method = findBestMethodForReference(targetType, methodName, sam, target.isClassRef);
        if (method == null) {
            return TypeInfo.fromClass(Object.class);
        }
        
        // Validate signature compatibility
        String error = validateMethodSignature(method, sam, target.isClassRef, targetType);
        if (error != null) {
            return TypeInfo.fromClass(Object.class);
        }
        
        return expectedType;
    }
    
    /**
     * Find the best matching method for a method reference using OverloadSelector logic.
     */
    private MethodInfo findBestMethodForReference(TypeInfo targetType, String methodName, 
                                                   MethodInfo sam, boolean isClassRef) {
        List<MethodInfo> allOverloads = targetType.getAllMethodOverloads(methodName);
        if (allOverloads.isEmpty()) {
            return null;
        }
        
        // Extract SAM parameter types for overload matching
        TypeInfo[] samParamTypes = extractSamParamTypes(sam, isClassRef, targetType);
        
        // Filter candidates by arity first, then use OverloadSelector
        List<MethodInfo> candidates = new ArrayList<>();
        int expectedArity = samParamTypes.length;
        
        for (MethodInfo method : allOverloads) {
            int methodArity = method.getParameters().size();
            
            // Direct match: instance::method or ClassName::staticMethod
            if (methodArity == expectedArity) {
                candidates.add(method);
            }
            // Unbound instance method: ClassName::instanceMethod (first SAM param is receiver)
            else if (isClassRef && methodArity == sam.getParameters().size() - 1 && !isStaticMethod(method)) {
                candidates.add(method);
            }
        }
        
        if (candidates.isEmpty()) {
            return null;
        }
        
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        
        // Use OverloadSelector for multi-candidate selection
        return OverloadSelector.selectBestOverload(candidates, samParamTypes);
    }
    
    /**
     * Extract the effective parameter types from SAM for method matching.
     * For unbound instance methods, excludes the first receiver parameter.
     */
    private TypeInfo[] extractSamParamTypes(MethodInfo sam, boolean isClassRef, TypeInfo targetType) {
        List<FieldInfo> samParams = sam.getParameters();
        
        // For class references, we might be matching an unbound instance method
        // where the first SAM param is the receiver - handled in findBestMethodForReference
        TypeInfo[] types = new TypeInfo[samParams.size()];
        for (int i = 0; i < samParams.size(); i++) {
            types[i] = samParams.get(i).getTypeInfo();
        }
        return types;
    }
    
    /**
     * Check if a method is static (Java reflection).
     */
    private boolean isStaticMethod(MethodInfo method) {
        java.lang.reflect.Method javaMethod = method.getJavaMethod();
        if (javaMethod != null) {
            return java.lang.reflect.Modifier.isStatic(javaMethod.getModifiers());
        }
        // For script-defined methods, assume non-static unless marked
        return method.isStatic();
    }
    
    /**
     * Validate method signature compatibility with SAM.
     */
    private String validateMethodSignature(MethodInfo method, MethodInfo sam, 
                                           boolean isClassRef, TypeInfo targetType) {
        List<FieldInfo> methodParams = method.getParameters();
        List<FieldInfo> samParams = sam.getParameters();
        
        int methodArity = methodParams.size();
        int samArity = samParams.size();
        
        // Check for unbound instance method reference (ClassName::instanceMethod)
        boolean isUnbound = isClassRef && methodArity == samArity - 1 && !isStaticMethod(method);
        
        // Validate parameter count
        if (methodArity != samArity && !isUnbound) {
            return "Parameter count mismatch";
        }
        
        // Validate parameter types
        int offset = isUnbound ? 1 : 0;
        for (int i = 0; i < methodArity; i++) {
            TypeInfo methodParamType = methodParams.get(i).getTypeInfo();
            TypeInfo samParamType = samParams.get(i + offset).getTypeInfo();
            
            if (methodParamType != null && samParamType != null) {
                if (!TypeChecker.isTypeCompatible(methodParamType, samParamType)) {
                    return "Parameter type mismatch at position " + (i + 1);
                }
            }
        }
        
        // For unbound reference, validate receiver compatibility
        if (isUnbound && samArity > 0) {
            TypeInfo firstSamParam = samParams.get(0).getTypeInfo();
            if (firstSamParam != null && !TypeChecker.isTypeCompatible(targetType, firstSamParam)) {
                return "Receiver type mismatch";
            }
        }
        
        // Validate return type (covariant)
        TypeInfo methodReturn = method.getReturnType();
        TypeInfo samReturn = sam.getReturnType();
        
        if (samReturn != null && methodReturn != null) {
            boolean samIsVoid = "void".equals(samReturn.getFullName()) || samReturn.getJavaClass() == void.class;
            if (!samIsVoid && !TypeChecker.isTypeCompatible(samReturn, methodReturn)) {
                return "Return type mismatch";
            }
        }
        
        return null;
    }
    
    /**
     * Resolve a constructor reference (ClassName::new or Type[]::new).
     */
    private TypeInfo resolveConstructorReference(MethodReferenceTarget target, MethodInfo sam, 
                                                  TypeInfo expectedType) {
        TypeInfo targetType = target.type;
        
        // Handle array constructor reference: Type[]::new
        if (isArrayType(targetType)) {
            return resolveArrayConstructorReference(targetType, sam, expectedType);
        }
        
        // Get the actual class type for constructor lookup
        TypeInfo classType = target.type instanceof ClassTypeInfo 
                ? ((ClassTypeInfo) target.type).getInstanceType() 
                : target.type;
        
        if (classType == null) {
            return TypeInfo.fromClass(Object.class);
        }
        
        // Cannot construct interfaces or abstract classes
        if (classType.getKind() == TypeInfo.Kind.INTERFACE) {
            return TypeInfo.fromClass(Object.class);
        }
        
        // Extract SAM parameter types for constructor matching
        List<FieldInfo> samParams = sam.getParameters();
        TypeInfo[] paramTypes = new TypeInfo[samParams.size()];
        for (int i = 0; i < samParams.size(); i++) {
            paramTypes[i] = samParams.get(i).getTypeInfo();
        }
        
        // Find matching constructor
        MethodInfo constructor = classType.findConstructor(paramTypes);
        if (constructor == null && samParams.size() > 0) {
            // Try by arity if exact type match fails
            constructor = classType.findConstructor(samParams.size());
        }
        
        if (constructor == null) {
            return TypeInfo.fromClass(Object.class);
        }
        
        // Validate return type compatibility
        TypeInfo samReturn = sam.getReturnType();
        if (samReturn != null && !TypeChecker.isTypeCompatible(samReturn, classType)) {
            return TypeInfo.fromClass(Object.class);
        }
        
        return expectedType;
    }
    
    /**
     * Resolve an array constructor reference: Type[]::new
     * Must match IntFunction<Type[]> or similar single-int-param functional interface.
     */
    private TypeInfo resolveArrayConstructorReference(TypeInfo arrayType, MethodInfo sam, 
                                                       TypeInfo expectedType) {
        List<FieldInfo> samParams = sam.getParameters();
        
        // Array constructor takes exactly one int parameter (the size)
        if (samParams.size() != 1) {
            return TypeInfo.fromClass(Object.class);
        }
        
        TypeInfo sizeParam = samParams.get(0).getTypeInfo();
        if (sizeParam == null || !isIntLike(sizeParam)) {
            return TypeInfo.fromClass(Object.class);
        }
        
        // Return type must be compatible with array type
        TypeInfo samReturn = sam.getReturnType();
        if (samReturn != null && !TypeChecker.isTypeCompatible(samReturn, arrayType)) {
            return TypeInfo.fromClass(Object.class);
        }
        
        return expectedType;
    }
    
    /**
     * Check if a type is an array type.
     */
    private boolean isArrayType(TypeInfo type) {
        if (type == null) return false;
        String name = type.getFullName();
        return name != null && name.endsWith("[]");
    }
    
    /**
     * Check if a type is int-like (int, Integer, or numeric that can represent array size).
     */
    private boolean isIntLike(TypeInfo type) {
        if (type == null) return false;
        String name = type.getFullName();
        return "int".equals(name) || "java.lang.Integer".equals(name) 
            || "long".equals(name) || "java.lang.Long".equals(name);
    }
    
    public static ExpressionNode.TypeResolverContext createBasicContext() {
        return new ExpressionNode.TypeResolverContext() {
            public TypeInfo resolveIdentifier(String name) {
                if ("true".equals(name) || "false".equals(name)) return TypeInfo.fromPrimitive("boolean");
                if ("null".equals(name)) return TypeInfo.unresolved("null", "<null>");
                return null;
            }
            public TypeInfo resolveMemberAccess(TypeInfo targetType, String memberName) { return null; }
            public TypeInfo resolveMethodCall(TypeInfo targetType, String methodName, TypeInfo[] argTypes) { return null; }
            public TypeInfo resolveArrayAccess(TypeInfo arrayType) {
                String typeName = arrayType.getFullName();
                if (typeName.endsWith("[]")) {
                    // For primitive arrays, we need to map back to primitive TypeInfo
                    String elementType = typeName.substring(0, typeName.length() - 2);
                    switch (elementType) {
                        case "int": case "long": case "float": case "double": 
                        case "byte": case "short": case "char": case "boolean":
                            return TypeInfo.fromPrimitive(elementType);
                        default:
                            return TypeInfo.unresolved(elementType, elementType);
                    }
                }
                return null;
            }
            public TypeInfo resolveTypeName(String typeName) { 
                // Handle primitives
                switch (typeName) {
                    case "int": case "long": case "float": case "double": 
                    case "byte": case "short": case "char": case "boolean": case "void":
                        return TypeInfo.fromPrimitive(typeName);
                    default:
                        return TypeInfo.unresolved(typeName, typeName);
                }
            }
        };
    }
}
