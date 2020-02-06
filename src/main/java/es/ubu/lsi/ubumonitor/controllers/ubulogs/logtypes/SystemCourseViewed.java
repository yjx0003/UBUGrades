package es.ubu.lsi.ubumonitor.controllers.ubulogs.logtypes;

import java.util.List;

import es.ubu.lsi.ubumonitor.model.LogLine;

/**
 * The user with id '' viewed the course with id ''. The user with id '' viewed
 * the section number '' of the course with id ''.
 * 
 * @author Yi Peng Ji
 *
 */
public class SystemCourseViewed extends ReferencesLog {

	/**
	 * Instacia única de la clase SystemCourseViewed.
	 */
	private static SystemCourseViewed instance;

	/**
	 * Constructor privado de la clase singleton.
	 */
	private SystemCourseViewed() {
	}

	/**
	 * Devuelve la instancia única de SystemCourseViewed.
	 * 
	 * @return instancia singleton
	 */
	public static SystemCourseViewed getInstance() {
		if (instance == null) {
			instance = new SystemCourseViewed();
		}
		return instance;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLogReferencesAttributes(LogLine log, List<Integer> ids) {
		ReferencesLog referencesLog = ids.size() == 2 ? UserCourse.getInstance() : UserSectionCourse.getInstance();
		referencesLog.setLogReferencesAttributes(log, ids);

	}

}
