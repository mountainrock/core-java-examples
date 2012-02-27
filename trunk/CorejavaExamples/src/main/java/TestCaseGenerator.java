package project.tools.junit;

import japa.parser.ast.BlockComment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.LineComment;
import japa.parser.ast.Node;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.AnnotationDeclaration;
import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.EmptyMemberDeclaration;
import japa.parser.ast.body.EmptyTypeDeclaration;
import japa.parser.ast.body.EnumConstantDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.InitializerDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.InstanceOfExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralMinValueExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.LongLiteralMinValueExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.AssertStmt;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.LabeledStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.SynchronizedStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.stmt.TypeDeclarationStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.type.WildcardType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Uses javaparser (japa) to generate testcases with easy mock methods.
 * 
 * @author W353322
 * 
 */
public class TestCaseGenerator {

	private static final String TAB = "\t";

	Map<String, String> memberVariables = new HashMap<String, String>();

	StringBuilder testMethod;
	StringBuilder testClass = null;
	Map<String, String> testCases = new LinkedHashMap<String, String>();

	public static final String NL = "\r\n";

	public static final String srcDir = "C:/svn/im-project-mo/mo-project/branches/mo-project-1.0/mo-project-client/src/main/java/com/jpmc/am/mo/project/web/server/rpc";//"src/test/resources";
	public static final String destnDir = "src/test/resources/temp";

	public static void main(final String[] args) throws Exception {
		new TestCaseGenerator().generateTestCases();
	}
	
	public void generateTestCases() throws Exception {
		final File srcDirObj = new File(srcDir);
		final File destnDirObj = new File(destnDir);
		if (!destnDirObj.exists()) {
			destnDirObj.mkdirs();
		}

		final File[] javaSrcFiles = srcDirObj.listFiles(new FileFilter() {

			@Override
			public boolean accept(final File file) {
				final String path = file.getName();
				logi("Reading " + path);
				return file.isFile() && path.endsWith(".java");
			}
		});

		// time to do it
		for (final File javaSrcFile : javaSrcFiles) {
			logi("Processing : " + javaSrcFile);
			testClass = new StringBuilder(); 
			testCases.clear();
			visitAClass(javaSrcFile);
		}
	}

	public void visitAClass(final File classFile) throws Exception {

		final String source = Helper.readFile(classFile);
		final CompilationUnit cu = Helper.parserString(source);
		cu.accept(new TestVisitor(source), null);

		// logi(memberVariables);
		final String classToTest = cu.getTypes().get(0).getName();
		testClass.append(getImports());
		testClass.append("public class ").append(classToTest + "Test{" + NL);
		final String testInstance = StringUtils.uncapitalise(classToTest);
		testClass.append(NL + TAB + classToTest + " " + testInstance + ";" + NL);
		// dump generated methods
		for (final Iterator<Map.Entry<String, String>> it = testCases.entrySet().iterator(); it.hasNext();) {
			final Map.Entry<String, String> entry = it.next();
			final String key = entry.getKey();
			final String value = entry.getValue();
			if (StringUtils.isEmpty(value)) {
				continue;
			}
			testClass.append(NL+"@Test"+NL).append("public void test" + StringUtils.capitalise(key).substring(0, key.lastIndexOf("(")) + "(){" + NL).append(TAB + "//given" + NL).append(value).append(NL + TAB + "//when" + NL)
					.append(NL + TAB + "//" + testInstance + "." + key + ";" + NL).append(NL + TAB + "//then" + NL + NL).append("}");
		}
		// done
		testClass.append(NL + "}");
		logi("Result" + NL+ testClass.toString());
		FileUtils.writeStringToFile(new File(destnDir + "/" + classToTest + "Test.java"), testClass.toString());
	}

	private String getImports() {
		return "import static org.easymock.EasyMock.*;" + NL + "import static org.junit.Assert.*;" + NL + "import org.easymock.classextension.EasyMock;" + NL + "import org.junit.Assert;" + NL + "import org.junit.Test;"
				+ NL;
	}

	public void logi(final Object obj) {
		if (obj != null)
			System.out.println(obj.toString());
	}

	void processCode(final String source, final Node node) {
		final String parsed = node.toString();
		if (node instanceof FieldDeclaration) {
			final FieldDeclaration field = (FieldDeclaration) node;
			final VariableDeclarator variableDeclarator = field.getVariables().get(0);
			final String type = field.getType().toString();
			memberVariables.put(variableDeclarator.getId().getName(), type);
		}

		if (node instanceof MethodDeclaration) {
			final MethodDeclaration codeBlock = (MethodDeclaration) node;
			logi(getMethodName(codeBlock));
			final BlockStmt methodBody = codeBlock.getBody();
			testMethod = new StringBuilder();
			generateTestCaseForMethod(getMethodName(codeBlock), methodBody, TAB);
			testCases.put(getMethodName(codeBlock), testMethod.toString());
		}

	}

	private String getMethodName(final MethodDeclaration codeBlock) {
		final List<Parameter> parameters = codeBlock.getParameters();
		;
		String params = "";
		if (parameters !=null) {
			for (final Parameter parameter : parameters) {
				params = params + parameter.getId().toString() + ",";
			}
			if (!StringUtils.isEmpty(params))
				params = params.substring(0, params.lastIndexOf(","));
		}
		return codeBlock.getName() + "(" + params + ")";
	}

