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

package pl.baczkowicz.spy.ui;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.scripts.BaseScriptManagerInterface;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.connections.IConnectionFactory;
import pl.baczkowicz.spy.ui.controllers.AboutController;
import pl.baczkowicz.spy.ui.controllers.EditChartSeriesController;
import pl.baczkowicz.spy.ui.controllers.EditConnectionsController;
import pl.baczkowicz.spy.ui.controllers.FormattersController;
import pl.baczkowicz.spy.ui.controllers.TestCasesExecutionController;
import pl.baczkowicz.spy.ui.events.ConnectionStatusChangeEvent;
import pl.baczkowicz.spy.ui.events.ConnectionsChangedEvent;
import pl.baczkowicz.spy.ui.events.LoadConfigurationFileEvent;
import pl.baczkowicz.spy.ui.events.ShowAboutWindowEvent;
import pl.baczkowicz.spy.ui.events.ShowEditChartSeriesWindowEvent;
import pl.baczkowicz.spy.ui.events.ShowEditConnectionsWindowEvent;
import pl.baczkowicz.spy.ui.events.ShowExternalWebPageEvent;
import pl.baczkowicz.spy.ui.events.ShowFormattersWindowEvent;
import pl.baczkowicz.spy.ui.events.ShowTestCasesWindowEvent;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityManager;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.panes.TitledPaneController;
import pl.baczkowicz.spy.ui.stats.StatisticsManager;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.ImageUtils;
import pl.baczkowicz.spy.ui.versions.VersionManager;

public abstract class BaseViewManager
{
	private final static Logger logger = LoggerFactory.getLogger(BaseViewManager.class);
	
	public final static KeyCombination newSubscription = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	
	public final static KeyCombination newPublication = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	
	public static int TITLE_MARGIN = 13;
	
	protected IConfigurationManager configurationManager;

	protected VersionManager versionManager;

	protected Application application;
	
	protected IKBus eventBus;
	
	protected IConnectionFactory connectionFactory;

	protected ObservableList<String> stylesheets;

	protected StatisticsManager statisticsManager;
	
	protected SpyPerspective selectedPerspective = SpyPerspective.DEFAULT;
	
	// Controllers and stages
	
	protected Stage aboutStage;
	
	protected Stage formattersStage;
	
	protected Stage testCasesStage;	

	protected Stage editConnectionsStage; 	
	
	protected AboutController aboutController;

	protected EditConnectionsController editConnectionsController;

	protected Stage chartSeriesStage;

	protected EditChartSeriesController editChartSeriesController;

	private BaseScriptManagerInterface genericScriptManager;
	
	public void init()
	{
		eventBus.subscribe(this, this::showAbout, ShowAboutWindowEvent.class);
		eventBus.subscribe(this, this::showFormatters, ShowFormattersWindowEvent.class);
		eventBus.subscribe(this, this::showTestCases, ShowTestCasesWindowEvent.class);
		eventBus.subscribe(this, this::showEditConnectionsWindow, ShowEditConnectionsWindowEvent.class);
		eventBus.subscribe(this, this::showEditChartSeries, ShowEditChartSeriesWindowEvent.class);
		eventBus.subscribe(this, this::showExternalWebPage, ShowExternalWebPageEvent.class);
		
		TITLE_MARGIN = BaseConfigurationUtils.getIntegerProperty("ui.titlepane.margin", TITLE_MARGIN, configurationManager.getUiPropertyFile());
		logger.trace("Property TITLE_MARGIN = {}", TITLE_MARGIN);
	}

	private void initialiseAboutWindow(final Window parentWindow)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("AboutWindow.fxml");
		final AnchorPane window = FxmlUtils.loadAnchorPane(loader);
		
		aboutController = ((AboutController) loader.getController());
		aboutController.setVersionManager(versionManager);
		aboutController.setEventBus(eventBus);
		aboutController.setPropertyFileLoader(configurationManager.getDefaultPropertyFile());
		aboutController.init();
		
		Scene scene = new Scene(window);
		scene.getStylesheets().addAll(stylesheets);		

