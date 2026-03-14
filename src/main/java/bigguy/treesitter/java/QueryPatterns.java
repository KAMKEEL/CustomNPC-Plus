package bigguy.treesitter.java;

/**
 * Tree-sitter S-expression query patterns ported from Zed's Java extension.
 *
 * <p>Each constant contains a complete {@code .scm} query that can be compiled
 * via {@link JavaGrammar#compileQuery(String)} and executed against a parsed
 * tree. The queries are direct Java-string equivalents of Zed's
 * {@code languages/java/*.scm} files (v6.8.12).</p>
 *
 * <p>Queries can be combined by string concatenation to compose complex
 * patterns from simple building blocks.</p>
 */
public final class QueryPatterns {

    private QueryPatterns() {}

    // ─── HIGHLIGHTS (from highlights.scm, 3912B) ──────────────────────

    public static final String HIGHLIGHTS =
        // Identifiers: methods, constructors, types
        "(method_declaration name: (identifier) @function)\n" +
        "(method_invocation name: (identifier) @function)\n" +
        "(super) @function.builtin\n" +
        "\n" +
        "(type_identifier) @type\n" +
        "(class_declaration name: (identifier) @type)\n" +
        "(interface_declaration name: (identifier) @type)\n" +
        "(record_declaration name: (identifier) @type)\n" +
        "(enum_declaration name: (identifier) @enum)\n" +
        "(enum_constant name: (identifier) @constant)\n" +
        "(constructor_declaration name: (identifier) @constructor)\n" +
        "\n" +
        // Field access
        "(field_access field: (identifier) @property)\n" +
        "(field_declaration declarator: (variable_declarator name: (identifier) @property))\n" +
        "\n" +
        // Literals
        "(string_literal) @string\n" +
        "(string_fragment) @string\n" +
        "(multiline_string_fragment) @string\n" +
        "(character_literal) @string\n" +
        "(escape_sequence) @string.escape\n" +
        "[(decimal_integer_literal) (hex_integer_literal) (octal_integer_literal) (binary_integer_literal)] @number\n" +
        "[(decimal_floating_point_literal) (hex_floating_point_literal)] @number\n" +
        "(true) @boolean\n" +
        "(false) @boolean\n" +
        "(null_literal) @constant.builtin\n" +
        "\n" +
        // Annotations
        "(annotation name: (identifier) @attribute)\n" +
        "(marker_annotation name: (identifier) @attribute)\n" +
        "\n" +
        // Comments
        "(line_comment) @comment\n" +
        "(block_comment) @comment\n" +
        "\n" +
        // Keywords
        "[\n" +
        "  \"abstract\" \"assert\" \"break\" \"case\" \"catch\"\n" +
        "  \"class\" \"continue\" \"default\" \"do\" \"else\"\n" +
        "  \"enum\" \"extends\" \"final\" \"finally\" \"for\"\n" +
        "  \"if\" \"implements\" \"import\" \"instanceof\" \"interface\"\n" +
        "  \"module\" \"native\" \"new\" \"package\" \"private\"\n" +
        "  \"protected\" \"public\" \"record\" \"return\" \"static\"\n" +
        "  \"strictfp\" \"switch\" \"synchronized\"\n" +
        "  \"throw\" \"throws\" \"transient\" \"try\"\n" +
        "  \"volatile\" \"while\" \"yield\"\n" +
        "] @keyword\n" +
        "\n" +
        // Operators
        "[\n" +
        "  \"=\" \">\" \"<\" \"!\" \"~\" \"?\" \":\" \"->\" \"==\" \">=\" \"<=\"\n" +
        "  \"!=\" \"&&\" \"||\" \"++\" \"--\" \"+\" \"-\" \"*\" \"/\" \"&\"\n" +
        "  \"|\" \"^\" \"%\" \"<<\" \">>\" \">>>\" \"+=\" \"-=\" \"*=\" \"/=\"\n" +
        "  \"&=\" \"|=\" \"^=\" \"%=\" \"<<=\" \">>=\" \">>>=\" \"...\"\n" +
        "] @operator\n" +
        "\n" +
        // Punctuation
        "[\"(\" \")\" ] @punctuation.bracket\n" +
        "[\"[\" \"]\" ] @punctuation.bracket\n" +
        "[\"{\" \"}\" ] @punctuation.bracket\n" +
        "[\".\" \";\" \",\"] @punctuation.delimiter\n" +
        "\n" +
        // Constants: uppercase identifiers (SCREAMING_CASE)
        "((identifier) @constant\n" +
        "  (#match? @constant \"^[A-Z_$][A-Z\\\\d_$]*$\"))\n";

