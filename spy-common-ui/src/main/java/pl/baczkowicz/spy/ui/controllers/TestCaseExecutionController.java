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
package pl.baczkowicz.spy.ui.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.scripts.ScriptRunningState;
import pl.baczkowicz.spy.testcases.TestCaseStatus;
import pl.baczkowicz.spy.ui.properties.TestCaseProperties;
import pl.baczkowicz.spy.ui.properties.TestCaseStepProperties;
import pl.baczkowicz.spy.ui.testcases.InteractiveTestCaseManager;
import pl.baczkowicz.spy.ui.utils.ImageUtils;

/**
 * Controller for the search window.
 */
public class TestCaseExecutionController extends AnchorPane implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(TestCaseExecutionController.class);	
	
	@FXML
	private CheckMenuItem autoExport;
	
	@FXML
	private Button startButton;
	
	@FXML
	private Button stopButton;
	 
	@FXML
	private TableView<TestCaseStepProperties> stepsView;
	
	@FXML
	private TableColumn<TestCaseStepProperties, String> stepNumberColumn;
	
	@FXML
	private TableColumn<TestCaseStepProperties, String> descriptionColumn;
	
	@FXML
	private TableColumn<TestCaseStepProperties, TestCaseStatus> statusColumn;
	
	@FXML
	private TableColumn<TestCaseStepProperties, String> infoColumn;

	private TestCaseProperties testCaseProperties;

	private InteractiveTestCaseManager testCaseManager;
	
	public void initialize(URL location, ResourceBundle resources)
	{
		stepNumberColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, String>("stepNumber"));
		stepNumberColumn.setCellFactory(new Callback<TableColumn<TestCaseStepProperties, String>, TableCell<TestCaseStepProperties, String>>()
		{
			public TableCell<TestCaseStepProperties, String> call(TableColumn<TestCaseStepProperties, String> param)
			{
				final TableCell<TestCaseStepProperties, String> cell = new TableCell<TestCaseStepProperties, String>()
				{
					@Override
					public void updateItem(String item, boolean empty)
					{
						super.updateItem(item, empty);
						setText(item);
					}
				};
				cell.setAlignment(Pos.CENTER);
				
				return cell;
			}
		});
		
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, String>("description"));
		
		statusColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, TestCaseStatus>("status"));
		statusColumn.setCellFactory(new Callback<TableColumn<TestCaseStepProperties, TestCaseStatus>, TableCell<TestCaseStepProperties, TestCaseStatus>>()
		{
			public TableCell<TestCaseStepProperties, TestCaseStatus> call(TableColumn<TestCaseStepProperties, TestCaseStatus> param)
			{
				final TableCell<TestCaseStepProperties, TestCaseStatus> cell = new TableCell<TestCaseStepProperties, TestCaseStatus>()
				{
					@Override
					public void updateItem(TestCaseStatus item, boolean empty)
					{
						super.updateItem(item, empty);
						
						if (!isEmpty())
						{
							setGraphic(getIconForStatus(item));
						}
						else
						{
							setGraphic(null);
						}
					}
				};
				cell.setAlignment(Pos.CENTER);
				cell.setPadding(new Insets(0, 0, 0, 0));
				
				return cell;
			}
		});
		
		infoColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, String>("executionInfo"));
		
		// Note: important - without that, cell height goes nuts with progress indicator
		stepsView.setFixedCellSize(24);
		
		// TODO: for testing only
		stepsView.setTableMenuButtonVisible(true);	
		
		stepsView.setPlaceholder(new Label("No test steps available - select a test case..."));
	}	

	public void init()
	{
		//
	}	
	
	public boolean isAutoExportEnabled()
	{
		return autoExport.isSelected();
	}
	
	@FXML
	private void exportResult()	
	{
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select location for the result file");				
		final File selectedFile = fileChooser.showSaveDialog(stepsView.getScene().getWindow());

		if (selectedFile != null)
		{			
			testCaseManager.exportTestCaseResultAsCSV(testCaseProperties.getScript(), selectedFile);
		}
	}
	
	public void display(final TestCaseProperties testCaseProperties, final ObservableList<TestCaseStepProperties> items)
	{
		this.testCaseProperties = testCaseProperties;
		
		refreshState();
		stepsView.getItems().clear();
		logger.info("Item to display = " + items.size());
		stepsView.getItems().addAll(items);
	}
	
	public void refreshState()
	{
		if (ScriptRunningState.RUNNING.equals(testCaseProperties.getScript().getStatus()))
		{
			startButton.setDisable(true);
			stopButton.setDisable(false);
		}
		else
		{
			startButton.setDisable(false);
			stopButton.setDisable(true);
		}
	}
	
	@FXML
	private void startTestCase()
	{
		testCaseManager.runTestCase(testCaseProperties);
		refreshState();
	}
	
	@FXML
	private void stopTestCase()
	{
		testCaseManager.stopTestCase(testCaseProperties);
		refreshState();
	}
	
	public Node getIconForStatus(final TestCaseStatus status)
	{
		String iconName = null;
		
		switch (status)
		{
			case ACTIONED:
				iconName = "testcase_actioned";
				break;
			case ERROR:
				iconName = "testcase_error";
				break;
			case FAILED:
				iconName = "testcase_fail";
				break;
			case IN_PROGRESS:
			{
				final ProgressIndicator progressIndicator = new ProgressIndicator();
				progressIndicator.setMaxSize(18, 18);
				progressIndicator.setPadding(new Insets(0, 0, 0, 0));				
				return progressIndicator;
			}
			case NOT_RUN:
				break;
			case PASSED:
				iconName = "testcase_pass";
				break;
			case SKIPPED:
				iconName = "testcase_skipped";
				break;
			default:
				break;		
		}
	
		if (iconName != null)
		{
			return ImageUtils.createIcon(iconName);
		}
		
		return null;
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setTestCaseManager(final InteractiveTestCaseManager testCaseManager)
	{
		this.testCaseManager = testCaseManager;
	}
}
