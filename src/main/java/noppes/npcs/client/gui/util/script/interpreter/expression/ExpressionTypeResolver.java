package noppes.npcs.client.gui.util.script.interpreter.expression;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.InnerCallableScope;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
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
        
        if (node instanceof ExpressionNode.IntLiteralNode) return TypeInfo.fromPrimitive("int");
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
            return context.resolveMemberAccess(targetType, ma.getMemberName());
        }
        
        if (node instanceof ExpressionNode.MethodCallNode) {
            ExpressionNode.MethodCallNode mc = (ExpressionNode.MethodCallNode) node;
            TypeInfo targetType = mc.getTarget() == null ? context.resolveIdentifier("this") : resolveNodeType(mc.getTarget());
            if (targetType == null || !targetType.isResolved()) return null;
            TypeInfo[] argTypes = new TypeInfo[mc.getArguments().size()];
            for (int i = 0; i < argTypes.length; i++) argTypes[i] = resolveNodeType(mc.getArguments().get(i));
            return context.resolveMethodCall(targetType, mc.getMethodName(), argTypes);
        }
        
        if (node instanceof ExpressionNode.ArrayAccessNode) {
            ExpressionNode.ArrayAccessNode aa = (ExpressionNode.ArrayAccessNode) node;
            TypeInfo arrayType = resolveNodeType(aa.getArray());
            if (arrayType == null || !arrayType.isResolved()) return null;
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
    
    private TypeInfo resolveMethodReferenceType(ExpressionNode.MethodReferenceNode methodRef) {
        // Method reference type is determined by the expected functional interface
        TypeInfo expectedType = CURRENT_EXPECTED_TYPE;
        
        if (expectedType == null) {
            // No expected functional interface context
            return TypeInfo.fromClass(Object.class); // Fallback
        }
        
        // Check if expected type is a functional interface
        if (!isFunctionalInterface(expectedType)) {
            // Not a functional interface
            return TypeInfo.fromClass(Object.class);
        }
        
        // For now, we'll do basic validation and return the expected type
        // In a full implementation, we would:
        // 1. Resolve the target type (left side of ::)
        // 2. Find the referenced method on the target type
        // 3. Extract SAM signature from expected type
        // 4. Validate parameter/return type compatibility
        
        // Basic implementation: assume valid and return expected type
        return expectedType;
    }
    
    private boolean isFunctionalInterface(TypeInfo type) {
        // Check if the type has exactly one abstract method (SAM type)
        // Common functional interfaces: Runnable, Supplier, Consumer, Function, Predicate, etc.
        if (type == null) return false;
        
        String typeName = type.getFullName();
        if (typeName == null) return false;
        
        return typeName.equals("java.lang.Runnable") || 
               typeName.contains("Supplier") || 
               typeName.contains("Consumer") || 
               typeName.contains("Function") ||
               typeName.contains("Predicate") || 
               typeName.contains("BiFunction") ||
               typeName.contains("BiConsumer") || 
               typeName.contains("UnaryOperator") ||
               typeName.contains("BinaryOperator");
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
