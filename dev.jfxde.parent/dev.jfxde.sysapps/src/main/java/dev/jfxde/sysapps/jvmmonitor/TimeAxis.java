package dev.jfxde.sysapps.jvmmonitor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.chart.ValueAxis;

public class TimeAxis extends ValueAxis<Long> {

	private List<Integer> steps = List.of(1, 2, 5, 10, 15, 30);
	private List<Integer> units = List.of(1000, 60, 60, 24);
	private List<String> formats = List.of("HH:mm:ss", "HH:mm", "HH", "dd.MM.yyyy");
	private int unitIndex;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Override
	protected List<Long> calculateMinorTickMarks() {
		return List.of();
	}

	@Override
	protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {
		Range range = new Range();

		range.setLowerBound(minValue);
		range.setUpperBound(maxValue);
		double scale = calculateNewScale(length, minValue, maxValue);
		range.setScale(scale);

		long unit = getAccumutatedUnit();
		double diff = (maxValue - minValue) / unit;
		int step = 0;

		while (step == 0 && unitIndex < units.size()) {

			double stepLimit = Math.floor(diff / 5);
			step = steps.stream().filter(s -> s >= stepLimit).findFirst().orElse(0);

			if (step == 0) {
				unitIndex++;
				dateFormat.applyPattern(formats.get(unitIndex));
				diff /= units.get(unitIndex);
			}
		}

		range.setTickUnit(step);

		return range;
	}

	@Override
	protected void setRange(Object range, boolean animate) {
		Range r = (Range) range;
		setLowerBound(r.getLowerBound());
		setUpperBound(r.getUpperBound());
		setScale(r.getScale());
		currentLowerBound.set(r.getLowerBound());
	}

	@Override
	protected Object getRange() {
		return new Range();
	}

	@Override
	protected List<Long> calculateTickValues(double length, Object range) {

		List<Long> values = new ArrayList<>();
		Range r = (Range) range;

		long unit = getAccumutatedUnit();

		int nextUnit = units.get(unitIndex + 1);

		long min = (long) r.getLowerBound() / unit;
		long max = (long) r.getUpperBound() / unit;
		long roundedMin = min / nextUnit * nextUnit;

		for (long i = roundedMin; i <= max; i += r.getTickUnit()) {
			if (i >= min) {
				values.add(i * unit);
			}
		}

		return values;
	}

	@Override
	protected String getTickMarkLabel(Long value) {

		String result = dateFormat.format(value);

		return result;
	}

	private long getAccumutatedUnit() {
		long unit = units.stream().limit(unitIndex + 1).reduce((a, b) -> a * b).get();
		return unit;
	}

	private static class Range {
		private double lowerBound;
		private double upperBound;
		private double tickUnit;
		private double scale;

		public double getLowerBound() {
			return lowerBound;
		}

		public void setLowerBound(double lowerBound) {
			this.lowerBound = lowerBound;
		}

		public double getUpperBound() {
			return upperBound;
		}

		public void setUpperBound(double upperBound) {
			this.upperBound = upperBound;
		}

		public double getTickUnit() {
			return tickUnit;
		}

		public void setTickUnit(double tickUnit) {
			this.tickUnit = tickUnit;
		}

		public double getScale() {
			return scale;
		}

		public void setScale(double scale) {
			this.scale = scale;
		}

		@Override
		public String toString() {
			return "Range [lowerBound=" + lowerBound + ", upperBound=" + upperBound + ", tickUnit=" + tickUnit
					+ ", scale=" + scale + "]";
		}
	}
}
