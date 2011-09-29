package net.thucydides.junit.spring;

import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.springframework.test.context.TestContextManager;

/**
 * Use the Spring test annotations in Thucydides tests.
 *
 * @author johnsmart
 */
public class SpringIntegration extends TestWatchman {

    private final Class<?> clazz;

    private TestContextManager testContextManager;

    private SpringIntegration(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static SpringIntegration forClass(Class<?> testClass) {
        return new SpringIntegration(testClass);
    }

    @Override
    public void starting(FrameworkMethod method) {
        super.starting(method);
    }

    @Override
    public void finished(FrameworkMethod method) {
        super.finished(method);
    }

    public Statement apply(Statement base, FrameworkMethod method, Object testInstance) {
        TestContextManager contextManager = getTestContextManager(method.getMethod().getDeclaringClass());
        try {
            contextManager.prepareTestInstance(testInstance);
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate test instance", e);
        }

        Statement statement = new SpringContextStatement(base);
        statement = withBefores(method, testInstance, statement, contextManager);
        statement = withAfters(method, testInstance, statement, contextManager);

        return statement;
    }

    final class SpringContextStatement extends Statement {

        final Statement base;

        SpringContextStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            base.evaluate();
        }
    }

    protected TestContextManager getTestContextManager(Class<?> clazz) {
        if (testContextManager == null) {
            testContextManager = createTestContextManager(clazz);
        }
        return testContextManager;
    }

    /**
     * Creates a new {@link TestContextManager}. Can be overridden by subclasses.
     *
     * @param clazz the Class object corresponding to the test class to be managed
     */
    protected TestContextManager createTestContextManager(Class<?> clazz) {
        return new TestContextManager(clazz);
    }

	protected Statement withBefores(FrameworkMethod frameworkMethod,
                                    Object testInstance,
                                    Statement statement,
                                    TestContextManager testContextManager) {
		return new RunBeforeTestMethodCallbacks(statement,
                                                testInstance,
                                                frameworkMethod.getMethod(),
			                                    testContextManager);
	}

	protected Statement withAfters(FrameworkMethod frameworkMethod,
                                   Object testInstance,
                                   Statement statement,
                                   TestContextManager testContextManager) {
		return new RunAfterTestMethodCallbacks(statement,
                                               testInstance,
                                               frameworkMethod.getMethod(),
			                                   testContextManager);
	}

}