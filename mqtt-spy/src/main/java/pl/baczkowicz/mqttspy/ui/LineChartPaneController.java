/***********************************************************************************
 * 
 * Copyright (c) 2013-2015 Jason Winnebeck, Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0 which 
 * accompany this distribution.
 *    
 * The Apache License Version 2.0 is available at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation; 
 *    					 partially derivative work created from the JFXUtils examples (https://github.com/gillius/jfxutils).
 *    
 */
package pl.baczkowicz.mqttspy.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.gillius.jfxutils.chart.StableTicksAxis;

import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.ui.charts.ChartMode;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.utils.StylingUtils;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.events.observers.MessageAddedObserver;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.spy.ui.properties.MessageLimitProperties;
import pl.baczkowicz.spy.ui.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * Controller for line chart pane.
 */
public class LineChartPaneController<T extends FormattedMessage> implements Initializable, MessageAddedObserver<T>
{
	private static boolean lastAutoRefresh = true;
	
	private static boolean lastDisplaySymbols = true;
	
	private static MessageLimitProperties lastMessageLimit 
		= new MessageLimitProperties("50 messages", 50, 0);

	@FXML
	private AnchorPane chartPane;
	
	@FXML
	private Label showRangeLabel;
	
	@FXML
	private ComboBox<MessageLimitProperties> showRangeBox;
	
	@FXML
	private Button refreshButton;
	
	@FXML
	private CheckBox autoRefreshCheckBox;
	
	@FXML
	private CheckMenuItem displaySymbolsCheckBox;
	
	@FXML
	private MenuButton optionsButton;
	
	private BasicMessageStoreWithSummary<T> store;

	private EventManager<T> eventManager;
	
	private MqttSubscription subscription;

	private Collection<String> topics;
	
	private Map<String, List<FormattedMessage>> chartData = new HashMap<>();
	
	private Map<String, Series<Number, Number>> topicToSeries = new LinkedHashMap<>();
	
	private LineChart<Number, Number> lineChart;
	
	private boolean warningShown;

	private String seriesTypeName;

	private ChartMode chartMode;

	private String seriesValueName;

	private String seriesUnit;
	
	/**
	 * @param seriesValueName the seriesValueName to set
	 */
	public void setSeriesValueName(String seriesValueName)
	{
		this.seriesValueName = seriesValueName;
	}

