package coacs.tools.junit;

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
 * @au
 * 
 */
public class TestCaseGenerator {

	private static final String CLASS_TYPE_DECIMAL = "(Double|Float|double|float)+.*";

	private static final String CLASS_TYPE_NUMERIC = "(lont|short|int|bye|Long|Short|Integer|Byte)+.*";

	private static final String TAB = "\t";

	Map<String, String> memberVariables = new HashMap<String, String>();

	StringBuilder testMethodGiven, testMethodReplay, testMethodWhen, testMethodThen, testMethodReturn;
	StringBuilder testClass = null;
	Map<String, Map<String, String>> testCases = new LinkedHashMap<String, Map<String, String>>();

	public static final String NL = "\r\n";

	public static final String srcDir = "C:/svn/server/rpc";// "src/test/resources";
	public static final String destnDir = "c:/temp/src/test/resources/temp";
	private static String javaSrcFile = "C:/svn/test.java";

	public static void main(final String[] args) throws Exception {
		final TestCaseGenerator testCaseGenerator = new TestCaseGenerator();
		// testCaseGenerator.generateTestCases();
		testCaseGenerator.visitAClassAtPath(javaSrcFile);
	}

	public Map<String, Map<String, String>> getTestCases() {
		return testCases;
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
			visitAClass(javaSrcFile);
		}
	}

	public void visitAClassAtPath(final String classFilePath) {
		try {
			visitAClass(new File(classFilePath));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void visitAClass(final File classFile) throws Exception {
		final String source = Helper.readFile(classFile);
		visitAClassSource(source);
	}

	public String visitAClassSource(final String source) throws Exception {
		testClass = new StringBuilder();
		testCases.clear();
		final CompilationUnit cu = Helper.parserString(source);
		cu.accept(new TestVisitor(source), null);

		// logi(memberVariables);
		final String classToTest = cu.getTypes().get(0).getName();
		testClass.append(getImports());
		testClass.append("public class ").append(classToTest + "Test{" + NL);
		final String testInstance = StringUtils.uncapitalise(classToTest).replace("Impl", "");
		testClass.append(NL + TAB + classToTest + " " + testInstance + ";" + NL);
		// dump generated methods
		for (final Iterator<Map.Entry<String, Map<String, String>>> it = testCases.entrySet().iterator(); it.hasNext();) {
			final Map.Entry<String, Map<String, String>> entry = it.next();
			final String key = entry.getKey();
			final Map<String, String> codeBlocks = testCases.get(key);
			final String given = codeBlocks.get("Given");
			final String replay = codeBlocks.get("Replay");
			final String when = codeBlocks.get("When");
			final String then = codeBlocks.get("Then");
			final String returnS = codeBlocks.get("Return");
			String replayStr = "";

			if (replay.length() > 0) {
				replayStr = TAB + String.format("EasyMock.replay(%s);", replay.substring(0, replay.lastIndexOf(","))) + NL;
			}
			testClass.append(NL + "@Test" + NL).append("public void test" + StringUtils.capitalise(key).substring(0, key.lastIndexOf("(")) + "(){" + NL).append(TAB + "//given" + NL).append(given).append(replayStr)
					.append(NL + TAB + "//when" + NL).append(when).append(NL + TAB + "" + testInstance + "." + key + ";" + NL).append(NL + TAB + "//then" + NL + NL).append(then).append("}");
		}
		// done
		testClass.append(NL + "}");
		logi("Result" + NL + testClass.toString());
		final String result = testClass.toString();
		FileUtils.writeStringToFile(new File(destnDir + "/" + classToTest + "Test.java"), result);
		return result;
	}

	private String getImports() {
		return "import static org.easymock.EasyMock.*;" + NL + "import static org.junit.Assert.*;" + NL + "import org.easymock.classextension.EasyMock;" + NL + "import org.junit.Assert;" + NL + "import org.junit.Test;"
				+ NL;
	}

	public void logi(final Object obj, final Object... params) {
		if (obj != null)
			System.out.println(String.format(obj.toString(), params));
	}

	void processCode(final String source, final Node node) {
		final String parsed = node.toString();
		if (node instanceof FieldDeclaration) {
			final FieldDeclaration field = (FieldDeclaration) node;
			final VariableDeclarator variableDeclarator = field.getVariables().get(0);
			final String type = field.getType().toString();
			memberVariables.put(variableDeclarator.getId().getName(), type);

		} else if (node instanceof MethodDeclaration) {
			final MethodDeclaration codeBlock = (MethodDeclaration) node;
			logi(getMethodName(codeBlock));
			final BlockStmt methodBody = codeBlock.getBody();
			testMethodGiven = new StringBuilder();
			testMethodWhen = new StringBuilder();
			testMethodThen = new StringBuilder(format(TAB+" //Assert.assertTrue(result!=null);"+NL));
			testMethodReplay = new StringBuilder();
			testMethodReturn = new StringBuilder();
			try {
				generateTestCaseForMethod(getMethodName(codeBlock), methodBody, TAB, methodBody);
			} catch (final Exception e) {
				e.printStackTrace();
				// continue
			}
			final Map<String, String> codeBlocks = new HashMap<String, String>();
			codeBlocks.put("Given", testMethodGiven.toString());
			codeBlocks.put("When", testMethodWhen.toString());
			codeBlocks.put("Then", testMethodThen.toString());
			codeBlocks.put("Replay", testMethodReplay.toString());
			codeBlocks.put("Return", testMethodReturn.toString());
			testCases.put(getMethodName(codeBlock), codeBlocks);

		} else {
			logi("Unknown %s", node.getClass());
		}

	}

	private String getMethodName(final MethodDeclaration codeBlock) {
		final List<Parameter> parameters = codeBlock.getParameters();
		String params = "";
		if (parameters != null) {
			for (final Parameter parameter : parameters) {
				params = params + parameter.getId().toString() + ",";
			}
			if (!StringUtils.isEmpty(params))
				params = params.substring(0, params.lastIndexOf(","));
		}
		return format("%s(%s)", codeBlock.getName(), params);
	}

	private String format(final String str, final Object... params) {
		return String.format(str, params);
	}

	public void generateTestCaseForMethod(final String methodName, final BlockStmt body, final String tab, final BlockStmt methodBody) {
		final List<Statement> stmts = body != null ? body.getStmts() : null;
		if (stmts == null || (methodName.matches("^(set).*") && memberVariables.containsKey(methodName.substring(methodName.indexOf("set"))))
				|| (methodName.matches("^(get).*") && memberVariables.containsKey(methodName.substring(methodName.indexOf("get"))))) {// ignore
																																		// setter
			return;
		}

		for (final Statement statement : stmts) {
			logi("%sProcessing  %s", tab, statement.getClass().getSimpleName());
			if (statement instanceof ExpressionStmt) {
				final ExpressionStmt expr = (ExpressionStmt) statement;
				final Expression expression = expr.getExpression();
				if (expression instanceof VariableDeclarationExpr) {
					final VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;
					final List<VariableDeclarator> vars = variableDeclarationExpr.getVars();
					final VariableDeclarator variableDeclarator = vars.get(0);

					String mockExpression = null;
					final String classType = variableDeclarationExpr.getType().toString();
					final String mockVariableName = variableDeclarator.getId().toString();

					final Expression initExpression = variableDeclarator.getInit();
					final String initializer = initExpression != null ? initExpression.toString() : "";
					if (StringUtils.isNotEmpty(initializer)) { // variable value
						if (initExpression instanceof BinaryExpr) {
							final BinaryExpr bexpr = (BinaryExpr) initExpression;
							final String testData = getTestDataForBinaryExpression(bexpr, tab, "");
							testMethodGiven.append(testData);

						} else if (initializer.contains("new ")) { // new object
							continue;
						} else if (classType.matches("(String|\")")) {
							mockExpression = format("\"test-%s\"", StringUtils.lowerCase(mockVariableName));
						} else if (initializer.contains(".")) {
							// api call. mock the API call
							if (initExpression instanceof MethodCallExpr) {
								final MethodCallExpr methodCaller = (MethodCallExpr) initExpression;
								final String methodInvokerObject = methodCaller.getScope().toString();
								final String invokerMethodName = methodCaller.getName();
								String initClassName = memberVariables.get(methodInvokerObject);
								if (initClassName == null) {
									initClassName = StringUtils.capitalize(methodInvokerObject);
								}
								mockExpression = format("EasyMock.createMock(%s.class)", initClassName);
								testMethodGiven.append(tab + initClassName).append(" ").append(methodInvokerObject).append(" = ").append(mockExpression + ";" + NL);
								// set expectation
								final List<Expression> args = methodCaller.getArgs();
								String mockArgs = "";
								if (args != null) {
									for (final Expression expression2 : args) {
										if (expression2 instanceof NameExpr) {
											final String param = guessParamClassType(expression2);
											mockArgs += format("EasyMock.isA(%s.class)", param);
										}
									}
									if (StringUtils.isNotBlank(mockArgs)) {
										mockArgs = mockArgs.substring(0, mockArgs.length() - 1);
										// TODO: set the return value
										testMethodGiven.append(format("%s %s %s = new %s();", TAB, classType, mockVariableName, classType)).append(";" + NL);
										testMethodGiven.append(format("%s EasyMock.expect(%s.%s(%s))", TAB, methodInvokerObject, invokerMethodName, mockArgs)).append(").andReturn(" + mockVariableName + ");" + NL);
									}
								} else {
									testMethodGiven.append(TAB + String.format("%s.%s();", methodInvokerObject, invokerMethodName)).append(NL);
								}

								testMethodReplay.append(methodInvokerObject).append(",");
								continue;
							}
						} else if (classType.matches(CLASS_TYPE_NUMERIC)) {
							mockExpression = "1";
						} else if (classType.matches(CLASS_TYPE_DECIMAL)) {
							mockExpression = "1.0f";
						} else if (classType.endsWith("Dto") || classType.matches("StringBuilder|StringBuffer")) {
							mockExpression = " new  " + classType + "()";
						} else if (classType.matches("(final)+.*")) {
							mockExpression = "";
						} else if (classType.matches("(List|ArrayList)+.*")) {
							mockExpression = " new  ArrayList()";
						} else if (classType.matches("(Set|HashSet)+.*")) {
							mockExpression = " new  HashSet()";
						} else if (classType.matches("(Map|HashMap)+.*")) {
							mockExpression = " new  HashMap()";
						} else if ("String".equals(classType)) {
							mockExpression = " \"test" + StringUtils.capitalize(mockVariableName) + "\"";
						} else if ("boolean".equals(classType)) {
							mockExpression = " false";

						} else {
							mockExpression = format(" createMock(%s.class)", classType);
						}
					}

					testMethodGiven.append(tab + classType).append(" ").append(mockVariableName).append(" = ").append(mockExpression + ";\r\n");
				}
				logi(tab + " ExpressionStmt : " + expression.getClass() + " " + expression);
			} else if (statement instanceof TryStmt) {
				final TryStmt tryBlock = (TryStmt) statement;
				final BlockStmt tryBlockStmt = tryBlock.getTryBlock();
				generateTestCaseForMethod(methodName, tryBlockStmt, tab + TAB, methodBody);
			} else if (statement instanceof ForeachStmt) {
				final ForeachStmt forEach = (ForeachStmt) statement;
				generateTestCaseForMethod(methodName, (BlockStmt) forEach.getBody(), tab + TAB, methodBody);
			} else if (statement instanceof WhileStmt) {
				final WhileStmt whileStmt = (WhileStmt) statement;
				generateTestCaseForMethod(methodName, (BlockStmt) whileStmt.getBody(), tab + TAB, methodBody);
			} else if (statement instanceof ReturnStmt) {
				final ReturnStmt returnStmt = (ReturnStmt) statement;
				testMethodReturn.append(StringUtils.substringBetween(returnStmt.toString(), "return", ";")).append(" = ");
				final Expression expr = returnStmt.getExpr();
				if (expr instanceof BinaryExpr) {
					final BinaryExpr bexpr = (BinaryExpr) expr;
				}
				logi(tab + "return %s %s", expr, expr.getClass());
			} else {
				logi(TAB + statement.getClass() + " : " + statement);
			}
			logi("%sFinished %s", tab, statement.getClass().getSimpleName());
		}

	}

	private String getTestDataForBinaryExpression(final BinaryExpr bexpr, final String tab, final String code) {
		final Expression left = bexpr.getLeft();
		final Expression right = bexpr.getRight();
		String result = code;
		if (left instanceof NameExpr) {
			result = result + format("%s int %s = 1;%s", tab, left, NL);
		} else if (left instanceof BinaryExpr) {
			return getTestDataForBinaryExpression((BinaryExpr) left, tab, result);
		}
		if (right instanceof NameExpr) {
			result = result + format("%s int %s = 1;%s", tab, right, NL);
		} else if (right instanceof BinaryExpr) {
			return getTestDataForBinaryExpression((BinaryExpr) right, tab, result);
		}

		return result;
	}

	// TODO: set the type of the argument
	public String guessParamClassType(final Expression expression2) {
		final NameExpr nameExpr = (NameExpr) expression2;
		String param = nameExpr.toString();
		param = param.endsWith("Id") ? "Integer" : param;
		param = param.endsWith("List") ? "List" : param;
		param = param.endsWith("Map") ? "Map" : param;
		return param;
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