    // ─── OUTLINE (from outline.scm, 4286B) ────────────────────────────

    public static final String OUTLINE =
        // Classes
        "(class_declaration\n" +
        "  (modifiers)? @context\n" +
        "  name: (identifier) @name) @item\n" +
        "\n" +
        "(interface_declaration\n" +
        "  (modifiers)? @context\n" +
        "  name: (identifier) @name) @item\n" +
        "\n" +
        "(enum_declaration\n" +
        "  (modifiers)? @context\n" +
        "  name: (identifier) @name) @item\n" +
        "\n" +
        "(record_declaration\n" +
        "  (modifiers)? @context\n" +
        "  name: (identifier) @name) @item\n" +
        "\n" +
        "(annotation_type_declaration\n" +
        "  (modifiers)? @context\n" +
        "  name: (identifier) @name) @item\n" +
        "\n" +
        // Methods
        "(method_declaration\n" +
        "  (modifiers)? @context\n" +
        "  type: (_) @context\n" +
        "  name: (identifier) @name\n" +
        "  parameters: (formal_parameters\n" +
        "    (formal_parameter type: (_) @context)?)) @item\n" +
        "\n" +
        // Constructors
        "(constructor_declaration\n" +
        "  (modifiers)? @context\n" +
        "  name: (identifier) @name\n" +
        "  parameters: (formal_parameters\n" +
        "    (formal_parameter type: (_) @context)?)) @item\n" +
        "\n" +
        // Fields
        "(field_declaration\n" +
        "  (modifiers)? @context\n" +
        "  type: (_) @context\n" +
        "  declarator: (variable_declarator\n" +
        "    name: (identifier) @name)) @item\n" +
        "\n" +
        // Enum constants
        "(enum_constant\n" +
        "  name: (identifier) @name) @item\n" +
        "\n" +
        // Static initializers
        "(static_initializer \"static\") @item @name\n";

    // ─── RUNNABLES (from runnables.scm, 4807B) ────────────────────────