	public void generateTestCaseForMethod(final String methodName, final BlockStmt body, final String tab) {
		final List<Statement> stmts = body!=null ? body.getStmts() : null;
		if (stmts == null || (methodName.matches("^(set).*") && memberVariables.containsKey(methodName.substring(methodName.indexOf("set"))))
				|| (methodName.matches("^(get).*") && memberVariables.containsKey(methodName.substring(methodName.indexOf("get"))))) {// ignore
																																		// setter
			return;
		}
		for (final Statement statement : stmts) {
			if (statement instanceof ExpressionStmt) {
				final ExpressionStmt expr = (ExpressionStmt) statement;
				final Expression expression = expr.getExpression();
				if (expression instanceof VariableDeclarationExpr) {
					final VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;
					final List<VariableDeclarator> vars = variableDeclarationExpr.getVars();
					final VariableDeclarator variableDeclarator = vars.get(0);
					final String classType = variableDeclarationExpr.getType().toString();
					final String mockName = variableDeclarator.getId().toString();

					String mockExpression;
					if (classType.endsWith("Dto") || classType.matches("StringBuilder|StringBuffer")) {
						mockExpression = " = new  " + classType + "();";
					} else if (classType.matches("(final)+.*")) {
						mockExpression="";
					} else if (classType.matches("(List|ArrayList)+.*")) {
						mockExpression = " = new  ArrayList();";
					} else if (classType.matches("(Set|HashSet)+.*")) {
						mockExpression = " = new  HashSet();";
					} else if (classType.matches("(Map|HashMap)+.*")) {
						mockExpression = " = new  HashMap();";
					} else if ("String".equals(classType)) {
						mockExpression = " = \"test" + StringUtils.capitalize(mockName) + "\";";
					} else if ("Integer".equals(classType)) {
						mockExpression = "= 1;";
					} else if ("boolean".equals(classType)) {
						mockExpression = "= false;";

					} else {
						mockExpression = " = createMock(" + classType + ".class);";
					}
					testMethod.append(tab + classType).append(" ").append(mockName).append(mockExpression + "\r\n");
				}
				logi(tab + " ExpressionStmt : " + expression.getClass() + " " + expression);
			} else if (statement instanceof TryStmt) {
				final TryStmt tryBlock = (TryStmt) statement;
				final BlockStmt tryBlockStmt = tryBlock.getTryBlock();
				logi(tab + " TRY{");
				generateTestCaseForMethod(methodName, tryBlockStmt, tab + TAB);
				logi(tab + " /TRY }");
			} else if (statement instanceof ForeachStmt) {
				final ForeachStmt forEach = (ForeachStmt) statement;
				logi(tab + " FOR{" + forEach.getVariable());
				generateTestCaseForMethod(methodName, (BlockStmt) forEach.getBody(), tab + TAB);
				logi(tab + " /FOR}");
			} else if (statement instanceof WhileStmt) {
				final WhileStmt whileStmt = (WhileStmt) statement;
				logi(tab + " WHILE{" + whileStmt.getCondition());
				generateTestCaseForMethod(methodName, (BlockStmt) whileStmt.getBody(), tab + TAB);
				logi(tab + " /WHILE}");
			} else {
				logi(TAB + statement.getClass() + " : " + statement);
			}
		}
	}

	class TestVisitor extends VoidVisitorAdapter<Object> {

		private final String source;

		public TestVisitor(final String source) {
			this.source = source;
		}

		@Override
		public void visit(final AnnotationDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final AnnotationMemberDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ArrayAccessExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ArrayCreationExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ArrayInitializerExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final AssertStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final AssignExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final BinaryExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final BlockComment n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final BlockStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final BooleanLiteralExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final BreakStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final CastExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final CatchClause n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final CharLiteralExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ClassExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ClassOrInterfaceDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ClassOrInterfaceType n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final CompilationUnit n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ConditionalExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ConstructorDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ContinueStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final DoStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final DoubleLiteralExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final EmptyMemberDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final EmptyStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final EmptyTypeDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final EnclosedExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final EnumConstantDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final EnumDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ExplicitConstructorInvocationStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ExpressionStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final FieldAccessExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final FieldDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ForeachStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ForStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final IfStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ImportDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final InitializerDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final InstanceOfExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final IntegerLiteralExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final IntegerLiteralMinValueExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final JavadocComment n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final LabeledStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final LineComment n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final LongLiteralExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final LongLiteralMinValueExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final MarkerAnnotationExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final MemberValuePair n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final MethodCallExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final MethodDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final NameExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final NormalAnnotationExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final NullLiteralExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ObjectCreationExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final PackageDeclaration n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final Parameter n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final PrimitiveType n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final QualifiedNameExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ReferenceType n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ReturnStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final SingleMemberAnnotationExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final StringLiteralExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final SuperExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final SwitchEntryStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final SwitchStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final SynchronizedStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ThisExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final ThrowStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final TryStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final TypeDeclarationStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final TypeParameter n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final UnaryExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final VariableDeclarationExpr n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final VariableDeclarator n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final VariableDeclaratorId n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final VoidType n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final WhileStmt n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

		@Override
		public void visit(final WildcardType n, final Object arg) {
			processCode(source, n);
			super.visit(n, arg);
		}

	}

}
