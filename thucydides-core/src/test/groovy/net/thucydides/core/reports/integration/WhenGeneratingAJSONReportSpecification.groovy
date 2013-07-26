package net.thucydides.core.reports.integration

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.ValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import com.github.goldin.spock.extensions.tempdir.TempDir;

import net.thucydides.core.annotations.Feature;
import net.thucydides.core.annotations.Issue;
import net.thucydides.core.annotations.Issues;
import net.thucydides.core.annotations.Story;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.reports.AcceptanceTestReporter;
import net.thucydides.core.reports.TestOutcomes;
import net.thucydides.core.reports.integration.WhenGeneratingAJSONReportSpock.ATestScenarioWithIssues;
import net.thucydides.core.reports.json.JSONTestOutcomeReporter;
import net.thucydides.core.util.ExtendedTemporaryFolder;

class WhenGeneratingAJSONReportSpecification extends spock.lang.Specification {

	def AcceptanceTestReporter reporter

	@TempDir File outputDirectory					
	
	TestOutcomes allTestOutcomes = Mock();
		
	public void setup() throws IOException {
		reporter = new JSONTestOutcomeReporter();		
		reporter.setOutputDirectory(outputDirectory);
	}
	
	class AUserStory {
	}

	@Story(AUserStory.class)
	class SomeTestScenario {
		public void a_simple_test_case() {
		}

		public void should_do_this() {
		}

		public void should_do_that() {
		}
	}
	
	@WithTag(name="important feature", type = "feature")
	class SomeTestScenarioWithTags {
		public void a_simple_test_case() {
		}

		@WithTag(name="simple story",type = "story")
		public void should_do_this() {
		}

		public void should_do_that() {
		}
	}

	@Feature
	class AFeature {
		class AUserStoryInAFeature {
		}
	}

	@Story(AFeature.AUserStoryInAFeature.class)
	class SomeTestScenarioInAFeature {
		public void should_do_this() {
		}

		public void should_do_that() {
		}
	}

	class ATestScenarioWithoutAStory {
		public void should_do_this() {
		}

		public void should_do_that() {
		}

		public void and_should_do_that() {
		}
	}

	@Story(AUserStory.class)
	@Issues(["#123", "#456"])
	class ATestScenarioWithIssues {
		public void a_simple_test_case() {
		}

		@Issue("#789")
		public void should_do_this() {
		}

		public void should_do_that() {
		}
	}


	
	def "should get tags from user story if present"() {
		
		def TestOutcome testOutcome = TestOutcome.forTest("should_do_this", SomeTestScenario.class);
		 			
		expect:					
			testOutcome.getTags().contains(TestTag.withName("A user story").andType("story"))			
	}
	
	
	def "should_get_tags_from_user_stories_and_features_if_present"() {
		def TestOutcome testOutcome = TestOutcome.forTest("should_do_this", SomeTestScenarioInAFeature.class);
		
		expect:						
			testOutcome.getTags().containsAll([
				TestTag.withName("A user story in a feature").andType("story"),
				TestTag.withName("A feature").andType("feature")
			]);
												
	}

	
	def "should get tags using tag annotations if present"() {		
		def testOutcome = TestOutcome.forTest("should_do_this", SomeTestScenarioWithTags.class);
		
		expect:
			testOutcome.getTags().containsAll([TestTag.withName("important feature").andType("feature"),
				TestTag.withName("simple story").andType("story")]);
	}

	
	def "should add a story tag based on the class name if nothing else is specified"() {
		def testOutcome = TestOutcome.forTest("should_do_this", ATestScenarioWithoutAStory.class);
		
		expect : 
			testOutcome.getTags().contains(TestTag.withName("A test scenario without a story").andType("story"));
	}

	
	def "should generate an JSON report for an acceptance test run"()
			throws Exception {
		def testOutcome = TestOutcome.forTest("should_do_this", SomeTestScenario.class);
		def startTime = new DateTime(2013,1,1,0,0,0,0);
		testOutcome.setStartTime(startTime);
		
		String expectedReport = """\
			{
			  "title": "Should do this",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenGeneratingAJSONReportSpecification\$SomeTestScenario"
			  },
			  "result": "SUCCESS",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenGeneratingAJSONReportSpecification\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenGeneratingAJSONReportSpecification.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenGeneratingAJSONReportSpecification"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1373969264517,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
		""" 
			 
		testOutcome.recordStep(TestStepFactory.successfulTestStepCalled("step 1"))
		def jsonReport = reporter.generateReportFor(testOutcome, allTestOutcomes)
		def generatedReportText = getStringFrom(jsonReport)
		def jsonCmp = new CustomComparator(JSONCompareMode.STRICT, new Customization("test-steps[0].startTime", comparator))		
		
		expect : 
		   JSONCompare.compareJSON(expectedReport, generatedReportText,jsonCmp).passed();
	}
			
	
	def "should include issues in the JSON report"()
			throws Exception {
		def testOutcome = TestOutcome.forTest("should_do_this", ATestScenarioWithIssues.class);
		def startTime = new DateTime(2013,1,1,0,0,0,0);
		testOutcome.setStartTime(startTime);
		testOutcome.recordStep(TestStepFactory.successfulTestStepCalled("step 1"));
		
		def expectedReport = """
			{
			  "title": "Should do this",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenGeneratingAJSONReportSpecification\$ATestScenarioWithIssues",
			    "issues": [
			      "#123",
			      "#456",
			      "#789"
			    ]
			  },
			  "result": "SUCCESS",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenGeneratingAJSONReportSpecification\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenGeneratingAJSONReportSpecification.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenGeneratingAJSONReportSpecification"
			  },
			  "issues": [
			    "#456",
			    "#789",
			    "#123"
			  ],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1374810594394,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
		"""
			
		def jsonReport = reporter.generateReportFor(testOutcome, allTestOutcomes)
		def jsonCmp = new CustomComparator(JSONCompareMode.STRICT, new Customization("test-steps[0].startTime", comparator))
		System.out.println(getStringFrom(jsonReport) );
		expect:
		   JSONCompare.compareJSON(expectedReport, getStringFrom(jsonReport),jsonCmp).passed();
	}
		
			
	def getStringFrom(File reportFile) throws IOException {
		return FileUtils.readFileToString(reportFile);
	}
	
	
	ValueMatcher<Object> comparator = new ValueMatcher<Object>() {
		@Override
		public boolean equal(Object o1, Object o2) {
			return true;
		}
	};

}
