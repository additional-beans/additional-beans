package io.additionalbeans.build.architecture;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.netflix.nebula.archrules.core.ArchRulesService;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.domain.properties.HasOwner;
import com.tngtech.archunit.core.domain.properties.HasParameterTypes;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

/**
 * @see org.springframework.boot.build.architecture.ArchitectureCheck
 */
public class LibraryArchRules implements ArchRulesService {

	@Override
	public Map<String, ArchRule> getRules() {
		List<ArchRule> rules = List.of(
				allBeanPostProcessorBeanMethodsShouldBeStaticAndHaveParametersThatWillNotCausePrematureInitialization(),
				allBeanFactoryPostProcessorBeanMethodsShouldBeStaticAndHaveNoParameters(),
				noClassesShouldCallStepVerifierStepVerifyComplete(),
				noClassesShouldConfigureDefaultStepVerifierTimeout(), noClassesShouldCallCollectorsToList(),
				noClassesShouldCallURLEncoderWithStringEncoding(), noClassesShouldCallURLDecoderWithStringEncoding(),
				noClassesShouldLoadResourcesUsingResourceUtils(), noClassesShouldCallStringToUpperCaseWithoutLocale(),
				noClassesShouldCallStringToLowerCaseWithoutLocale(),
				conditionalOnMissingBeanShouldNotSpecifyOnlyATypeThatIsTheSameAsMethodReturnType(),
				enumSourceShouldNotSpecifyOnlyATypeThatIsTheSameAsMethodParameterType());
		return rules.stream().collect(Collectors.toMap(ArchRule::toString, Function.identity()));
	}

	private ArchRule allBeanPostProcessorBeanMethodsShouldBeStaticAndHaveParametersThatWillNotCausePrematureInitialization() {
		return ArchRuleDefinition.methods()
			.that()
			.areAnnotatedWith("org.springframework.context.annotation.Bean")
			.and()
			.haveRawReturnType(
					JavaClass.Predicates.assignableTo("org.springframework.beans.factory.config.BeanPostProcessor"))
			.should(onlyHaveParametersThatWillNotCauseEagerInitialization())
			.andShould()
			.beStatic()
			.allowEmptyShould(true);
	}

	private ArchCondition<JavaMethod> onlyHaveParametersThatWillNotCauseEagerInitialization() {
		DescribedPredicate<CanBeAnnotated> notAnnotatedWithLazy = DescribedPredicate
			.not(CanBeAnnotated.Predicates.annotatedWith("org.springframework.context.annotation.Lazy"));
		DescribedPredicate<JavaClass> notOfASafeType = DescribedPredicate
			.not(JavaClass.Predicates.assignableTo("org.springframework.beans.factory.ObjectProvider")
				.or(JavaClass.Predicates.assignableTo("org.springframework.context.ApplicationContext"))
				.or(JavaClass.Predicates.assignableTo("org.springframework.core.env.Environment")));
		return new ArchCondition<>("not have parameters that will cause eager initialization") {

			@Override
			public void check(JavaMethod item, ConditionEvents events) {
				item.getParameters()
					.stream()
					.filter(notAnnotatedWithLazy)
					.filter((parameter) -> notOfASafeType.test(parameter.getRawType()))
					.forEach((parameter) -> events.add(SimpleConditionEvent.violated(parameter,
							parameter.getDescription() + " will cause eager initialization as it is "
									+ notAnnotatedWithLazy.getDescription() + " and is "
									+ notOfASafeType.getDescription())));
			}

		};
	}

	private ArchRule allBeanFactoryPostProcessorBeanMethodsShouldBeStaticAndHaveNoParameters() {
		return ArchRuleDefinition.methods()
			.that()
			.areAnnotatedWith("org.springframework.context.annotation.Bean")
			.and()
			.haveRawReturnType(JavaClass.Predicates
				.assignableTo("org.springframework.beans.factory.config.BeanFactoryPostProcessor"))
			.should(onlyInjectEnvironment())
			.andShould()
			.beStatic()
			.allowEmptyShould(true);
	}

	private ArchCondition<JavaMethod> onlyInjectEnvironment() {
		return new ArchCondition<>("only inject Environment") {

			@Override
			public void check(JavaMethod item, ConditionEvents events) {
				List<JavaParameter> parameters = item.getParameters();
				for (JavaParameter parameter : parameters) {
					if (!"org.springframework.core.env.Environment".equals(parameter.getType().getName())) {
						events.add(SimpleConditionEvent.violated(item,
								item.getDescription() + " should only inject Environment"));
					}
				}
			}

		};
	}

	private ArchRule noClassesShouldCallStringToLowerCaseWithoutLocale() {
		return ArchRuleDefinition.noClasses()
			.should()
			.callMethod(String.class, "toLowerCase")
			.because("String.toLowerCase(Locale.ROOT) should be used instead");
	}

	private ArchRule noClassesShouldCallStringToUpperCaseWithoutLocale() {
		return ArchRuleDefinition.noClasses()
			.should()
			.callMethod(String.class, "toUpperCase")
			.because("String.toUpperCase(Locale.ROOT) should be used instead");
	}

	private ArchRule noClassesShouldCallStepVerifierStepVerifyComplete() {
		return ArchRuleDefinition.noClasses()
			.should()
			.callMethod("reactor.test.StepVerifier$Step", "verifyComplete")
			.because("it can block indefinitely and expectComplete().verify(Duration) should be used instead");
	}

