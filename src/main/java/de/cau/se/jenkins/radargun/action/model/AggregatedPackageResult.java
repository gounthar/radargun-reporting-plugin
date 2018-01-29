package de.cau.se.jenkins.radargun.action.model;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import hudson.Functions;
import hudson.model.Action;
import hudson.model.Job;
import jenkins.model.Jenkins;
import radargun.lib.com.google.common.collect.Lists;

public class AggregatedPackageResult implements Action, StaplerProxy, ResultOverview {

	public final String packageName;
	public final Job<?, ?> job;
	public final SetMultimap<String, AggregatedPerformanceTestResult> performanceTestResults = HashMultimap.create();

	public AggregatedPackageResult(final String packageName, final Job<?, ?> job) {
		this.packageName = packageName;
		this.job = job;
	}

	public Collection<? extends PerformanceTest> getPerformanceTestResults() {
		return this.performanceTestResults.values();
	}

	public void addPerformanceTestResult(AggregatedPerformanceTestResult testResult) {
		if (!(testResult.benchmarkName == null)) {
			this.performanceTestResults.put(testResult.benchmarkName, testResult);
		}
	}

	public Collection<PerformanceTestResult> getFailedTests() {
		final Collection<PerformanceTestResult> failedTests = Lists.newArrayList();

		// for (final PerformanceTest performanceTestResult :
		// this.performanceTestResults) {
		// failedTests.addAll(performanceTestResult.getFailedTest());
		// }

		return failedTests;
	}

	public int getFailCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getTotalCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDisplayName() {
		return this.packageName;
	}

	@Override
	public String getIconFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrlName() {
		return this.packageName;
	}

	@Override
	public Object getTarget() {
		return this;
	}

	public Object getDynamic(final String token, final StaplerRequest req, final StaplerResponse rsp) {
		final Set<AggregatedPerformanceTestResult> performanceTestResults =  this.performanceTestResults.get(token);
		for(AggregatedPerformanceTestResult agrPtr : performanceTestResults) {
			if(agrPtr.unit.equals(req.getParameter("unit"))) {
				String val = req.getParameter("unit");
				return agrPtr;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getDisplayName());
	}

	@Override
	public boolean equals(Object o) {
		return Objects.equals(this, o);
	}

	public Job<?, ?> getJob() {
		return this.job;
	}

	public Collection<AggregatedPerformanceTestResult> getPerformanceTestResult(String testName) {
		return this.performanceTestResults.get(testName);
	}

	public void mergePerformanceTestResults(Collection<PerformanceTestResult> performanceTestResults) {
		for (final PerformanceTestResult performanceTestResult : performanceTestResults) {
			final AggregatedPerformanceTestResult agrPtr = this.getAggregatedTestResultForUnit(
					performanceTestResult.benchmarkName,
					performanceTestResult.getTestResult().getAssertion().getTimeunit());
			if (agrPtr == null) {
				this.performanceTestResults.put(performanceTestResult.benchmarkName,
						new AggregatedPerformanceTestResult(performanceTestResult, this.job));
			} else {
				agrPtr.addPerformanceTestResult(performanceTestResult);
			}

		}
	}

	private AggregatedPerformanceTestResult getAggregatedTestResultForUnit(final String benchmarkName,
			final String unit) {
		final Set<AggregatedPerformanceTestResult> aggregatedPerformanceTestResults = this.performanceTestResults
				.get(benchmarkName);
		for (final AggregatedPerformanceTestResult agrPtr : aggregatedPerformanceTestResults) {
			if (agrPtr.unit.equals(unit)) {
				return agrPtr;
			}
		}

		return null;
	}

	@Override
	public int getNumberOfTests() {
		return 0;
	}

	@Override
	public int getNumberOfFailedTests() {
		return 0;
	}

	@Override
	public int getNumberOfErrorTests() {
		return 0;
	}

	@Override
	public int getNumberOfSuccessfulTests() {
		return 0;
	}

	@JavaScriptMethod
	public String getDataPointsOfTest(String benchmarkName, String unit) {
		return this.getAggregatedTestResultForUnit(benchmarkName, unit).getDataPoints();
	}

	public String getResURL() {
		final String url = Jenkins.get().getRootUrl() + Functions.getResourcePath();
		return url;
	}
}