    public static final String RUNNABLES =
        // Main method detection
        "(class_declaration\n" +
        "  name: (identifier) @java_class_name\n" +
        "  body: (class_body\n" +
        "    (method_declaration\n" +
        "      (modifiers \"public\" \"static\")\n" +
        "      type: (void_type)\n" +
        "      name: (identifier) @_main_name\n" +
        "      parameters: (formal_parameters\n" +
        "        (formal_parameter\n" +
        "          type: (array_type\n" +
        "            element: (type_identifier))))) @run)\n" +
        "  (#eq? @_main_name \"main\")\n" +
        "  (#set! tag \"java-main\"))\n" +
        "\n" +
        // @Test method
        "(class_declaration\n" +
        "  name: (identifier) @java_class_name\n" +
        "  body: (class_body\n" +
        "    (method_declaration\n" +
        "      (modifiers\n" +
        "        (marker_annotation\n" +
        "          name: (identifier) @_test_annotation))\n" +
        "      name: (identifier) @java_method_name) @run)\n" +
        "  (#any-of? @_test_annotation \"Test\" \"ParameterizedTest\" \"RepeatedTest\")\n" +
        "  (#set! tag \"java-test-method\"))\n" +
        "\n" +
        // @Test class
        "(class_declaration\n" +
        "  name: (identifier) @java_class_name\n" +
        "  body: (class_body\n" +
        "    (method_declaration\n" +
        "      (modifiers\n" +
        "        (marker_annotation\n" +
        "          name: (identifier) @_test_annotation))))\n" +
        "  (#any-of? @_test_annotation \"Test\" \"ParameterizedTest\" \"RepeatedTest\")\n" +
        "  (#set! tag \"java-test-class\")) @run\n" +
        "\n" +
        // Nested @Test method
        "(class_declaration\n" +
        "  name: (identifier) @java_class_name\n" +
        "  body: (class_body\n" +
        "    (class_declaration\n" +
        "      (modifiers\n" +
        "        (marker_annotation\n" +
        "          name: (identifier) @_nested_annotation))\n" +
        "      name: (identifier) @_nested_class_name\n" +
        "      body: (class_body\n" +
        "        (method_declaration\n" +
        "          (modifiers\n" +
        "            (marker_annotation\n" +
        "              name: (identifier) @_test_annotation))\n" +
        "          name: (identifier) @java_method_name) @run)))\n" +
        "  (#eq? @_nested_annotation \"Nested\")\n" +
        "  (#any-of? @_test_annotation \"Test\" \"ParameterizedTest\" \"RepeatedTest\")\n" +
        "  (#set! tag \"java-test-method-nested\"))\n" +
        "\n" +
        // Nested @Test class
        "(class_declaration\n" +
        "  name: (identifier) @java_class_name\n" +
        "  body: (class_body\n" +
        "    (class_declaration\n" +
        "      (modifiers\n" +
        "        (marker_annotation\n" +
        "          name: (identifier) @_nested_annotation))\n" +
        "      name: (identifier) @_nested_class_name\n" +
        "      body: (class_body\n" +
        "        (method_declaration\n" +
        "          (modifiers\n" +
        "            (marker_annotation\n" +
        "              name: (identifier) @_test_annotation))))))\n" +
        "  (#eq? @_nested_annotation \"Nested\")\n" +
        "  (#any-of? @_test_annotation \"Test\" \"ParameterizedTest\" \"RepeatedTest\")\n" +
        "  (#set! tag \"java-test-class-nested\")) @run\n" +
        "\n" +
        // Package declaration (for FQN resolution)
        "(package_declaration\n" +
        "  (scoped_identifier) @java_package_name)\n";

    // ─── LOCALS (from locals.scm, 2222B) ──────────────────────────────

    public static final String LOCALS =
        "(program) @local.scope\n" +
        "(class_declaration body: (class_body) @local.scope)\n" +
        "(record_declaration body: (class_body) @local.scope)\n" +
        "(interface_declaration body: (interface_body) @local.scope)\n" +
        "(enum_declaration body: (enum_body) @local.scope)\n" +
        "(block) @local.scope\n" +
        "(if_statement) @local.scope\n" +
        "(for_statement) @local.scope\n" +
        "(enhanced_for_statement) @local.scope\n" +
        "(while_statement) @local.scope\n" +
        "(do_statement) @local.scope\n" +
        "(try_statement) @local.scope\n" +
        "(catch_clause) @local.scope\n" +
        "(lambda_expression) @local.scope\n" +
        "(method_declaration) @local.scope\n" +
        "(constructor_declaration) @local.scope\n" +
        "\n" +
        // Definitions
        "(package_declaration\n" +
        "  (scoped_identifier) @local.definition.namespace)\n" +
        "(class_declaration\n" +
        "  name: (identifier) @local.definition.type)\n" +
        "(record_declaration\n" +
        "  name: (identifier) @local.definition.type)\n" +
        "(interface_declaration\n" +
        "  name: (identifier) @local.definition.type)\n" +
        "(enum_declaration\n" +
        "  name: (identifier) @local.definition.enum)\n" +
        "(method_declaration\n" +
        "  name: (identifier) @local.definition.method)\n" +
        "(constructor_declaration\n" +
        "  name: (identifier) @local.definition.method)\n" +
        "(local_variable_declaration\n" +
        "  declarator: (variable_declarator\n" +
        "    name: (identifier) @local.definition.var))\n" +
        "(program\n" +
        "  (local_variable_declaration\n" +
        "    declarator: (variable_declarator)\n" +
        "      name: (identifier) @variable)))\n" +
        "(formal_parameter\n" +
        "  name: (identifier) @local.definition.parameter)\n" +
        "(field_declaration\n" +
        "  declarator: (variable_declarator\n" +
        "    name: (identifier) @local.definition.field))\n" +
        "(import_declaration\n" +
        "  (scoped_identifier) @local.definition.import)\n" +
        "\n" +
        // References
        "(identifier) @local.reference\n" +
        "(type_identifier) @local.reference\n";

