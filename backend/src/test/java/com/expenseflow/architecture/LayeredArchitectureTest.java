package com.expenseflow.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

@AnalyzeClasses(packages = "com.expenseflow", importOptions = {
        ImportOption.DoNotIncludeTests.class,
        LayeredArchitectureTest.ExcludeDebugController.class
})
public class LayeredArchitectureTest {

    public static class ExcludeDebugController implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.contains("DebugController");
        }
    }

    @ArchTest
    public static final ArchRule layeredArchitectureRule = Architectures.layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("com.expenseflow..controller..")
            .layer("Service").definedBy("com.expenseflow..service..", "com.expenseflow..provider..", "com.expenseflow..event..", "com.expenseflow..scheduler..", "com.expenseflow..listener..")
            .layer("Repository").definedBy("com.expenseflow..repository..")
            .layer("Security").definedBy("com.expenseflow..security..")
            .layer("Config").definedBy("com.expenseflow..config..")

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Security", "Config")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service", "Security", "Config");

    @ArchTest
    public static final ArchRule controllersShouldNotAccessRepositories = classes()
            .that().resideInAPackage("..controller..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..service..", "..dto..", "..security..", "..config..", "..exception..",
                    "java..", "jakarta..", "org.springframework..", "io.swagger..", "lombok..", "org.slf4j.."
            );

    @ArchTest
    public static final ArchRule servicesShouldNotDependOnControllers = classes()
            .that().resideInAPackage("..service..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..service..", "..repository..", "..entity..", "..dto..", "..config..",
                    "..util..", "..event..", "..exception..", "..security..", "..core..", "..mapper..",
                    "..provider..", "..specification..",
                    "java..", "jakarta..", "org.springframework..", "lombok..", "org.slf4j..", "org.hibernate.."
            );

    @ArchTest
    public static final ArchRule entitiesShouldNotBeExposedByControllers = methods()
            .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
            .and().arePublic()
            .should().notHaveRawReturnType(resideInAPackage("..entity.."));

    private static com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> resideInAPackage(String packageIdentifier) {
        return new com.tngtech.archunit.base.DescribedPredicate<>("reside in a package '" + packageIdentifier + "'") {
            @Override
            public boolean test(com.tngtech.archunit.core.domain.JavaClass javaClass) {
                return javaClass.getPackageName().matches(packageIdentifier.replace("..", ".*"));
            }
        };
    }
}
