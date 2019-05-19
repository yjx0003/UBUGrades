package controllers.ubugrades;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.Controller;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import model.Course;
import model.DescriptionFormat;
import model.EnrolledUser;
import model.GradeItem;
import model.Group;
import model.MoodleUser;
import model.Role;
import model.mod.Module;
import model.mod.ModuleType;
import webservice.WebService;
import webservice.core.CoreCourseGetContents;
import webservice.core.CoreEnrolGetEnrolledUsers;
import webservice.core.CoreEnrolGetUsersCourses;
import webservice.core.CoreUserGetUsersByField;
import webservice.core.CoreUserGetUsersByField.Field;
import webservice.gradereport.GradereportUserGetGradeItems;
import webservice.gradereport.GradereportUserGetGradesTable;

public class CreatorUBUGradesController {

	static final Logger logger = LoggerFactory.getLogger(CreatorUBUGradesController.class);

	private static final Controller CONTROLLER = Controller.getInstance();

	private static final String FOLDER_ICON = "icon fa fa-folder fa-fw icon itemicon";
	private static final Pattern NIVEL = Pattern.compile("level(\\d+)");

	public static List<EnrolledUser> createEnrolledUsers(int courseid) throws IOException {

		WebService ws = CoreEnrolGetEnrolledUsers.newBuilder(courseid).build();

		String response = ws.getResponse();

		JSONArray users = new JSONArray(response);

		List<EnrolledUser> enrolledUsers = new ArrayList<EnrolledUser>();

		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			enrolledUsers.add(createEnrolledUser(user));
		}
		return enrolledUsers;

	}

	public static List<Role> createRoles(JSONArray jsonArray) {

		if (jsonArray == null)
			return null;

		List<Role> roles = new ArrayList<Role>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			roles.add(createRole(jsonObject));
		}
		return roles;
	}

	public static Role createRole(JSONObject jsonObject) {

		if (jsonObject == null)
			return null;

		int roleid = jsonObject.getInt("roleid");

		Role role = CONTROLLER.getBBDD().getRoleById(roleid);

		String name = jsonObject.getString("name");
		String shortName = jsonObject.getString("shortname");

		role.setName(name);
		role.setShortName(shortName);

		CONTROLLER.getBBDD().getActualCourse().addRole(role);

		return role;

	}

	public static List<Group> createGroups(JSONArray jsonArray) {

		if (jsonArray == null)
			return null;

		List<Group> groups = new ArrayList<Group>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			groups.add(createGroup(jsonObject));
		}
		return groups;
	}

	public static Group createGroup(JSONObject jsonObject) {

		if (jsonObject == null)
			return null;

		int groupid = jsonObject.getInt("id");
		Group group = CONTROLLER.getBBDD().getGroupById(groupid);

		String name = jsonObject.getString("name");
		String description = jsonObject.getString("description");
		DescriptionFormat descriptionFormat = DescriptionFormat.get(jsonObject.getInt("descriptionformat"));

		group.setName(name);
		group.setDescription(description);
		group.setDescriptionFormat(descriptionFormat);

		CONTROLLER.getBBDD().getActualCourse().addGroup(group);

		return group;

	}

	public static EnrolledUser createEnrolledUser(JSONObject user) {

		int id = user.getInt("id");

		EnrolledUser enrolledUser = CONTROLLER.getBBDD().getEnrolledUserById(id);

		enrolledUser.setFirstname(user.optString("firstname"));
		enrolledUser.setLastname(user.optString("lastname"));
		enrolledUser.setFullName(user.optString("fullname"));
		enrolledUser.setFirstaccess(Instant.ofEpochSecond(user.optInt("firstaccess")));
		enrolledUser.setLastaccess(Instant.ofEpochSecond(user.optInt("lastaccess")));
		enrolledUser.setDescription(user.optString("description"));
		enrolledUser.setDescriptionformat(DescriptionFormat.get(user.optInt("descriptionformat")));
		enrolledUser.setCity(user.optString("city"));
		enrolledUser.setCountry(user.optString("country"));
		enrolledUser.setProfileimageurlsmall(user.optString("profileimageurlsmall"));
		enrolledUser.setProfileimageurl(user.optString("profileimageurl"));

		byte[] imageBytes=convertImageToBytes(new Image(user.optString("profileimageurlsmall")));
	
		enrolledUser.setImageBytes(imageBytes);
		
		List<Course> courses = createCourses(user.optJSONArray("enrolledcourses"));
		courses.forEach(course -> course.addEnrolledUser(enrolledUser));

		List<Role> roles = createRoles(user.optJSONArray("roles"));
		roles.forEach(role -> enrolledUser.addRole(role));

		List<Group> groups = createGroups(user.optJSONArray("groups"));
		groups.forEach(group -> enrolledUser.addGroup(group));

		return enrolledUser;

	}
	private static byte[] convertImageToBytes(Image img) {
		//https://stackoverflow.com/a/24038735
		BufferedImage bImage = SwingFXUtils.fromFXImage(img, null);
		
		try (ByteArrayOutputStream s = new ByteArrayOutputStream();){
			ImageIO.write(bImage, "png", s);
			byte[] res  = s.toByteArray();
			return res;
		} catch (IOException e) {
			logger.error("No se ha podido transformar la imagen a bytes array");
		}
		return new byte[0];
		
	}

	public static List<Course> createCourses(JSONArray jsonArray) {
		if (jsonArray == null)
			return null;

		List<Course> courses = new ArrayList<Course>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			courses.add(createCourse(jsonObject));
		}
		return courses;

	}

	public static Course createCourse(JSONObject jsonObject) {
		if (jsonObject == null)
			return null;

		int id = jsonObject.getInt("id");
		Course course = CONTROLLER.getBBDD().getCourseById(id);

		String shortName = jsonObject.getString("shortname");
		String fullName = jsonObject.getString("fullname");

		String idNumber = jsonObject.optString("idnumber");
		String summary = jsonObject.optString("summary");
		DescriptionFormat summaryFormat = DescriptionFormat.get(jsonObject.optInt("summaryformat"));
		Instant startDate = Instant.ofEpochSecond(jsonObject.optInt("startdate"));
		Instant endDate = Instant.ofEpochSecond(jsonObject.optInt("enddate"));

		course.setShortName(shortName);
		course.setFullName(fullName);
		course.setIdNumber(idNumber);
		course.setSummary(summary);
		course.setSummaryformat(summaryFormat);
		course.setStartDate(startDate);
		course.setEndDate(endDate);

		CONTROLLER.getBBDD().putCourse(course);

		return course;

	}

	public static Module createModule(JSONObject jsonObject) {

		if (jsonObject == null)
			return null;

		int cmid = jsonObject.getInt("id");

		ModuleType moduleType = ModuleType.get(jsonObject.getString("modname"));

		Module module = CONTROLLER.getBBDD().getCourseModuleByIdOrCreate(cmid, moduleType);

		module.setId(jsonObject.getInt("id"));
		module.setUrl(jsonObject.optString("url"));
		module.setName(jsonObject.optString("name"));
		module.setInstance(jsonObject.optInt("instance"));
		module.setDescription(jsonObject.optString("description"));
		module.setVisible(jsonObject.optInt("visible"));
		module.setUservisible(jsonObject.optBoolean("uservisible"));
		module.setVisibleoncoursepage(jsonObject.optInt("visibleoncoursepage"));
		module.setModicon(jsonObject.optString("modicon"));
		module.setModuleType(ModuleType.get(jsonObject.getString("modname")));
		module.setIndent(jsonObject.optInt("indent"));

		CONTROLLER.getBBDD().getActualCourse().addModule(module);

		return module;

	}

	public static MoodleUser createMoodleUser(String username) throws IOException {
		WebService ws = new CoreUserGetUsersByField(Field.USERNAME, username);
		String response = ws.getResponse();

		JSONObject jsonObject = new JSONArray(response).getJSONObject(0);

		MoodleUser moodleUser = new MoodleUser();
		moodleUser.setId(jsonObject.getInt("id"));

		moodleUser.setUserName(jsonObject.optString("username"));

		moodleUser.setFullName(jsonObject.optString("fullname"));

		moodleUser.setEmail(jsonObject.optString("email"));

		moodleUser.setFirstAccess(Instant.ofEpochSecond(jsonObject.optLong("firstaccess")));

		moodleUser.setLastAccess(Instant.ofEpochSecond(jsonObject.optLong("lastaccess")));

		moodleUser.setUserPhoto(new Image(jsonObject.optString("profileimageurlsmall")));

		moodleUser.setLang(jsonObject.optString("lang"));

		moodleUser.setTimezone(jsonObject.optString("timezone"));

		List<Course> courses = getUserCourses(moodleUser.getId());
		moodleUser.setCourses(courses);

		return moodleUser;
	}

	public static List<Course> getUserCourses(int userid) throws IOException {
		WebService ws = new CoreEnrolGetUsersCourses(userid);
		String response = ws.getResponse();
		JSONArray jsonArray = new JSONArray(response);
		return createCourses(jsonArray);

	}

	public static List<Module> createModules(int courseid) throws IOException {

		WebService ws = CoreCourseGetContents.newBuilder(courseid)
				.setExcludecontents(true)
				.build();
		String response = ws.getResponse();

		JSONArray jsonArray = new JSONArray(response);
		List<Module> modulesList = new ArrayList<Module>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject section = jsonArray.getJSONObject(i);
			// por si se quiere crear una clase section

			JSONArray modules = section.getJSONArray("modules");

			for (int j = 0; j < modules.length(); j++) {
				JSONObject mJson = modules.getJSONObject(j);

				modulesList.add(createModule(mJson));

			}
		}

		return modulesList;

	}

	public static List<GradeItem> createGradeItems(int courseid) throws IOException {

		WebService ws = new GradereportUserGetGradesTable(courseid);

		String response = ws.getResponse();

		JSONObject jsonObject = new JSONObject(response);

		List<GradeItem> gradeItems = createHierarchyGradeItems(jsonObject);
		ws = new GradereportUserGetGradeItems(courseid);
		response = ws.getResponse();
		jsonObject = new JSONObject(response);
		setBasicAttributes(gradeItems, jsonObject);
		setEnrolledUserGrades(gradeItems, jsonObject);
		updateToOriginalGradeItem(gradeItems);

		return gradeItems;
	}

	private static void updateToOriginalGradeItem(List<GradeItem> gradeItems) {
		for (GradeItem gradeItem : gradeItems) {
			GradeItem original = CONTROLLER.getBBDD().getGradeItemById(gradeItem.getId());
			CONTROLLER.getBBDD().getActualCourse().addGradeItem(original);

			// comparamos si son diferente instancia. OJO no confundirse con != de Python
			if (gradeItem != original) {
				if (gradeItem.getFather() != null) {
					GradeItem originalFather = CONTROLLER.getBBDD().getGradeItemById(gradeItem.getFather().getId());
					original.setFather(originalFather);
				}
				List<GradeItem> originalChildren = new ArrayList<>();
				for (GradeItem child : gradeItem.getChildren()) {
					originalChildren.add(CONTROLLER.getBBDD().getGradeItemById(child.getId()));
				}
				original.setChildren(originalChildren);

				original.setItemname(gradeItem.getItemname());
				original.setLevel(gradeItem.getLevel());
				original.setWeightraw(gradeItem.getWeightraw());
				original.setGraderaw(gradeItem.getGraderaw());
				original.setGrademax(gradeItem.getGrademax());
				original.setGrademin(gradeItem.getGrademin());
			}

		}
	}

	private static List<GradeItem> createHierarchyGradeItems(JSONObject jsonObject) throws IOException {

		JSONObject table = jsonObject.getJSONArray("tables").getJSONObject(0);

		int maxDepth = table.getInt("maxdepth") + 1;

		GradeItem[] categories = new GradeItem[maxDepth];

		JSONArray tabledata = table.getJSONArray("tabledata");

		List<GradeItem> gradeItems = new ArrayList<GradeItem>();
		for (int i = 0; i < tabledata.length(); i++) {

			JSONObject item = tabledata.optJSONObject(i); // linea del gradereport

			if (item == null) // grade item no visible
				continue;

			JSONObject itemname = item.getJSONObject("itemname");
			int nivel = getNivel(itemname.getString("class"));

			Document content = Jsoup.parseBodyFragment(itemname.getString("content"));

			GradeItem gradeItem = new GradeItem();

			gradeItem.setLevel(nivel);
			gradeItem.setItemname(content.text());
			Element element;
			if ((element = content.selectFirst("i")) != null && element.className().equals(FOLDER_ICON)) {
				gradeItem.setItemname(content.text());
				categories[nivel] = gradeItem;
				setFatherAndChildren(categories, nivel, gradeItem);

			} else if (categories[nivel] != null) {

				gradeItems.add(categories[nivel]);
				categories[nivel] = null;

			} else {

				gradeItems.add(gradeItem);
				setFatherAndChildren(categories, nivel, gradeItem);
			}

		}

		return gradeItems;
	}

	private static void setBasicAttributes(List<GradeItem> gradeItems, JSONObject jsonObject)
			throws IOException {

		JSONObject usergrade = jsonObject.getJSONArray("usergrades").getJSONObject(0);

		Course course = CONTROLLER.getBBDD().getCourseById(usergrade.getInt("courseid"));

		JSONArray gradeitems = usergrade.getJSONArray("gradeitems");

		if (gradeitems.length() != gradeItems.size()) {
			logger.error(
					"El tamaño de las lineas del calificador no son iguales: de la funcion gradereport_user_get_grade_items es de tamaño"
							+ gradeitems.length() + "y de la funcion gradereport_user_get_grades_table se obtiene: "
							+ gradeItems.size());
			throw new IllegalStateException(
					"El tamaño de las lineas del calificador no son iguales: de la funcion gradereport_user_get_grade_items es de tamaño"
							+ gradeitems.length() + "y de la funcion gradereport_user_get_grades_table se obtiene: "
							+ gradeItems.size());
		}

		for (int i = 0; i < gradeitems.length(); i++) {
			JSONObject gradeitem = gradeitems.getJSONObject(i);
			GradeItem gradeItem = gradeItems.get(i);

			gradeItem.setId(gradeitem.getInt("id"));

			CONTROLLER.getBBDD().putGradeItem(gradeItem);

			gradeItem.setCourse(course);
			String itemtype = gradeitem.getString("itemtype");
			ModuleType moduleType;

			if (itemtype.equals("mod")) {
				Module module = CONTROLLER.getBBDD().getCourseModuleById(gradeitem.getInt("cmid"));
				gradeItem.setModule(module);
				moduleType = ModuleType.get(gradeitem.getString("itemmodule"));

			} else if (itemtype.equals("course")) {
				moduleType = ModuleType.CATEGORY;

			} else {
				moduleType = ModuleType.get(itemtype);
			}
			gradeItem.setItemModule(moduleType);
			gradeItem.setWeightraw(gradeitem.optDouble("weightraw"));

			gradeItem.setGrademin(gradeitem.optDouble("grademin"));
			gradeItem.setGrademax(gradeitem.optDouble("grademax"));

		}

	}

	private static void setEnrolledUserGrades(List<GradeItem> gradeItems, JSONObject jsonObject) {
		JSONArray usergrades = jsonObject.getJSONArray("usergrades");

		for (int i = 0; i < usergrades.length(); i++) {

			JSONObject usergrade = usergrades.getJSONObject(i);

			EnrolledUser enrolledUser = CONTROLLER.getBBDD().getEnrolledUserById(usergrade.getInt("userid"));

			JSONArray gradeitems = usergrade.getJSONArray("gradeitems");
			for (int j = 0; j < gradeitems.length(); j++) {
				JSONObject gradeitem = gradeitems.getJSONObject(j);
				GradeItem gradeItem = gradeItems.get(j);

				enrolledUser.addGrade(gradeItem, gradeitem.optDouble("graderaw"));

			}
		}

	}

	protected static void setFatherAndChildren(GradeItem[] categories, int nivel, GradeItem gradeItem) {
		if (nivel > 1) {
			GradeItem padre = categories[nivel - 1];
			gradeItem.setFather(padre);
			padre.addChildren(gradeItem);
		}
	}

	/**
	 * Busca el nivel jerarquico del grade item dentro del calificador. Por ejemplo
	 * "level1 levelodd oddd1 b1b b1t column-itemname", devolvería 1.
	 * 
	 * @param stringClass
	 *            el string del key "class" de "itemname"
	 * @return el nivel
	 */
	protected static int getNivel(String stringClass) {
		Matcher matcher = NIVEL.matcher(stringClass);
		if (matcher.find()) {
			return Integer.valueOf(matcher.group(1));
		}
		throw new IllegalStateException("No se encuentra el nivel en " + stringClass
				+ ", probablemente haya cambiado el formato de las tablas.");
	}

}