	private ArchRule noClassesShouldConfigureDefaultStepVerifierTimeout() {
		return ArchRuleDefinition.noClasses()
			.should()
			.callMethod("reactor.test.StepVerifier", "setDefaultTimeout", "java.time.Duration")
			.because("expectComplete().verify(Duration) should be used instead");
	}

	private ArchRule noClassesShouldCallCollectorsToList() {
		return ArchRuleDefinition.noClasses()
			.should()
			.callMethod(Collectors.class, "toList")
			.because("java.util.stream.Stream.toList() should be used instead");
	}

	private ArchRule noClassesShouldCallURLEncoderWithStringEncoding() {
		return ArchRuleDefinition.noClasses()
			.should()
			.callMethod(URLEncoder.class, "encode", String.class, String.class)
			.because("java.net.URLEncoder.encode(String s, Charset charset) should be used instead");
	}

	private ArchRule noClassesShouldCallURLDecoderWithStringEncoding() {
		return ArchRuleDefinition.noClasses()
			.should()
			.callMethod(URLDecoder.class, "decode", String.class, String.class)
			.because("java.net.URLDecoder.decode(String s, Charset charset) should be used instead");
	}

	private ArchRule noClassesShouldLoadResourcesUsingResourceUtils() {
		return ArchRuleDefinition.noClasses()
			.should()
			.callMethodWhere(JavaCall.Predicates
				.target(HasOwner.Predicates.With.owner(JavaClass.Predicates.simpleName("ResourceUtils")))
				.and(JavaCall.Predicates.target(HasName.Predicates.name("getURL")))
				.and(JavaCall.Predicates.target(HasParameterTypes.Predicates.rawParameterTypes(String.class)))
				.or(JavaCall.Predicates
					.target(HasOwner.Predicates.With.owner(JavaClass.Predicates.simpleName("ResourceUtils")))
					.and(JavaCall.Predicates.target(HasName.Predicates.name("getFile")))
					.and(JavaCall.Predicates.target(HasParameterTypes.Predicates.rawParameterTypes(String.class)))))
			.because("org.springframework.boot.io.ApplicationResourceLoader should be used instead");
	}

	private List<ArchRule> noClassesShouldCallObjectsRequireNonNull() {
		return List.of(
				ArchRuleDefinition.noClasses()
					.should()
					.callMethod(Objects.class, "requireNonNull", Object.class, String.class)
					.because("org.springframework.utils.Assert.notNull(Object, String) should be used instead"),
				ArchRuleDefinition.noClasses()
					.should()
					.callMethod(Objects.class, "requireNonNull", Object.class, Supplier.class)
					.because("org.springframework.utils.Assert.notNull(Object, Supplier) should be used instead"));
	}

	private ArchRule conditionalOnMissingBeanShouldNotSpecifyOnlyATypeThatIsTheSameAsMethodReturnType() {
		return ArchRuleDefinition.methods()
			.that()
			.areAnnotatedWith("org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean")
			.should(notSpecifyOnlyATypeThatIsTheSameAsTheMethodReturnType())
			.allowEmptyShould(true);
	}

	private ArchCondition<? super JavaMethod> notSpecifyOnlyATypeThatIsTheSameAsTheMethodReturnType() {
		return new ArchCondition<>("not specify only a type that is the same as the method's return type") {

			@Override
			public void check(JavaMethod item, ConditionEvents events) {
				JavaAnnotation<JavaMethod> conditional = item
					.getAnnotationOfType("org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean");
				Map<String, Object> properties = conditional.getProperties();
				if (!properties.containsKey("type") && !properties.containsKey("name")) {
					conditional.get("value").ifPresent((value) -> {
						JavaType[] types = (JavaType[]) value;
						if (types.length == 1 && item.getReturnType().equals(types[0])) {
							events.add(SimpleConditionEvent.violated(item, conditional.getDescription()
									+ " should not specify only a value that is the same as the method's return type"));
						}
					});
				}
			}

		};
	}

	private ArchRule enumSourceShouldNotSpecifyOnlyATypeThatIsTheSameAsMethodParameterType() {
		return ArchRuleDefinition.methods()
			.that()
			.areAnnotatedWith("org.junit.jupiter.params.provider.EnumSource")
			.should(notSpecifyOnlyATypeThatIsTheSameAsTheMethodParameterType())
			.allowEmptyShould(true);
	}

	private ArchCondition<? super JavaMethod> notSpecifyOnlyATypeThatIsTheSameAsTheMethodParameterType() {
		return new ArchCondition<>("not specify only a type that is the same as the method's parameter type") {

			@Override
			public void check(JavaMethod item, ConditionEvents events) {
				JavaAnnotation<JavaMethod> conditional = item
					.getAnnotationOfType("org.junit.jupiter.params.provider.EnumSource");
				Map<String, Object> properties = conditional.getProperties();
				if (properties.size() == 1 && item.getParameterTypes().size() == 1) {
					conditional.get("value").ifPresent((value) -> {
						if (value.equals(item.getParameterTypes().get(0))) {
							events.add(SimpleConditionEvent.violated(item, conditional.getDescription()
									+ " should not specify only a value that is the same as the method's parameter type"));
						}
					});
				}
			}

		};
	}

}
