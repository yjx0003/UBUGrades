package es.ubu.lsi.webservice.core;

import es.ubu.lsi.webservice.WSFunctions;
import es.ubu.lsi.webservice.WebService;

public class CoreCourseGetEnrolledCoursesByTimelineClassification extends WebService{

	private String classification;
	public static final String PAST = "past";
	public static final String IN_PROGRESS ="inprogress";
	public static final String FUTURE = "future";
	
	public CoreCourseGetEnrolledCoursesByTimelineClassification(String classification) {
		this.classification = classification;
	}
	
	@Override
	public WSFunctions getWSFunction() {
		return WSFunctions.CORE_COURSE_GET_ENROLLED_COURSES_BY_TIMELINE_CLASSIFICATION;
	}

	@Override
	protected void appendToUrlParameters() {
		appendToUrlClassification(classification);
		
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

}
