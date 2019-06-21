package controllers.ubulogs;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.Component;
import model.ComponentEvent;
import model.EnrolledUser;
import model.Event;
import model.LogLine;

/**
 * Clase abstracta con metodos de creacion de agrupamiento de los logs.
 * 
 * @author Yi Peng Ji
 *
 * @param <T>
 *            tipo de agrupamiento
 */
public abstract class GroupByAbstract<T extends Serializable> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(GroupByAbstract.class);

	private Map<EnrolledUser, Map<Component, Map<Event, Map<T, Long>>>> countsEvents;

	private String jsonCountEvents;



	private Map<EnrolledUser, Map<Component, Map<T, Long>>> countsComponents;
	private String jsonCountComponents;

	private Map<Component, Map<T, DescriptiveStatistics>> componentStatistics;
	private Map<Component, Map<Event, Map<T, DescriptiveStatistics>>> componentEventStatistics;

	/**
	 * Constructor para agrupar la lineas de log en funcion de los usuarios.
	 * 
	 * @param logLines
	 *            las lineas de log
	 */
	public GroupByAbstract(List<LogLine> logLines) {
		setCounts(logLines);

	}

	public static Predicate<LogLine> isBetweenTwoDates(ZonedDateTime start, ZonedDateTime end) {
		return l -> l.getTime().isAfter(start) && l.getTime().isBefore(end);
	}

	public static Predicate<LogLine> containsComponent(List<Component> components) {
		return l -> components.contains(l.getComponent());
	}

	/**
	 * Crea la estructura de contadores en un formato multi nivel.
	 * 
	 * @param logLines
	 *            loneas de log
	 */
	public void setCounts(List<LogLine> logLines) {
		// quitamos los nulos, sino salta excepcion en el Collectors.groupingBy
		// LogLine::getUser
		countsEvents = logLines.stream()
				.filter(l -> l.getUser() != null)
				.collect(Collectors.groupingBy(LogLine::getUser,
						Collectors.groupingBy(LogLine::getComponent,
								Collectors.groupingBy(LogLine::getEventName,
										Collectors.groupingBy(getGroupByFunction(), Collectors.counting())))));

		Gson gsonBuilder = new GsonBuilder().create();
		jsonCountEvents = gsonBuilder.toJson(countsEvents);
		LOGGER.info("JSON del contador de logs de componentes y eventos  para {} : {}", getTypeTime(),
				jsonCountEvents);

		countsComponents = logLines.stream()
				.filter(l -> l.getUser() != null)
				.collect(Collectors.groupingBy(LogLine::getUser,
						Collectors.groupingBy(LogLine::getComponent,
								Collectors.groupingBy(getGroupByFunction(), Collectors.counting()))));

		jsonCountComponents=gsonBuilder.toJson(countsComponents);
		LOGGER.info("JSON del contador de logs de componentes  para {} : {}", getTypeTime(),
				jsonCountComponents);

	}

	/**
	 * Devuelve los rangos en formato string
	 * 
	 * @param start
	 *            fecha de inico
	 * @param end
	 *            fecha de fin
	 * @return rangos de un tipo de fecha en formato string
	 */
	public List<String> getRangeString(LocalDate start, LocalDate end) {
		return getRangeString(getRange(start, end));
	}

	/**
	 * Devuelve los rangos en formato string
	 * 
	 * @param rangeList
	 *            lista de rangos
	 * @return rangos de un tipo de fecha en formato string
	 */
	public List<String> getRangeString(List<T> rangeList) {
		return rangeList.stream()
				.map(getStringFormatFunction())
				.collect(Collectors.toList());
	}

	/**
	 * Genera las estadisticas para un usuario
	 * 
	 * @param enrolledUsers
	 *            usuarios que se quiere generar las estadisticas
	 * @param components
	 *            componentes qeue se quiere generar
	 * @param groupByRange
	 *            rango de un tipo de agrupacion
	 */
	public void generateComponentsStatistics(List<EnrolledUser> enrolledUsers, List<Component> components,
			List<T> groupByRange) {

		if (enrolledUsers.isEmpty() || components.isEmpty() || groupByRange.isEmpty()) {
			return;
		}

		componentStatistics = new EnumMap<>(Component.class);
		// el metodo computeIfAbsent devuelve el valor de la key y si no existe la key
		// se crea y devuelve el valor nuevo.
		for (EnrolledUser user : enrolledUsers) {

			Map<Component, Map<T, Long>> userCounts = countsComponents.computeIfAbsent(user,
					k -> new EnumMap<>(Component.class));
			for (Component component : components) {

				Map<T, Long> userComponentsCounts = userCounts.computeIfAbsent(component, k -> new HashMap<>());
				Map<T, DescriptiveStatistics> statistics = componentStatistics.computeIfAbsent(component,
						k -> new HashMap<>());

				for (T groupBy : groupByRange) {

					long count = userComponentsCounts.computeIfAbsent(groupBy, k -> 0L);
					DescriptiveStatistics descriptiveStatistics = statistics.computeIfAbsent(groupBy,
							k -> new DescriptiveStatistics());
					descriptiveStatistics.addValue(count);
				}
			}
		}
		LOGGER.info("Estadisticas de todos los usuarios del curso para components {} y {} :\n{}", components,
				groupByRange, componentStatistics);

	}

	/**
	 * Genera las estadisticas para un usuario de componentes y eventos
	 * 
	 * @param enrolledUsers
	 *            usuarios que se quiere generar las estadisticas
	 * @param componentsEvents
	 *            componentes y eventos qeue se quiere generar
	 * @param groupByRange
	 *            rango de un tipo de agrupacion
	 */
	public void generateComponentsEventsStastistics(List<EnrolledUser> enrolledUsers,
			List<ComponentEvent> componentsEvents, List<T> groupByRange) {

		if (enrolledUsers.isEmpty() || componentsEvents.isEmpty() || groupByRange.isEmpty()) {
			return;
		}
		componentEventStatistics = new EnumMap<>(Component.class);
		// el metodo computeIfAbsent devuelve el valor de la key y si no existe la key
		// se crea y devuelve el valor nuevo.
		for (EnrolledUser user : enrolledUsers) {

			Map<Component, Map<Event, Map<T, Long>>> componentCounts = countsEvents.computeIfAbsent(user,
					k -> new EnumMap<>(Component.class));
			for (ComponentEvent componentEvent : componentsEvents) {

				Component component = componentEvent.getComponent();
				Event event = componentEvent.getEventName();

				Map<Event, Map<T, Long>> componentsCounts = componentCounts.computeIfAbsent(component,
						k -> new EnumMap<>(Event.class));
				Map<Event, Map<T, DescriptiveStatistics>> componentStatisticsMap = componentEventStatistics
						.computeIfAbsent(component, k -> new EnumMap<>(Event.class));

				Map<T, Long> eventsCounts = componentsCounts.computeIfAbsent(event, k -> new HashMap<>());
				Map<T, DescriptiveStatistics> eventDescriptiveStatistics = componentStatisticsMap
						.computeIfAbsent(event, k -> new HashMap<>());

				for (T groupBy : groupByRange) {

					long count = eventsCounts.computeIfAbsent(groupBy, k -> 0L);
					DescriptiveStatistics descriptiveStatistics = eventDescriptiveStatistics.computeIfAbsent(groupBy,
							k -> new DescriptiveStatistics());
					descriptiveStatistics.addValue(count);
				}
			}

		}

		LOGGER.info("Estadisticas de todos los usuarios del curso para components {} y {}:\n{}", componentsEvents,
				groupByRange, componentEventStatistics);
	}

	/**
	 * Devuelve las medias de los componentes de un listado de usuarios, componentes
	 * y fecha de inicio y de fin
	 * 
	 * @param enrolledUsers
	 *            lista de usuarios
	 * @param components
	 *            lista de componentes
	 * @param start
	 *            fecha de inicio
	 * @param end
	 *            fecha de fin
	 * @return medias de componentes
	 */
	public Map<Component, List<Double>> getComponentsMeans(List<EnrolledUser> enrolledUsers, List<Component> components,
			LocalDate start,
			LocalDate end) {

		List<T> range = this.getRange(start, end);

		generateComponentsStatistics(enrolledUsers, components, range);

		Map<Component, List<Double>> results = new EnumMap<>(Component.class);
		for (Component component : components) {
			List<Double> means = new ArrayList<>();
			Map<T, DescriptiveStatistics> statistics = componentStatistics.computeIfAbsent(component,
					k -> new HashMap<>());
			for (T typeTime : range) {
				DescriptiveStatistics descriptiveStatistics = statistics.computeIfAbsent(typeTime,
						k -> new DescriptiveStatistics());
				means.add(descriptiveStatistics.getMean());
			}
			results.put(component, means);

		}

		return results;
	}

	/**
	 * Devuelve las medias para un listado de usuarios, componente y evento con una
	 * fecha de inicio y fin.
	 * 
	 * @param enrolledUsers
	 *            usuarios matriculados
	 * @param componentsEvents
	 *            componente y evento
	 * @param start
	 *            fecha de inicio
	 * @param end
	 *            fecha de fin
	 * @return las medias
	 */
	public Map<ComponentEvent, List<Double>> getComponentEventMeans(List<EnrolledUser> enrolledUsers,
			List<ComponentEvent> componentsEvents,
			LocalDate start, LocalDate end) {

		List<T> range = this.getRange(start, end);

		generateComponentsEventsStastistics(enrolledUsers, componentsEvents, range);

		Map<ComponentEvent, List<Double>> results = new HashMap<>();
		for (ComponentEvent componentEvent : componentsEvents) {
			Component component = componentEvent.getComponent();
			Event event = componentEvent.getEventName();

			Map<Event, Map<T, DescriptiveStatistics>> eventStatistics = componentEventStatistics
					.computeIfAbsent(component, k -> new EnumMap<>(Event.class));
			Map<T, DescriptiveStatistics> statistics = eventStatistics.computeIfAbsent(event, k -> new HashMap<>());
			List<Double> means = new ArrayList<>();
			for (T typeTime : range) {
				DescriptiveStatistics descriptiveStatistics = statistics.computeIfAbsent(typeTime,
						k -> new DescriptiveStatistics());
				means.add(descriptiveStatistics.getMean());
			}
			results.put(componentEvent, means);
		}

		return results;
	}

	/**
	 * Devuelve los contadores de acceso a los registros de unos usuarios y
	 * componentes
	 * 
	 * @param users
	 *            usuarios
	 * @param components
	 *            componentes
	 * @param start
	 *            inicio
	 * @param end
	 *            fin
	 * @return mapa multinivel
	 */
	public Map<EnrolledUser, Map<Component, List<Long>>> getUsersComponentCounts(List<EnrolledUser> users,
			List<Component> components, LocalDate start, LocalDate end) {
		List<T> groupByRange = getRange(start, end);

		Map<EnrolledUser, Map<Component, List<Long>>> result = new HashMap<>();

		for (EnrolledUser user : users) {
			Map<Component, List<Long>> componentsCount = result.computeIfAbsent(user,
					k -> new EnumMap<>(Component.class));
			Map<Component, Map<T, Long>> userCounts = countsComponents.computeIfAbsent(user,
					k -> new EnumMap<>(Component.class));

			for (Component component : components) {
				List<Long> userComponentCounts = componentsCount.computeIfAbsent(component, k -> new ArrayList<>());
				Map<T, Long> counts = userCounts.computeIfAbsent(component, k -> new HashMap<>());

				for (T groupBy : groupByRange) {
					long count = counts.computeIfAbsent(groupBy, k -> 0L);
					userComponentCounts.add(count);
				}
			}
		}
		LOGGER.info("Estadisticas de los usuarios: {} para {} y {}:\n{}", users, components, groupByRange, result);
		return result;
	}

	/**
	 * Devuelve los contadores de acceso para un listado de usuarios, componente y
	 * evento en un rango de fechas
	 * 
	 * @param users
	 *            usuarios
	 * @param componentsEvents
	 *            componente y evento
	 * @param start
	 *            inicio
	 * @param end
	 *            fin
	 * @return contadores en multinivel
	 */
	public Map<EnrolledUser, Map<ComponentEvent, List<Long>>> getUserComponentEventCounts(List<EnrolledUser> users,
			List<ComponentEvent> componentsEvents, LocalDate start, LocalDate end) {

		List<T> groupByRange = getRange(start, end);
		Map<EnrolledUser, Map<ComponentEvent, List<Long>>> result = new HashMap<>();

		for (EnrolledUser user : users) {
			Map<ComponentEvent, List<Long>> componentEventResult = result.computeIfAbsent(user, k -> new HashMap<>());

			Map<Component, Map<Event, Map<T, Long>>> componentCounts = countsEvents.computeIfAbsent(user,
					k -> new EnumMap<>(Component.class));

			for (ComponentEvent componentEvent : componentsEvents) {
				Component component = componentEvent.getComponent();
				Event event = componentEvent.getEventName();

				List<Long> countsResult = componentEventResult.computeIfAbsent(componentEvent, k -> new ArrayList<>());
				Map<Event, Map<T, Long>> eventCounts = componentCounts.computeIfAbsent(component,
						k -> new EnumMap<>(Event.class));
				Map<T, Long> counts = eventCounts.computeIfAbsent(event, k -> new HashMap<>());

				for (T groupBy : groupByRange) {
					long count = counts.computeIfAbsent(groupBy, k -> 0L);
					countsResult.add(count);
				}
			}
		}

		return result;
	}

	/**
	 * El maximo de los componentes de los usuarios
	 * 
	 * @param enrolledUsers
	 *            usuarios que se quiere buscar el maximo
	 * @param components
	 *            componentes
	 * @param start
	 *            fecha de inicio
	 * @param end
	 *            fecha de fin
	 * @return maximo encontrado
	 */
	public long getMaxComponent(List<EnrolledUser> enrolledUsers, List<Component> components, LocalDate start,
			LocalDate end) {

		if (components.isEmpty()) {
			return 1L;
		}

		Map<EnrolledUser, Map<T, Long>> sumComponentsMap = new HashMap<>();
		List<T> range = getRange(start, end);
		for (EnrolledUser enrolledUser : enrolledUsers) {
			Map<Component, Map<T, Long>> componentsMap = countsComponents.get(enrolledUser);
			Map<T, Long> sumComponents = sumComponentsMap.computeIfAbsent(enrolledUser, k -> new HashMap<>());
			for (Component component : components) {
				Map<T, Long> groupByMap = componentsMap.get(component);
				for (T groupBy : range) {
					sumComponents.merge(groupBy, groupByMap.getOrDefault(groupBy, 0L), Long::sum);
				}

			}
		}
		LOGGER.info("Maximos de solo componentes: {} {}", components, sumComponentsMap);

		return getMax(sumComponentsMap);

	}

	/**
	 * Devuelve el maximo de los componentes y eventos a partir de una fecha de
	 * inicio y fin.
	 * 
	 * @param enrolledUsers
	 *            usuarios que se quiere sacar el maximo
	 * 
	 * @param componentsEvents
	 *            el listado de componentes y eventos que se quiere buscar el maximo
	 * @param start
	 *            fecha de inicio
	 * @param end
	 *            fecha de fin
	 * 
	 * @return el maximo encontrado
	 */
	public long getMaxComponentEvent(List<EnrolledUser> enrolledUsers, List<ComponentEvent> componentsEvents,
			LocalDate start, LocalDate end) {

		if (componentsEvents.isEmpty()) {
			return 1L;
		}

		Map<EnrolledUser, Map<T, Long>> sumComponentsMap = new HashMap<>();
		List<T> range = getRange(start, end);

		for (EnrolledUser enrolledUser : enrolledUsers) {
			Map<Component, Map<Event, Map<T, Long>>> componentsMap = countsEvents.get(enrolledUser);
			Map<T, Long> sumComponents = sumComponentsMap.computeIfAbsent(enrolledUser, k -> new HashMap<>());
			for (ComponentEvent componentEvent : componentsEvents) {
				Map<Event, Map<T, Long>> eventMap = componentsMap.get(componentEvent.getComponent());
				Map<T, Long> groupByMap = eventMap.get(componentEvent.getEventName());
				for (T groupBy : range) {
					sumComponents.merge(groupBy, groupByMap.getOrDefault(groupBy, 0L), Long::sum);
				}
			}

		}

		LOGGER.info("Maximos de componentes y eventos: {}{}", componentsEvents, sumComponentsMap);

		return getMax(sumComponentsMap);
	}

	/**
	 * Busca el máximo mapa de usuario por cada tipo de tiempo y el contador.
	 * 
	 * @param sumComponentsMap
	 *            mapa de usuario por cada tipo de tiempo y el contador
	 * @return el valor maximo encontrado
	 */
	private long getMax(Map<EnrolledUser, Map<T, Long>> sumComponentsMap) {
		long max = 1L;

		for (Map<T, Long> sumComponents : sumComponentsMap.values()) {
			for (long values : sumComponents.values()) {
				if (values > max) {
					max = values;
				}
			}
		}
		return max;
	}
	
	public String getJsonCountEvents() {
		return jsonCountEvents;
	}

	public String getJsonCountComponents() {
		return jsonCountComponents;
	}

	/**
	 * Devuelve el rango entre dos local date
	 * 
	 * @param start
	 *            fecha de inicio
	 * @param end
	 *            fecha de fin
	 * @return rango del tipo de tiempo entre dos fechas
	 */
	public abstract List<T> getRange(LocalDate start, LocalDate end);

	/**
	 * La funcion usada para agrupar el tipo de tiempo
	 * 
	 * @return devuelve la funcion que se usa para agrupar
	 */
	public abstract Function<LogLine, T> getGroupByFunction();

	/**
	 * Devuelve la funcion de cómo se formatea el tipo T
	 * 
	 * @return la función de formateo
	 */
	public abstract Function<T, String> getStringFormatFunction();

	/**
	 * Devuelve que forma de medir el tiempo es.
	 * 
	 * @return forma de medir
	 */
	public abstract TypeTimes getTypeTime();

	/**
	 * Si el tipo de agrupación usa el date picker
	 * 
	 * @return true si lo usa, false en caso contrario
	 */
	public abstract boolean useDatePicker();

	@Override
	public String toString() {
		return getTypeTime().toString();
	}

}