    // ─── FOLDS (from folds.scm, 110B) ─────────────────────────────────

    public static final String FOLDS =
        "[(block)\n" +
        "  (class_body)\n" +
        "  (constructor_declaration)\n" +
        "  (argument_list)\n" +
        "  (annotation_argument_list)] @fold\n";

    // ─── BRACKETS (from brackets.scm, 150B) ───────────────────────────

    public static final String BRACKETS =
        "(\"(\" @open \")\" @close)\n" +
        "(\"[\" @open \"]\" @close)\n" +
        "(\"{\" @open \"}\" @close)\n" +
        "(\"<\" @open \">\" @close)\n" +
        "(\"\\\"\" @open \"\\\"\" @close)\n";

    // ─── INDENTS (from indents.scm, 75B) ──────────────────────────────

    public static final String INDENTS =
        "(_ \"{\" \"}\" @end) @indent\n" +
        "(_ \"[\" \"]\" @end) @indent\n" +
        "(_ \"(\" \")\" @end) @indent\n";

    // ─── TEXT OBJECTS (from textobjects.scm, 1167B) ───────────────────

    public static final String TEXT_OBJECTS =
        "(method_declaration) @function.around\n" +
        "(method_declaration body: (block) @function.inside)\n" +
        "(constructor_declaration) @function.around\n" +
        "(constructor_declaration body: (constructor_body) @function.inside)\n" +
        "(lambda_expression) @function.around\n" +
        "(lambda_expression body: (_) @function.inside)\n" +
        "\n" +
        "(class_declaration) @class.around\n" +
        "(class_declaration body: (class_body) @class.inside)\n" +
        "(interface_declaration) @class.around\n" +
        "(interface_declaration body: (interface_body) @class.inside)\n" +
        "(enum_declaration) @class.around\n" +
        "(enum_declaration body: (enum_body) @class.inside)\n" +
        "(record_declaration) @class.around\n" +
        "(record_declaration body: (class_body) @class.inside)\n" +
        "(annotation_type_declaration) @class.around\n" +
        "(annotation_type_declaration body: (annotation_type_body) @class.inside)\n" +
        "\n" +
        "(line_comment)+ @comment.around\n" +
        "(block_comment) @comment.around\n";

    // ─── INJECTIONS (from injections.scm, 663B) ──────────────────────

    public static final String INJECTIONS =
        "((line_comment) @content\n" +
        "  (#set! \"language\" \"comment\"))\n" +
        "((block_comment) @content\n" +
        "  (#match? @content \"^/\\\\*[^*]\")\n" +
        "  (#set! \"language\" \"comment\"))\n" +
        "((block_comment) @content\n" +
        "  (#match? @content \"^/\\\\*\\\\*\")\n" +
        "  (#set! \"language\" \"doxygen\"))\n";

    // ─── OVERRIDES (from overrides.scm, 112B) ─────────────────────────

    public static final String OVERRIDES =
        "[(block_comment) (line_comment)] @comment.inclusive\n" +
        "[(character_literal) (string_literal)] @string\n";
}
