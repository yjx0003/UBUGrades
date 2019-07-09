package export.builder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import export.CSVBuilderAbstract;
import model.DataBase;
import model.EnrolledUser;
import model.GradeItem;

/**
 * Builds the grades file.
 * 
 * @author Raúl Marticorena
 * @since 2.4.0.0
 */
public class CSVGrade extends CSVBuilderAbstract {

	/** Logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CSVGrade.class);

	/** Header. */
	private static final String[] HEADER = new String[] { "UserId", "CourseModule", "Level", "Grade", "MinGrade",
			"MaxGrade", "WeightRaw", "UserFullName", "CourseModuleName" };
	
	static Comparator<EnrolledUser> compareByFullName = (EnrolledUser o1, EnrolledUser o2) -> o1.getFullName()
			.compareTo(o2.getFullName());

	/** Decimal format (change , by . ). */
	static DecimalFormat decimalFormat;
	
	// Format number with four decimal digits and point.
	static {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
		decimalFormat = new DecimalFormat("#.0000", otherSymbols);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param name name 
	 * @param dataBase dataBase
	 */
	public CSVGrade(String name, DataBase dataBase) {
		super(name, dataBase, HEADER);
	}

	@Override
	public void buildBody() {		

		Map<Integer, GradeItem> gradesMap = getDataBase().getGradeItems();
		List<GradeItem> gradeItems = new ArrayList<>(gradesMap.values());

		Map<Integer, EnrolledUser> enrolledUsers = getDataBase().getUsers();
		Collection<EnrolledUser> studentsCollection = enrolledUsers.values();

		Stream<EnrolledUser> usersWithoutNull = studentsCollection.stream().filter(user -> user.getFullName() != null)
				.sorted(compareByFullName);

		String modName, courseModuleId, fullName, weightRaw;
		for (EnrolledUser eu : (Iterable<EnrolledUser>) usersWithoutNull::iterator) {
			if (eu.getFullName() != null) {
				for (GradeItem gradeItem : gradeItems) {
					double value = gradeItem.getEnrolledUserGrade(eu);
					modName = gradeItem.getItemModule() != null ? gradeItem.getItemModule().getModName().toString()
							: "";
					fullName = eu.getFullName() != null ? eu.getFullName() : "";
					courseModuleId = gradeItem.getModule() != null ? Integer.toString(gradeItem.getModule().getCmid())
							: "";
					weightRaw = !Double.isNaN(gradeItem.getWeightraw()) ? decimalFormat.format(gradeItem.getWeightraw())
							: "NaN";
					LOGGER.debug("Data line: {}, {}, {}, {}, {}, {}, {}, {}, {}, {}", eu.getId(), courseModuleId, gradeItem.getLevel(),
							value, gradeItem.getGrademin(), gradeItem.getGrademax(),
							weightRaw, fullName, gradeItem.getItemname(), modName);
					
					getData().add(new String[] { 
							Integer.toString(eu.getId()),
							courseModuleId, 
							Integer.toString(gradeItem.getLevel()),
							Double.toString(value),
							Double.toString(gradeItem.getGrademin()),
							Double.toString(gradeItem.getGrademax()),
							weightRaw,
							fullName,
							gradeItem.getItemname(),
							modName							
					});
				}
			}
		}
	}
}
