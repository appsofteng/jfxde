package dev.jfxde.sysapps.jvmmonitor;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.DataUnit;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.PropertyDescriptor;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class JvmMonitorContent extends TabPane {

	private static final int COLUMN_COUNT = 2;
	private DoubleBinding prefColumnWidthBinding;
	private static final int WINDOW_SIZE = 3600;
	private static final int PERIOD = 1000;
	private AppContext context;
	private ScheduledFuture<?> future;
	private XYChart.Series<Long, Number> usedMemorySeries;
	private XYChart.Series<Long, Number> usedHeapMemorySeries;
	private XYChart.Series<Long, Number> usedNonHeapMemorySeries;
	private XYChart.Series<Long, Number> committedMemorySeries;
	private XYChart.Series<Long, Number> usedCpuSeries;
	private XYChart.Series<Long, Number> threadSeries;
	private XYChart.Series<Long, Number> daemonThreadSeries;
	private XYChart.Series<Long, Number> loadedClassSeries;
	private TimeAxis memoryXAxis;
	private TimeAxis cpuXAxis;
	private TimeAxis threadXAxis;
	private TimeAxis classXAxis;
	private NumberAxis threadYAxis;

	public JvmMonitorContent(AppContext context) {
		this.context = context;
		prefColumnWidthBinding = widthProperty().divide(COLUMN_COUNT);

		getTabs().addAll(createPropertyTab(), createProcessTab());

		start();
	}

	@SuppressWarnings("unchecked")
	private Tab createPropertyTab() {
		Tab tab = new Tab();
		tab.textProperty().bind(context.rc().getStringBinding("properties"));
		tab.setClosable(false);

		TableView<PropertyDescriptor> table = new TableView<>();
		tab.setContent(table);

		table.setItems(Sys.sm().getSystemProperties());

		TableColumn<PropertyDescriptor, String> keyColumn = new TableColumn<>();
		keyColumn.textProperty().bind(context.rc().getStringBinding("key"));
		keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));

		TableColumn<PropertyDescriptor, String> valueColumn = new TableColumn<>();
		valueColumn.textProperty().bind(context.rc().getStringBinding("value"));
		valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
		valueColumn.setMaxWidth(Double.MAX_VALUE);

		table.getColumns().addAll(keyColumn, valueColumn);
		table.getStyleClass().add("jd-table-view");

		return tab;
	}

	private Tab createProcessTab() {
		Tab tab = new Tab();
		tab.textProperty().bind(context.rc().getStringBinding("process"));
		tab.setClosable(false);

		GridPane pane = new GridPane();
		tab.setContent(pane);

		Chart memoryChart = createMemoryChart();
		Chart cpuChart = createCpuChart();
		Chart threadChart = createThreadChart();
		Chart classChart = createClassChart();

		GridPane.setConstraints(memoryChart, 0, 0);
		GridPane.setConstraints(cpuChart, 1, 0);
		GridPane.setConstraints(threadChart, 0, 1);
		GridPane.setConstraints(classChart, 1, 1);

		GridPane.setMargin(cpuChart, new Insets(0, 10, 0, 0));
		GridPane.setMargin(classChart, new Insets(0, 10, 0, 0));

		pane.getChildren().addAll(memoryChart, cpuChart, threadChart, classChart);

		return tab;
	}

	@SuppressWarnings("unchecked")
	private Chart createMemoryChart() {
		memoryXAxis = createXAxis();

		NumberAxis yAxis = new NumberAxis();
		yAxis.setAnimated(false);
		yAxis.setLabel("MiB");
		LineChart<Long, Number> chart = new LineChart<>(memoryXAxis, yAxis);
		chart.titleProperty().bind(context.rc().getStringBinding("memoryUsage"));
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		chart.prefWidthProperty().bind(prefColumnWidthBinding);

		usedHeapMemorySeries = new XYChart.Series<>();
		usedHeapMemorySeries.nameProperty().bind(context.rc().getStringBinding("heap"));

		usedNonHeapMemorySeries = new XYChart.Series<>();
		usedNonHeapMemorySeries.nameProperty().bind(context.rc().getStringBinding("nonHeap"));

		usedMemorySeries = new XYChart.Series<>();
		usedMemorySeries.nameProperty().bind(context.rc().getStringBinding("used"));

		committedMemorySeries = new XYChart.Series<>();
		committedMemorySeries.nameProperty().bind(context.rc().getStringBinding("committed"));

		chart.getData().addAll(usedHeapMemorySeries, usedNonHeapMemorySeries, usedMemorySeries, committedMemorySeries);

		return chart;
	}

	private Chart createCpuChart() {

		cpuXAxis = createXAxis();

		NumberAxis yAxis = new NumberAxis(0, 100, 10);
		yAxis.setAnimated(false);
		yAxis.setLabel("%");
		LineChart<Long, Number> chart = new LineChart<>(cpuXAxis, yAxis);
		chart.titleProperty().bind(context.rc().getStringBinding("cpuUsage"));
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		chart.prefWidthProperty().bind(prefColumnWidthBinding);

		usedCpuSeries = new XYChart.Series<>();
		usedCpuSeries.nameProperty().bind(context.rc().getStringBinding("used"));
		chart.getData().add(usedCpuSeries);

		return chart;
	}

	@SuppressWarnings("unchecked")
	private Chart createThreadChart() {
		threadXAxis = createXAxis();

		threadYAxis = new NumberAxis(0, 50, 5);
		threadYAxis.setAnimated(false);
		threadYAxis.labelProperty().bind(context.rc().getStringBinding("count"));
		LineChart<Long, Number> chart = new LineChart<>(threadXAxis, threadYAxis);
		chart.titleProperty().bind(context.rc().getStringBinding("threads"));
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		chart.prefWidthProperty().bind(prefColumnWidthBinding);

		daemonThreadSeries = new XYChart.Series<>();
		daemonThreadSeries.nameProperty().bind(context.rc().getStringBinding("daemon"));

		threadSeries = new XYChart.Series<>();
		threadSeries.nameProperty().bind(context.rc().getStringBinding("total"));

		chart.getData().addAll(daemonThreadSeries, threadSeries);

		return chart;
	}

	@SuppressWarnings("unchecked")
	private Chart createClassChart() {
		classXAxis = createXAxis();

		NumberAxis classYAxis = new NumberAxis();
		classYAxis.setAnimated(false);
		classYAxis.labelProperty().bind(context.rc().getStringBinding("count"));
		LineChart<Long, Number> chart = new LineChart<>(classXAxis, classYAxis);
		chart.titleProperty().bind(context.rc().getStringBinding("classes"));
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		chart.prefWidthProperty().bind(prefColumnWidthBinding);

		loadedClassSeries = new XYChart.Series<>();
		loadedClassSeries.nameProperty().bind(context.rc().getStringBinding("loaded"));

		chart.getData().addAll(loadedClassSeries);

		return chart;
	}

	private TimeAxis createXAxis() {
		TimeAxis axis = new TimeAxis();
		axis.setAnimated(false);
		axis.setMinorTickVisible(false);
		axis.setMinorTickCount(0);

		return axis;
	}

	private double getCpuUsage() {

		double usage = 0;

		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			AttributeList list = mbs.getAttributes(name, new String[] { "ProcessCpuLoad" });

			if (!list.isEmpty()) {
				Attribute att = (Attribute) list.get(0);
				Double value = (Double) att.getValue();
				// Negative means not available.
				if (value >= 0) {
					// percentage value with 1 decimal point precision
					usage = ((int) (value * 1000) / 10.0);
				}

			}

		} catch (Exception e) {

		}

		return usage;
	}

	private void start() {

		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
		MemoryUsage nonheapMemoryUsage = memoryBean.getNonHeapMemoryUsage();

		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();

		Runnable task = new Runnable() {
			public void run() {
				long time = System.currentTimeMillis();
				double usedHeapMemory = DataUnit.MiB.convert(heapMemoryUsage.getUsed(), DataUnit.Byte);
				double usedNonHeapMemory = DataUnit.MiB.convert(nonheapMemoryUsage.getUsed(), DataUnit.Byte);
				double usedMemory = usedHeapMemory + usedNonHeapMemory;
				double committedMemory = DataUnit.MiB
						.convert(heapMemoryUsage.getCommitted() + nonheapMemoryUsage.getCommitted(), DataUnit.Byte);
				double cpuUsage = getCpuUsage();
				double threadCount = threadBean.getThreadCount();
				double daemonThreadCount = threadBean.getDaemonThreadCount();
				double peakThreadCount = threadBean.getPeakThreadCount();
				double loadedClassCount = classBean.getLoadedClassCount();

				Platform.runLater(() -> {

					usedMemorySeries.getData().add(new XYChart.Data<>(time, usedMemory));
					usedHeapMemorySeries.getData().add(new XYChart.Data<>(time, usedHeapMemory));
					usedNonHeapMemorySeries.getData().add(new XYChart.Data<>(time, usedNonHeapMemory));
					committedMemorySeries.getData().add(new XYChart.Data<>(time, committedMemory));

					usedCpuSeries.getData().add(new XYChart.Data<>(time, cpuUsage));
					threadSeries.getData().add(new XYChart.Data<>(time, threadCount));
					daemonThreadSeries.getData().add(new XYChart.Data<>(time, daemonThreadCount));
					threadYAxis.setUpperBound(Math.ceil(peakThreadCount / 10) * 10);

					loadedClassSeries.getData().add(new XYChart.Data<>(time, loadedClassCount));

					if (usedMemorySeries.getData().size() > WINDOW_SIZE) {
						usedMemorySeries.getData().remove(0);
						usedHeapMemorySeries.getData().remove(0);
						usedNonHeapMemorySeries.getData().remove(0);
						committedMemorySeries.getData().remove(0);
						usedCpuSeries.getData().remove(0);
						threadSeries.getData().remove(0);
						daemonThreadSeries.getData().remove(0);
						loadedClassSeries.getData().remove(0);
					}
				});
			}
		};

		future = Sys.tm().scheduleAtFixedRate​(task, 0, PERIOD, TimeUnit.MILLISECONDS);
	}

	void stop() {
		future.cancel(false);
	}
}
