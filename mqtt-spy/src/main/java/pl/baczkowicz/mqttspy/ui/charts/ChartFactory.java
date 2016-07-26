/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.ui.charts;

import java.util.Collection;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pl.baczkowicz.mqttspy.ui.LineChartPaneController;
import pl.baczkowicz.mqttspy.ui.PieChartPaneController;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.properties.SubscriptionTopicSummaryProperties;
import pl.baczkowicz.spy.ui.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;

@SuppressWarnings("unchecked")
public class ChartFactory<T extends FormattedMessage>
{
	public void createMessageBasedLineChart(Collection<String> topics, 
			final BasicMessageStoreWithSummary<T> store,
			final ChartMode mode, 
			final String seriesType, final String seriesValueName, 
			final String seriesUnit, final String title, 
			final Scene parentScene, final EventManager<T> eventManager)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("LineChartPane.fxml");
		final AnchorPane statsWindow = FxmlUtils.loadAnchorPane(loader);
		final LineChartPaneController<T> statsPaneController = ((LineChartPaneController<T>) loader.getController());		
		statsPaneController.setEventManager(eventManager);
		statsPaneController.setStore(store);
		statsPaneController.setSeriesTypeName(seriesType);
		statsPaneController.setTopics(topics);
		statsPaneController.setChartMode(mode);
		statsPaneController.setSeriesValueName(seriesValueName);
		statsPaneController.setSeriesUnit(seriesUnit);
		statsPaneController.init();
		
		Scene scene = new Scene(statsWindow);
		scene.getStylesheets().addAll(parentScene.getStylesheets());		

		final Stage statsPaneStage = new Stage();
		statsPaneStage.setWidth(600);
		statsPaneStage.setHeight(470);
		statsPaneStage.setScene(scene);			       
		statsPaneStage.setTitle(title);
		statsPaneStage.show();
		// Resize to get axis right
		statsPaneStage.setHeight(480);
		statsPaneStage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				statsPaneController.cleanup();
			}
		});
	}
	
	public void createMessageBasedPieChart(final String title, 
			final Scene parentScene, final ObservableList<SubscriptionTopicSummaryProperties<T>> observableList)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("PieChartPane.fxml");
		final AnchorPane chartWindow = FxmlUtils.loadAnchorPane(loader);
		final PieChartPaneController<T> chartPaneController = ((PieChartPaneController<T>) loader.getController());		
		chartPaneController.setObservableList(observableList);
		chartPaneController.init();
		
		Scene scene = new Scene(chartWindow);
		scene.getStylesheets().addAll(parentScene.getStylesheets());		

		final Stage statsPaneStage = new Stage();
		statsPaneStage.setWidth(800);
		statsPaneStage.setHeight(600);
		statsPaneStage.setScene(scene);			       
		statsPaneStage.setTitle(title);
		statsPaneStage.show();

		statsPaneStage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				chartPaneController.cleanup();
			}
		});
	}
}