		aboutStage = new Stage();
		aboutStage.setTitle("About mqtt-spy");		
		aboutStage.initModality(Modality.WINDOW_MODAL);
		aboutStage.initOwner(parentWindow);
		aboutStage.setScene(scene);
	}

	private void initialiseEditChartSeriesWindow(final Window parentWindow)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("EditChartSeries.fxml");
		final AnchorPane editChartSeriesWindow = FxmlUtils.loadAnchorPane(loader);
		
		editChartSeriesController = ((EditChartSeriesController) loader.getController());
		editChartSeriesController.setEventBus(eventBus);
		
		Scene scene = new Scene(editChartSeriesWindow);
		scene.getStylesheets().addAll(stylesheets);		

		chartSeriesStage = new Stage();
		chartSeriesStage.setTitle("Chart series editor");		
		chartSeriesStage.initOwner(parentWindow);
		chartSeriesStage.setScene(scene);
		((EditChartSeriesController) loader.getController()).init();
	}
	
	private void initialiseTestCasesWindow(final Window parentWindow)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("TestCasesExecutionPane.fxml");
		final AnchorPane testCasesWindow = FxmlUtils.loadAnchorPane(loader);		
		
		Scene scene = new Scene(testCasesWindow);
		scene.getStylesheets().addAll(stylesheets);		

		testCasesStage = new Stage();
		testCasesStage.setTitle("Test cases");		
		testCasesStage.initOwner(parentWindow);
		testCasesStage.setScene(scene);
		((TestCasesExecutionController) loader.getController()).init();
	}

	private void initialiseFormattersWindow(final Window parentWindow)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("FormattersWindow.fxml");
		final AnchorPane formattersWindow = FxmlUtils.loadAnchorPane(loader);
		
		final FormattersController formattersController = ((FormattersController) loader.getController());
		formattersController.setConfigurationManager(configurationManager);	
		formattersController.setScriptManager(genericScriptManager);
		formattersController.setEventBus(eventBus);
		formattersController.init();
		
		Scene scene = new Scene(formattersWindow);
		scene.getStylesheets().addAll(stylesheets);		

		formattersStage = new Stage();
		formattersStage.setTitle("Formatters");		
		formattersStage.initModality(Modality.WINDOW_MODAL);
		formattersStage.initOwner(parentWindow);
		formattersStage.setScene(scene);
	}
	
	protected void initialiseEditConnectionsWindow(final Window parentWindow)
	{
		// This is a dirty way to reload connection settings :) possibly could be removed if all connections are closed before loading a new config file
		if (editConnectionsController != null)
		{
			eventBus.unsubscribeConsumer(editConnectionsController, ConnectionStatusChangeEvent.class);
		}
		
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("EditConnectionsWindow.fxml");
		final AnchorPane connectionWindow = FxmlUtils.loadAnchorPane(loader);
		editConnectionsController = ((EditConnectionsController) loader.getController());		
		editConnectionsController.setEventBus(eventBus);
		editConnectionsController.setConnectionFactory(connectionFactory);
		editConnectionsController.setConfigurationManager(configurationManager);
		editConnectionsController.init();
		
		Scene scene = new Scene(connectionWindow);
		scene.getStylesheets().addAll(stylesheets);		

		editConnectionsStage = new Stage();
		editConnectionsStage.setTitle("Connection list");		
		editConnectionsStage.initModality(Modality.WINDOW_MODAL);
		editConnectionsStage.initOwner(parentWindow);
		editConnectionsStage.setMinWidth(950);
		editConnectionsStage.setScene(scene);
	}
	
	public void showAbout(final ShowAboutWindowEvent event)
	{
		if (aboutStage == null)
		{
			initialiseAboutWindow(event.getParent());
		}
		
		aboutController.reloadVersionInfo();
		aboutStage.show();		
	}
	
	public void showEditChartSeries(final ShowEditChartSeriesWindowEvent event)
	{
		if (chartSeriesStage == null)
		{
			initialiseEditChartSeriesWindow(event.getParent());
		}
		
		editChartSeriesController.populateValues(event.getEditedProperties());
		
		chartSeriesStage.show();		
	}
	
	public void showFormatters(final ShowFormattersWindowEvent event)
	{
		if (formattersStage == null || !event.getParent().equals(formattersStage.getScene().getWindow()))
		{
			initialiseFormattersWindow(event.getParent());
		}
		
		if (event.isShowAndWait())
		{
			formattersStage.initOwner(event.getParent());
			formattersStage.showAndWait();
			
			// Note: removed because we now check for the parent window, and recreate if necessary
			// formattersStage = null;
		}
		else
		{
			formattersStage.show();
		}
	}
	
	public void showTestCases(final ShowTestCasesWindowEvent event)
	{
		if (testCasesStage == null)
		{
			initialiseTestCasesWindow(event.getParent());
		}
		
		testCasesStage.show();
	}
	
	public void showEditConnectionsWindow(final ShowEditConnectionsWindowEvent event)
	{
		logger.debug("showEditConnectionsWindow()");
		if (editConnectionsController == null)
		{
			initialiseEditConnectionsWindow(event.getParent());
		}
		
		if (event.isCreateNew())
		{
			editConnectionsController.newConnection(IConnectionFactory.MQTT);
		}

		if (event.getConnectionProperties() != null)
		{
			editConnectionsController.selectConnection(event.getConnectionProperties());
		}
		
		editConnectionsController.updateUIForSelectedItem();
		editConnectionsController.setPerspective(selectedPerspective);
		editConnectionsStage.showAndWait();		
		eventBus.publish(new ConnectionsChangedEvent());
	}
	
	public static boolean getDetailedViewStatus(final SpyPerspective perspective)
	{
		switch (perspective)
		{
			case BASIC:
				return false;
			case DETAILED:
				return true;
			case SPY:
				return false;
			case SUPER_SPY:
				return true;
			default:
				return false;	
		}
	}
	
	public static boolean getBasicViewStatus(final SpyPerspective perspective)
	{
		switch (perspective)
		{
			case BASIC:
				return true;
			case DETAILED:
				return false;
			case SPY:
				return false;
			case SUPER_SPY:
				return false;
			default:
				return false;	
		}
	}
	
	public void loadDefaultConfigurationFile()
	{		
		final File defaultConfigurationFile = BaseConfigurationManager.getDefaultConfigurationFileObject();
		
		logger.info("Default configuration file present (" + defaultConfigurationFile.getAbsolutePath() + ") = " + defaultConfigurationFile.exists());
		
		if (defaultConfigurationFile.exists())
		{
			eventBus.publish(new LoadConfigurationFileEvent(defaultConfigurationFile));
		}
		else
		{
			configurationManager.initialiseConfiguration();
		}
	}
	
	public void showExternalWebPage(final ShowExternalWebPageEvent event)
	{
		application.getHostServices().showDocument(event.getWebpage());
	}
	
	public static ChangeListener<Number> createPaneTitleWidthListener(final TitledPane pane, final AnchorPane paneTitle)
	{
		return new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{				 										
				Platform.runLater(new Runnable()
				{										
					@Override
					public void run()
					{
						BaseViewManager.updateTitleWidth(pane, paneTitle, TITLE_MARGIN, null);
					}
				});
			}
		};
	}
	
	public static MenuButton createTitleButton(final String title, final String iconLocation, final double offset, 
			final PaneVisibilityManager paneVisibilityManager, final TitledPane pane)
	{
		final MenuButton button = new MenuButton();
		button.setId("pane-settings-button");
		button.setTooltip(new Tooltip(title));
		button.setPadding(Insets.EMPTY);
		button.setLineSpacing(0);
		button.setBorder(null);
		button.setGraphicTextGap(0);
		button.setFocusTraversable(false);		
		button.setGraphic(ImageUtils.createIcon(iconLocation, 14));
		button.setStyle("-fx-background-color: transparent;");
		
		final MenuItem detach = new MenuItem("Detach to a separate window", ImageUtils.createIcon("tab-detach", 14, "pane-settings-menu-graphic"));
		detach.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{				
				paneVisibilityManager.setPaneVisiblity(
						paneVisibilityManager.getPaneToStatusMapping().get(pane), 
						PaneVisibilityStatus.DETACHED);				
			}
		});
		final MenuItem hide = new MenuItem("Hide this pane", ImageUtils.createIcon("tab-close", 14, "pane-settings-menu-graphic"));
		hide.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{				
				paneVisibilityManager.setPaneVisiblity(
						paneVisibilityManager.getPaneToStatusMapping().get(pane), 
						PaneVisibilityStatus.NOT_VISIBLE);				
			}
		});
		button.getItems().add(detach);
		button.getItems().add(hide);
		
		for (MenuItem item : button.getItems())
		{
			item.getStyleClass().add("pane-settings-menu-item");
		}
		
		button.setTextAlignment(TextAlignment.RIGHT);
		button.setAlignment(Pos.CENTER_RIGHT);
		AnchorPane.setRightAnchor(button, offset);
		
		return button;
	}
	
	public static MenuButton createTitleButtons(final TitledPaneController controller,
			final AnchorPane paneTitle, final PaneVisibilityManager paneVisibilityManager)	
	{
		final TitledPane pane = controller.getTitledPane();
		
		final MenuButton settingsButton = createTitleButton("Pane settings", "settings", -5, paneVisibilityManager, pane);
			      
		final HBox titleBox = new HBox();
		titleBox.setPadding(new Insets(0, 0, 0, 0));	
		logger.trace(pane + ", " + paneTitle + ", " + paneVisibilityManager);
		titleBox.getChildren().addAll(controller.getTitleLabel());
		titleBox.prefWidth(Double.MAX_VALUE);		
		
		paneTitle.setPadding(new Insets(0, 0, 0, 0));
		paneTitle.getChildren().addAll(titleBox, settingsButton);
		paneTitle.setMaxWidth(Double.MAX_VALUE);
		
		pane.setText(null);
		pane.setGraphic(paneTitle);
		pane.widthProperty().addListener(createPaneTitleWidthListener(pane, paneTitle));
		
		return settingsButton;
	}
	
	public static double updateTitleWidth(final TitledPane titledPane, final AnchorPane paneTitle, final double margin, Double target)
	{
		final double marginWithPositionOfset = margin + paneTitle.getLayoutX();
				
		double titledPaneWidth = titledPane.getWidth();
		// logger.debug("{}; titledPane.getWidth() = {}, paneTitle.getWidth() = {}; margin = {}; target = {}", titledPane, titledPaneWidth, paneTitle.getWidth(), margin, target);
		
		
		if (titledPane.getScene() != null)			
		{
			if (titledPane.getScene().getWidth() < titledPaneWidth)
			{
				titledPaneWidth = titledPane.getScene().getWidth();
				logger.debug("Scene is smaller; {} titledPane.getScene().getWidth() = {}", titledPane, titledPaneWidth);				
			}
		}
		
		double jump = 0;
		final double newWidth = titledPaneWidth - marginWithPositionOfset;
		
		// If target specified, and the diff in resizing is less than X, adjust using the target. 
		// This is a workaround for the JavaFX resizing behaviour, otherwise it would be less responsive on making the window smaller
		if (target != null)
		{
			if (paneTitle.getWidth() - newWidth < 30 && paneTitle.getWidth() - newWidth > 0)
			{
				jump = target;
			}
		}
		
		updatePaneTitleWidth(paneTitle, newWidth - jump);
		
		return titledPaneWidth;
	}
	
	private static void updatePaneTitleWidth(final AnchorPane paneTitle, final double width)
	{
		logger.debug("Setting title pane width to {}", width);
		
		paneTitle.setPrefWidth(width);				
		paneTitle.setMaxWidth(width);
	}
	
	// ************

	/**
	 * Sets the configuration manager.
	 * 
	 * @param configurationManager the configurationManager to set
	 */
	public void setConfigurationManager(final IConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
	
	/**
	 * Sets the event bus.
	 *  
	 * @param eventBus the eventBus to set
	 */
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

	public void setVersionManager(final VersionManager versionManager)
	{
		this.versionManager = versionManager;		
	}
	
	public void setApplication(Application application)
	{
		this.application = application;
	}
	
	public void setConnectionFactory(final IConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
	}
	
	public void setGenericScriptManager(final BaseScriptManagerInterface genericBaseScriptManager)
	{
		this.genericScriptManager = genericBaseScriptManager;
	}

	public void setStatisticsManager(final StatisticsManager statisticsManager)
	{
		this.statisticsManager = statisticsManager;		
	}
	
	public SpyPerspective getPerspective()
	{
		return selectedPerspective;
	}
}
