package es.ubu.lsi.ubumonitor.controllers.ubulogs;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import es.ubu.lsi.ubumonitor.model.LogLine;

/**
 * Agrupa los logs por horas-
 * @author Yi Peng Ji
 *
 */
public class GroupByHour extends GroupByAbstract<Integer> {

	/**
	 * 
	 */
	private static final List<Integer> HOURS = IntStream.range(0,24).boxed().collect(Collectors.toList()); 

	private static final long serialVersionUID = 1L;

	public GroupByHour(List<LogLine> logLines) {
		super(logLines);
	}

	/**
	 * {@inheritDoc}
	 * No usa las fechas de inicio ni de fin.
	 */
	@Override
	public List<Integer> getRange(LocalDate start, LocalDate end) {
		return HOURS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<LogLine, Integer> getGroupByFunction() {
		return LogLine::getHour;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<Integer, String> getStringFormatFunction() {
		return String::valueOf;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TypeTimes getTypeTime() {
		return TypeTimes.HOUR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean useDatePicker() {
		return false;
	}
}