	public void initialize(URL location, ResourceBundle resources)
	{
		autoRefreshCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				lastAutoRefresh = autoRefreshCheckBox.isSelected();
				// Only refresh when auto-refresh enabled
				if (lastAutoRefresh)
				{
					refresh();				
				}
			}
		});
		displaySymbolsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				lastDisplaySymbols = displaySymbolsCheckBox.isSelected();
				refresh();
			}
		});
		showRangeBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				lastMessageLimit = showRangeBox.getValue();
				refresh();
			}
		});
		
		// Axis and chart        
		final NumberAxis xAxis = new NumberAxis();
		final StableTicksAxis yAxis = new StableTicksAxis();

        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>()
		{
			@Override
			public String toString(Number object)
			{
				final Date date = new Date(object.longValue());
				return TimeUtils.TIME_SDF.format(date);
			}
			
			@Override
			public Number fromString(String string)
			{
				return null;
			}
		});
        yAxis.setForceZeroInRange(false);
		lineChart = new LineChart<>(xAxis, yAxis);
	}		

	public void init()
	{		
		showRangeBox.setCellFactory(new Callback<ListView<MessageLimitProperties>, ListCell<MessageLimitProperties>>()
		{
			@Override
			public ListCell<MessageLimitProperties> call(ListView<MessageLimitProperties> l)
			{
				return new ListCell<MessageLimitProperties>()
				{
					@Override
					protected void updateItem(MessageLimitProperties item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText(null);
						}
						else
						{									
							setText(item.getName());
						}
					}
				};
			}
		});
		showRangeBox.setConverter(new StringConverter<MessageLimitProperties>()
		{
			@Override
			public String toString(MessageLimitProperties item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{
					return item.getName();
				}
			}

			@Override
			public MessageLimitProperties fromString(String id)
			{
				return null;
			}
		});
		showRangeBox.getItems().clear();
		showRangeBox.getItems().add(new MessageLimitProperties("All", 0, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("10 messages", 10, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("50 messages", 50, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("100 messages", 100, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("1k messages", 1000, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("10k messages", 10000, 0));
		showRangeBox.getItems().add(new MessageLimitProperties("1 minute", 0, TimeUtils.ONE_MINUTE));
		showRangeBox.getItems().add(new MessageLimitProperties("5 minutes", 0, 5 * TimeUtils.ONE_MINUTE));
		showRangeBox.getItems().add(new MessageLimitProperties("30 minutes", 0, 30 * TimeUtils.ONE_MINUTE));
		showRangeBox.getItems().add(new MessageLimitProperties("1 hour", 0, TimeUtils.ONE_HOUR));
		showRangeBox.getItems().add(new MessageLimitProperties("24 hours", 0, TimeUtils.ONE_DAY));
		showRangeBox.getItems().add(new MessageLimitProperties("48 hours", 0, 2 * TimeUtils.ONE_DAY));
		showRangeBox.getItems().add(new MessageLimitProperties("1 week", 0, 7 * TimeUtils.ONE_DAY));
				
		if (subscription != null)
		{
			refreshButton.setStyle(StylingUtils.createBaseRGBString(subscription.getColor()));
		}
		
		chartPane.getChildren().add(lineChart);
		AnchorPane.setBottomAnchor(lineChart, 0.0);
		AnchorPane.setLeftAnchor(lineChart, 0.0);
		AnchorPane.setTopAnchor(lineChart, 45.0);
		AnchorPane.setRightAnchor(lineChart, 0.0);
						
		if (ChartMode.USER_DRIVEN_MSG_PAYLOAD.equals(chartMode) || ChartMode.USER_DRIVEN_MSG_SIZE.equals(chartMode))
		{
			// Selecting a value will perform a refresh
			for (final MessageLimitProperties limit : showRangeBox.getItems())
			{
				if (limit.getMessageLimit() == lastMessageLimit.getMessageLimit()
						&& limit.getTimeLimit() == lastMessageLimit.getTimeLimit())
				{
					showRangeBox.setValue(limit);
				}
			}
			
			autoRefreshCheckBox.setSelected(lastAutoRefresh);
			displaySymbolsCheckBox.setSelected(lastDisplaySymbols);
		}
//		else if (ChartMode.STATS.equals(chartMode))
//		{
//			showRangeBox.setVisible(false);
//			showRangeLabel.setVisible(false);
//		}
		
		eventManager.registerMessageAddedObserver(this, store.getMessageList());
		
		setupPanAndZoom();
	}
	
	/**
	 * Sets up pan and zoom. Derivative work created from the JFXUtils examples (https://github.com/gillius/jfxutils).
	 */
	private void setupPanAndZoom()
	{
		// Panning works via either secondary (right) mouse or primary with ctrl
		// held down
		ChartPanManager panner = new ChartPanManager(lineChart);
		panner.setMouseFilter(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() == MouseButton.SECONDARY
						|| (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent
								.isShortcutDown()))
				{
					// let it through
				}
				else
				{
					mouseEvent.consume();
				}
			}
		});
		panner.start();
		// Zooming works only via primary mouse button without ctrl held down
		JFXChartUtil.setupZooming(lineChart, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() != MouseButton.PRIMARY
						|| mouseEvent.isShortcutDown())
					mouseEvent.consume();
			}
		});
		
		// Set up reset on double click
		lineChart.setOnMouseClicked(new EventHandler<MouseEvent>()
		{

			@Override
			public void handle(MouseEvent event)
			{
				if (event.getClickCount() == 2)
				{
					reset();
				}
			}
		});
	}
	
	public void cleanup()
	{
		eventManager.deregisterMessageAddedObserver(this);
	}
	
	private void divideMessagesByTopic(final Collection<String> topics)
	{
		chartData.clear();
		for (final FormattedMessage message : store.getMessages())
		{
			final String topic = message.getTopic();
			// logger.info("Topics = " + topics);
			if (topics.contains(topic))
			{				
				createTopicIfDoesNotExist(topic);
				chartData.get(topic).add(message);
			}
		}
	}
	
	private void createTopicIfDoesNotExist(final String topic)
	{
		if (chartData.get(topic) == null)
		{
			chartData.put(topic, new ArrayList<FormattedMessage>());
		}
	}
	
	/**
	 * Reset chart. Derivative work created from the JFXUtils examples (https://github.com/gillius/jfxutils).
	 */
	@FXML
	private void reset()
	{
		lineChart.getXAxis().setAutoRanging(true);
		lineChart.getYAxis().setAutoRanging(true);

		// Reset data to get ranging right
		final ObservableList<XYChart.Series<Number, Number>> data = lineChart.getData();
		lineChart.setData(FXCollections.<XYChart.Series<Number, Number>> emptyObservableList());
		lineChart.setData(data);
		lineChart.setData(FXCollections.<XYChart.Series<Number, Number>> emptyObservableList());
		lineChart.setData(data);
		lineChart.setAnimated(true);
	}
	
	private XYChart.Data<Number, Number> createDataObject(final FormattedMessage message)
	{
		if (ChartMode.USER_DRIVEN_MSG_PAYLOAD.equals(chartMode))
		{
			final Double value = Double.valueOf(message.getFormattedPayload());
			return new XYChart.Data<Number, Number>(message.getDate().getTime(), value);	
		}
		else if (ChartMode.USER_DRIVEN_MSG_SIZE.equals(chartMode))
		{
			final Integer value = Integer .valueOf(message.getPayload().length());
			return new XYChart.Data<Number, Number>(message.getDate().getTime(), value);	
		}
		else
		{
			// Nothing to do for now
		}
					
		return null;
	}
	
	private void addMessageToSeries(final Series<Number, Number> series, final FormattedMessage message)
	{
		try
    	{
    		series.getData().add(createDataObject(message));
    	}
    	catch (NumberFormatException e)
    	{
    		if (!warningShown && ChartMode.USER_DRIVEN_MSG_PAYLOAD.equals(chartMode))
    		{
    			String payload = message.getFormattedPayload();
    			if (payload.length() > 25)
    			{
    				payload = payload.substring(0, 25) + "...";
    			}
    			
    			DialogFactory.createWarningDialog(
    					"Invalid content",
    					"Payload \"" + payload 
    					+ "\" on \"" + message.getTopic() 
    					+ "\" cannot be converted to a number - ignoring all invalid values.");
    					
    			warningShown = true;
    		}
    	}
	}
	
	@FXML
	private void refresh()
	{		
		synchronized (chartData)
		{
			divideMessagesByTopic(topics);
			lineChart.getData().clear();
			lineChart.setCreateSymbols(lastDisplaySymbols);
			topicToSeries.clear();
			
			for (final String topic : topics)
			{
				final List<FormattedMessage> extractedMessages = new ArrayList<>();
				final Series<Number, Number> series = new XYChart.Series<>();
				topicToSeries.put(topic, series);
		        series.setName(topic);
		        
		        final MessageLimitProperties limit = showRangeBox.getValue();
		        final int itemsAvailable = chartData.get(topic).size();
		        
		        // Limit by number
		        int startIndex = 0;	        
		        if (limit.getMessageLimit() > 0 && limit.getMessageLimit() < itemsAvailable)
		        {
		        	startIndex = itemsAvailable - limit.getMessageLimit(); 
		        }
		        
		        // Limit by time
		        final Date now = new Date();
		        
		        for (int i = startIndex; i < itemsAvailable; i++)
		        {
		        	final FormattedMessage message = chartData.get(topic).get(chartData.get(topic).size() - i - 1);
		        	
		        	if (limit.getTimeLimit() > 0 && (message.getDate().getTime() + limit.getTimeLimit() < now.getTime()))
		        	{
		        		continue;
		        	}
		        	
		        	extractedMessages.add(message);
		        	addMessageToSeries(series, message);
		        }
		        // logger.info("Populated = {}=?{}/{}", chartData.get(topic).size(), topicToSeries.get(topic).getData().size(), limit.getMessageLimit());
				
		        // For further processing, take only messages put on chart
		        chartData.put(topic, extractedMessages);
		        lineChart.getData().add(series);
		        
		        populateTooltips(lineChart);
			}
		}
	}
	
	/**
	 * Populates the tooltip with data (chart-type independent).
	 * 
	 * @param series The series
	 * @param data The data
	 */
	private void populateTooltip(final Series<Number, Number> series, final Data<Number, Number> data)
	{
		final Date date = new Date();
		date.setTime(data.getXValue().longValue());
		
		final Tooltip tooltip = new Tooltip(
				seriesTypeName + " = " + series.getName()
				+ System.lineSeparator()
				+ seriesValueName + " = " + data.getYValue() + " " + seriesUnit
				+ System.lineSeparator()
				+ "Time = " + TimeUtils.TIME_SDF.format(date));
		
		Tooltip.install(data.getNode(), tooltip);
	}
	
	private void populateTooltips(final LineChart<Number, Number> lineChart)
	{
		for (final Series<Number, Number> series : lineChart.getData())
		{
			for (final Data<Number, Number> data : series.getData())
			{
				populateTooltip(series, data);
			}
		}
	}
	
	// TODO: optimise message handling
	@Override
	public void onMessageAdded(final List<BrowseReceivedMessageEvent<T>> events)
	{
		for (final BrowseReceivedMessageEvent<T> event : events)
		{
			onMessageAdded(event.getMessage());
		}
	}
	
	public void onMessageAdded(final FormattedMessage message)
	{
		// TODO: is that ever deregistered?
		synchronized (chartData)
		{	
			final String topic = message.getTopic();
			createTopicIfDoesNotExist(topic);		
			
			final MessageLimitProperties limit = showRangeBox.getValue();
			//logger.info("Message limit = {}", limit.getMessageLimit());
			//logger.info("Time limit = {}", limit.getTimeLimit());
			
			if (autoRefreshCheckBox.isSelected() && topics.contains(topic))
			{			
				// Apply message limit			
				while ((limit.getMessageLimit() > 0) && (chartData.get(topic).size() >= limit.getMessageLimit()))
				{
					//logger.info("Deleting = {}=?{}/{}", chartData.get(topic).size(), topicToSeries.get(topic).getData().size(), limit.getMessageLimit());
					chartData.get(topic).remove(0);
					topicToSeries.get(topic).getData().remove(0);
				}
				
				// Apply time limit
				final Date now = new Date();
				if (limit.getTimeLimit() > 0)
				{
					FormattedMessage oldestMessage = chartData.get(topic).get(0);
					while (oldestMessage.getDate().getTime() + limit.getTimeLimit() < now.getTime())
					{
						chartData.get(topic).remove(0);
						topicToSeries.get(topic).getData().remove(0);
						
						if (chartData.get(topic).size() == 0)
						{
							break;
						}
						oldestMessage = chartData.get(topic).get(0);
					}				
				}
				
				// Add the new message
				chartData.get(topic).add(message);
				addMessageToSeries(topicToSeries.get(topic), message);
				//logger.info("Added = {}=?{}/{}", chartData.get(topic).size(), topicToSeries.get(topic).getData().size(), limit.getMessageLimit());
				
				if (topicToSeries.get(topic).getData().size() > 0)
				{
					populateTooltip(topicToSeries.get(topic), 
						topicToSeries.get(topic).getData().get(topicToSeries.get(topic).getData().size() - 1));
				}
			}
		}
	}	
	
	public void setChartMode(final ChartMode mode)
	{
		this.chartMode = mode;
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================

	public void setTopics(final Collection<String> topics)
	{
		this.topics = topics;
	}
	
	public void setSubscription(MqttSubscription subscription)
	{
		this.subscription = subscription;		
	}
	
	public void setEventManager(final EventManager<T> eventManager)
	{
		this.eventManager = eventManager;
	}
	
	public void setStore(final BasicMessageStoreWithSummary<T> store)
	{
		this.store = store;		
	}	
	
	public void setSeriesTypeName(final String seriesTypeName)
	{
		this.seriesTypeName = seriesTypeName;
	}

	public void setSeriesUnit(String seriesUnit)
	{
		this.seriesUnit = seriesUnit;
	}
}
